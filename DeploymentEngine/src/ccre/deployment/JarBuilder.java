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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class JarBuilder {

    public static final boolean PRESERVE = true;
    public static final boolean DELETE = false;
    private static final String MANIFEST = "META-INF/MANIFEST.MF";

    private final File tempOut;
    private final JarOutputStream jout;
    private final boolean preserved;

    public JarBuilder(boolean preserve) throws IOException {
        this(null, preserve);
    }

    public JarBuilder(Manifest mf, boolean preserve) throws IOException {
        tempOut = File.createTempFile("jb-", ".jar");
        if (!preserve) {
            tempOut.deleteOnExit();
        }
        this.preserved = preserve;
        if (mf == null) {
            jout = new JarOutputStream(new FileOutputStream(tempOut));
        } else {
            jout = new JarOutputStream(new FileOutputStream(tempOut), mf);
        }
    }

    public void addClass(String elem, InputStream is) throws IOException {
        addResource(elem.replace('.', '/') + ".class", is);
    }

    public void addResource(String name, InputStream is) throws IOException {
        if (is == null) {
            throw new NullPointerException();
        }
        try {
            jout.putNextEntry(new ZipEntry(name));
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) > 0) {
                jout.write(buffer, 0, n);
            }
            jout.closeEntry();
        } finally {
            is.close();
        }
    }

    public void addAll(Artifact artifact, boolean andManifest) throws IOException {
        for (String cn : artifact.listClassNames()) {
            addClass(cn, artifact.loadClassFile(cn));
        }
        for (String cn : artifact.listResources()) {
            if (!andManifest && (MANIFEST.equals(cn) || (cn.startsWith("/") && MANIFEST.equals(cn.substring(1))))) {
                continue;// we don't want the manifest!
            }
            addResource(cn, artifact.loadResource(cn));
        }
    }

    public Jar build() throws IOException {
        jout.close();
        return new Jar(tempOut, preserved);
    }
}
