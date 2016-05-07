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

import java.io.IOException;
import java.io.InputStream;

/**
 * A built artifact, as in a collection of classes and resources stored
 * somewhere in some form.
 *
 * For example, this could be a folder, a JAR, or really anything else.
 *
 * @author skeggsc
 */
public abstract class Artifact implements AutoCloseable {
    /**
     * Lists the class names contained in this Artifact, in the form
     * <code>outer.package.inner.package.ClassName</code>.
     *
     * @return the array of class names.
     */
    public String[] listClassNames() {
        String[] in = listClassesAndResources();
        int count = 0;
        for (int i = 0; i < in.length; i++) {
            String name = in[i];
            if (name.endsWith(".class")) {
                // convert resource name to class name
                in[i] = name.substring(name.charAt(0) == '/' ? 1 : 0, name.length() - 6).replace('/', '.');
                count++;
            } else {
                in[i] = null;
            }
        }
        String[] out = new String[count];
        int i = 0;
        for (String name : in) {
            if (name != null) {
                out[i++] = name;
            }
        }
        if (i != count) {
            throw new RuntimeException("Internal error...?");
        }
        return out;
    }

    /**
     * Lists the names of the resources contained in this Artifact. These are
     * any files that aren't <code>.class</code> files.
     *
     * @return the array of resource names.
     */
    public String[] listResources() {
        String[] in = listClassesAndResources();
        int count = 0;
        for (int i = 0; i < in.length; i++) {
            String name = in[i];
            if (name.endsWith(".class")) {
                in[i] = null;
            } else {
                count++;
            }
        }
        String[] out = new String[count];
        int i = 0;
        for (String name : in) {
            if (name != null) {
                out[i++] = name;
            }
        }
        if (i != count) {
            throw new RuntimeException("Internal error...?");
        }
        return out;
    }

    /**
     * Returns the list of all classes and resources, in terms of the file
     * names. This could also be named <code>listFiles</code>.
     *
     * @return the array of filenames.
     */
    protected abstract String[] listClassesAndResources();

    /**
     * Open an InputStream that reads the named class, which can be either in
     * the form <code>package.ClassName</code> or <code>package/ClassName</code>
     * .
     *
     * @param name the name of the class to load.
     * @return an InputStream reading the classfile's data.
     * @throws IOException if the resource cannot be loaded.
     */
    public InputStream loadClassFile(String name) throws IOException {
        return loadResource("/" + name.replace('.', '/') + ".class");
    }

    /**
     * Open an InputStream that reads the named resource, which should be a file
     * path. It may optionally start with a forward slash.
     *
     * @param name the file path of the resource to load, within this artifact.
     * @return an InputStream reading the resource's data.
     * @throws IOException if the resource cannot be loaded.
     */
    public abstract InputStream loadResource(String name) throws IOException;

    /**
     * Coerce this artifact into a Jar, which may be optionally marked for
     * preservation.
     *
     * See {@link Jar} for a discussion of what Jar preservation means.
     *
     * If this is already a Jar, we just return the Jar, possibly modified for
     * preservation reasons.
     *
     * @param preserve if this Jar should be marked for preservation.
     * @return the new (or old) Jar containing the same classes and resources as
     * this artifact.
     * @throws IOException if an error occurs during conversion.
     */
    public Jar toJar(boolean preserve) throws IOException {
        JarBuilder jb = new JarBuilder(preserve);
        for (String elem : listClassNames()) {
            jb.addClass(elem, loadClassFile(elem));
        }
        for (String elem : listResources()) {
            jb.addResource(elem, loadResource(elem));
        }
        return jb.build();
    }

    public abstract void close() throws IOException;
}
