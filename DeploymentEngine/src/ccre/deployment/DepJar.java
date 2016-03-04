/*
 * Copyright 2015 Cel Skeggs.
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

/**
 * A collection of utilities for working with Jar files in the context of
 * deployment systems. This includes things like merging Jars and creating and
 * adding manifests.
 *
 * @author skeggsc
 */
public class DepJar {

    /**
     * Create a new Manifest from a Map of key-value pairs. If
     * <code>Manifest-Version</code> is not specified, it will be set to
     * <code>1.0</code>.
     *
     * @param entries the key-value pairs to include.
     * @return the constructed Manifest.
     */
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

    /**
     * Create a new Manifest from an array of keys and values. If
     * <code>Manifest-Version</code> is not specified, it will be set to
     * <code>1.0</code>.
     *
     * The <code>kvs[0]</code> is a key for <code>kvs[1]</code>, which is a
     * value. The <code>kvs[2]</code> is a key for <code>kvs[3]</code>, which is
     * a value. And so on and so forth.
     *
     * @param kvs the keys and values to include in the manifest.
     * @return the constructed Manifest.
     * @throws IllegalArgumentException if <code>kvs</code> does not have an
     * even number of elements, or if any element is null.
     */
    public static Manifest manifest(String... kvs) throws IllegalArgumentException {
        if ((kvs.length & 1) != 0) {
            throw new IllegalArgumentException("Invalid number of keys and values.");
        }
        HashMap<String, String> hm = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            if (kvs[i] == null || kvs[i + 1] == null) {
                throw new NullPointerException("Keys and values cannot be null!");
            }
            hm.put(kvs[i], kvs[i + 1]);
        }
        return manifest(hm);
    }

    /**
     * Construct a new Jar from a set of artifacts, and a manifest.
     *
     * If <code>manifest</code> is null, then it will be obtained from an
     * artifact that also has a manifest.
     *
     * See {@link Jar} for a discussion of what the <code>preserve</code>
     * argument means.
     *
     * Behavior is undefined when the same class file is provided by multiple
     * artifacts, or if a manifest is not provided but multiple artifacts
     * provide manifests.
     *
     * @param manifest the manifest to include in this Jar, which may be null.
     * @param preserve if this Jar should be marked for preservation.
     * @param artifacts the artifacts that contain the class files and resources
     * to include.
     * @return the newly-constructed Jar.
     * @throws IOException if the Jar cannot be constructed.
     */
    public static Jar combine(Manifest manifest, boolean preserve, Artifact... artifacts) throws IOException {
        JarBuilder jb = new JarBuilder(manifest, preserve);
        for (Artifact artifact : artifacts) {
            jb.addAll(artifact, manifest == null);
        }
        return jb.build();
    }

    /**
     * Construct a new Jar from a set of artifacts.
     *
     * The manifest then it will be obtained from an artifact that also has a
     * manifest.
     *
     * See {@link Jar} for a discussion of what the <code>preserve</code>
     * argument means.
     *
     * Behavior is undefined when the same class file is provided by multiple
     * artifacts, or if a manifest is not provided but multiple artifacts
     * provide manifests.
     *
     * @param preserve if this Jar should be marked for preservation.
     * @param artifacts the artifacts that contain the class files and resources
     * to include.
     * @return the newly-constructed Jar.
     * @throws IOException if the Jar cannot be constructed.
     */
    public static Jar combine(boolean preserve, Artifact... artifacts) throws IOException {
        return combine(null, preserve, artifacts);
    }
}
