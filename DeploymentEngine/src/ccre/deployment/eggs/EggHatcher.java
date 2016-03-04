/*
 * Copyright 2015-2016 Alexander Mackworth
 *
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 *
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package ccre.deployment.eggs;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import ccre.deployment.Artifact;
import ccre.deployment.Jar;
import ccre.log.Logger;

/**
 * The main class invoked inside a CCRE Egg to deploy the Egg.
 *
 * @author amackworth
 */
public class EggHatcher {
    private final static String banner = "                       .-~-.\n" +
            "                     .'     '.\n" +
            "                    /         \\\n" +
            "            .-~-.  :           ;\n" +
            "          .'     '.|           |\n" +
            "         /         \\           :\n" +
            "        :           ; .-~\"\"~-,/          hatching egg...\n" +
            "        |           /`        `'.\n" +
            "        :          |             \\\n" +
            "         \\         |             /\n" +
            "          `.     .' \\          .'\n" +
            "     jgs    `~~~`    '-.____.-'\n\n";

    /**
     * Deploys the egg contained by the current Jar, following the deployment
     * action that was specified when the egg was created.
     *
     * @param args the program arguments, which are ignored.
     * @throws Exception if the egg cannot be hatched.
     */
    public static void main(String[] args) throws Exception {
        System.out.println(banner);

        InputStream jarStream = EggHatcher.class.getResourceAsStream("/" + DepEgg.userCodeJarName);
        File tempFile = File.createTempFile("hatched-egg-", ".jar");
        Files.copy(jarStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        try (Artifact userCode = new Jar(tempFile)) {
            // Thanks to some OS X/Java weirdness, we have to make sure we're
            // loading the manifest out of this bundle. We do this by counting
            // the number of manifest resources in the parent, and indexing past
            // it.
            // FIXME: this is hacky af
            ClassLoader parent = EggHatcher.class.getClassLoader().getParent();
            int parentEntries;
            if (parent != null) {
                parentEntries = Collections.list(parent.getResources("META-INF/MANIFEST.MF")).size();
            } else {
                parentEntries = Collections.list(ClassLoader.getSystemResources("META-INF/MANIFEST.MF")).size();
            }

            ArrayList<URL> manifests = Collections.list(EggHatcher.class.getClassLoader().getResources("META-INF/MANIFEST.MF"));
            Manifest manifest = new Manifest(manifests.get(parentEntries).openStream());

            Attributes attrs = manifest.getMainAttributes();
            String className = attrs.getValue("Hatch-Action");
            if (className == null) {
                Logger.severe("Hatch action not found in manifest!");
                return;
            }

            try (URLClassLoader classLoader = new URLClassLoader(new URL[] { tempFile.toURI().toURL() })) {
                Class<?> deploymentClass = classLoader.loadClass(className);
                Constructor<?> construct = deploymentClass.getDeclaredConstructor();
                construct.setAccessible(true);
                Method deployMethod = deploymentClass.getMethod("deployArtifact", Artifact.class);
                deployMethod.setAccessible(true);
                deployMethod.invoke(construct.newInstance(), userCode);
            }
        }
    }
}
