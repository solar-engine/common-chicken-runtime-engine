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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Jar extends Artifact {

    private final JarFile jf;
    private final File f;
    private File preserved;

    public Jar(File f) throws IOException {
        this.jf = new JarFile(f);
        this.f = f;
    }

    Jar(File f, boolean preserved) throws IOException {
        this.jf = new JarFile(f);
        this.f = f;
        if (preserved) {
            this.preserved = f;
        }
    }

    @Override
    public String[] listClassesAndResources() {
        ArrayList<String> out = new ArrayList<>();
        for (JarEntry entry : Collections.list(jf.entries())) {
            if (!entry.isDirectory()) {
                out.add(entry.getName());
            }
        }
        return out.toArray(new String[0]);
    }

    @Override
    public InputStream loadResource(String name) throws IOException {
        JarEntry ent = jf.getJarEntry(name.startsWith("/") ? name.substring(1) : name);
        if (ent == null) {
            throw new NoSuchFileException("Cannot find JAR entry: " + name);
        }
        return jf.getInputStream(ent);
    }

    @Override
    public void close() throws Exception {
        jf.close();
    }

    @Override
    public Jar toJar(boolean preserve) throws IOException {
        if (preserve && preserved == null) {
            File out = File.createTempFile("jar-", ".jar");
            Files.copy(f.toPath(), out.toPath());
            this.preserved = out;
        }
        return this;
    }

    public File toFile() {
        return preserved;
    }
}
