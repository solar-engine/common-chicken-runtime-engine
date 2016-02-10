/*
 * Copyright 2015-2016 Cel Skeggs.
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

/**
 * A collection of class files and resources inside a Jar file.
 *
 * A number of methods that construct or work with Jars have an option for
 * preservation. Preservation means that the Jar should stay around after the
 * program has ended. However, it can be removed automatically when the
 * temporary directory is next cleaned by you or your operating system. If you
 * don't specify preservation, then a best-effort attempt will be made to delete
 * the Jar when the JVM exits.
 *
 * @author skeggsc
 */
public class Jar extends Artifact {

    private final JarFile jarfile;
    private final File jar;
    private File preserved;

    /**
     * Wraps the Jar in the specified file.
     *
     * @param jar the file to wrap.
     * @throws IOException
     */
    public Jar(File jar) throws IOException {
        this.jarfile = new JarFile(jar);
        this.jar = jar;
    }

    Jar(File jar, boolean preserved) throws IOException {
        this.jarfile = new JarFile(jar);
        this.jar = jar;
        if (preserved) {
            this.preserved = jar;
        }
    }

    @Override
    protected String[] listClassesAndResources() {
        ArrayList<String> out = new ArrayList<>();
        for (JarEntry entry : Collections.list(jarfile.entries())) {
            if (!entry.isDirectory()) {
                out.add(entry.getName());
            }
        }
        return out.toArray(new String[0]);
    }

    @Override
    public InputStream loadResource(String name) throws IOException {
        JarEntry ent = jarfile.getJarEntry(name.startsWith("/") ? name.substring(1) : name);
        if (ent == null) {
            throw new NoSuchFileException("Cannot find JAR entry: " + name);
        }
        return jarfile.getInputStream(ent);
    }

    @Override
    public void close() throws IOException {
        jarfile.close();
    }

    @Override
    public Jar toJar(boolean preserve) throws IOException {
        if (preserve && preserved == null) {
            File out = File.createTempFile("jar-", ".jar");
            out.delete(); // TODO: do this better
            Files.copy(jar.toPath(), out.toPath());
            this.preserved = out;
        }
        return this;
    }

    /**
     * Converts this Jar to a backing file on the filesystem.
     *
     * @return a file version of this Jar.
     */
    public File toFile() {
        return preserved != null ? preserved : jar;
    }
}
