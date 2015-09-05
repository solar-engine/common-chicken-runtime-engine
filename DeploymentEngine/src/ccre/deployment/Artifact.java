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

import java.io.IOException;
import java.io.InputStream;

public abstract class Artifact implements AutoCloseable {
    public String[] listClassNames() {
        String[] in = listClassesAndResources();
        int count = 0;
        for (int i = 0; i < in.length; i++) {
            String name = in[i];
            if (name.endsWith(".class")) {
                in[i] = name.substring(name.charAt(0) == '/' ? 1 : 0, name.length() - 6).replace('/', '.');// convert resource name to class name
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

    public abstract String[] listClassesAndResources();

    public InputStream loadClassFile(String name) throws IOException {
        return loadResource("/" + name.replace('.', '/') + ".class");
    }

    public abstract InputStream loadResource(String name) throws IOException;

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
}
