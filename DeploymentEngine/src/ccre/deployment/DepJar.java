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
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class DepJar {

    public static Manifest manifest(Map<String, String> entries) {
        Manifest mf = new Manifest();
        Attributes attrs = mf.getMainAttributes();
        if (!entries.containsKey("Manifest-Version")) {
            attrs.putValue("Manifest-Version", "1.0");
        }
        for (Map.Entry<String, String> ent : entries.entrySet()) {
            attrs.putValue(ent.getKey(), ent.getValue());
        }
        return mf;
    }

    public static Manifest manifest(String... kvs) {
        if ((kvs.length & 1) != 0) {
            throw new IllegalArgumentException("Invalid number of keys and values.");
        }
        HashMap<String, String> hm = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            if (kvs[i] == null || kvs[i + 1] == null) {
                throw new IllegalArgumentException("Keys and values cannot be null!");
            }
            hm.put(kvs[i], kvs[i + 1]);
        }
        return manifest(hm);
    }

    public static Jar combine(Manifest manifest, boolean preserve, Artifact... artifacts) throws IOException {
        JarBuilder jb = new JarBuilder(manifest, preserve);
        for (Artifact artifact : artifacts) {
            jb.addAll(artifact, manifest == null);
        }
        return jb.build();
    }

    public static Jar combine(boolean preserve, Artifact... artifacts) throws IOException {
        return combine(null, preserve, artifacts);
    }
}
