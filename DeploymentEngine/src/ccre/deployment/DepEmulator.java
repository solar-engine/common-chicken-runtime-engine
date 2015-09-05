/*
 * Copyright 2015 Colby Skeggs.
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
package ccre.deployment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.NoSuchFileException;
import java.util.jar.JarFile;

public class DepEmulator {
    public static void emulate(Artifact userCode) throws Exception {
        Jar userJar = userCode.toJar(false);
        File userJarFile = userJar.toFile();
        String userJarFilePath = userJarFile.getAbsolutePath();

        File cwd = DepProject.directory("emulation-roboRIO");
        if (!cwd.exists() && !cwd.mkdir()) {
            throw new IOException("Could not create directory: " + cwd);
        }

        File emulator = new File(DepProject.ccreProject("Emulator"), "Emulator.jar");
        if (!emulator.exists()) {
            throw new NoSuchFileException("Cannot find Emulator jar!");
        }

        URLClassLoader emulatorJar = new URLClassLoader(new URL[] { emulator.toURI().toURL() }, DepEmulator.class.getClassLoader());
        try {
            String main;
            try (JarFile jf = new JarFile(emulator)) {
                main = jf.getManifest().getMainAttributes().getValue("Main-Class");
            }
            if (main == null) {
                throw new RuntimeException("Emulator jar does not define Main-Class!");
            }
            Class<?> mainCls = emulatorJar.loadClass(main);
            Method mainM = mainCls.getMethod("main", String[].class);
            if (!Modifier.isStatic(mainM.getModifiers())) {
                throw new RuntimeException("Main method is not static on " + mainCls + "!");
            }
            mainM.invoke(null, new Object[] {new String[] { userJarFilePath }});
        } finally {
            emulatorJar.close();
        }
    }
}
