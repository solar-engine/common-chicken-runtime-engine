/*
 * Copyright 2015 Cel Skeggs, 2016 Alexander Mackworth.
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * A helper class for putting together a Jar from its individual components.
 *
 * @author skeggsc
 */
public class JarBuilder {

    /**
     * A constant that means that a Jar should be preserved after JVM exit.
     *
     * @see Jar
     */
    public static final boolean PRESERVE = true;
    /**
     * A constant that means that a Jar should be deleted after JVM exit.
     *
     * @see Jar
     */
    public static final boolean DELETE = false;

    /**
     * A constant that means that the manifest should be taken from an Artifact
     * when added to the builder.
     *
     * @see JarBuilder#addAll(Artifact, boolean)
     */
    public static final boolean KEEP_MANIFEST = true;
    /**
     * A constant that means that the manifest should not be taken from an
     * Artifact when added to the builder.
     *
     * @see JarBuilder#addAll(Artifact, boolean)
     */
    public static final boolean DISCARD_MANIFEST = false;

    private static final String MANIFEST = "META-INF/MANIFEST.MF";

    private final File tempOut;
    private final JarOutputStream jout;
    private final boolean preserved;

    /**
     * Creates a new JarBuilder that optionally preserves the result. No
     * manifest is specified.
     *
     * @param preserve if the generated Jar should be preserved.
     * @throws IOException if the new temporary Jar cannot be created.
     * @see Jar for an explanation of preservation.
     */
    public JarBuilder(boolean preserve) throws IOException {
        this(null, preserve);
    }

    /**
     * Creates a new JarBuilder that optionally preserves the result. A manifest
     * is specified.
     *
     * @param mf the manifest to include in the Jar.
     * @param preserve if the generated Jar should be preserved.
     * @throws IOException if the new temporary Jar cannot be created.
     * @see Jar for an explanation of preservation.
     */
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

    /**
     * Adds a class with the given dot-format name and with data specified by an
     * input stream.
     *
     * @param elem the dot-format name of the class, such as
     * <code>java.lang.Object</code>.
     * @param is the InputStream that carries the class data for this class.
     * @throws IOException if the InputStream fails, or if the Jar output fails.
     */
    public void addClass(String elem, InputStream is) throws IOException {
        addResource(elem.replace('.', '/') + ".class", is);
    }

    /**
     * Adds a resource with the given path and with data specified by an input
     * stream.
     *
     * @param name the path of the resource.
     * @param is the InputStream that carries the resource data for this
     * resource.
     * @throws IOException if the InputStream fails, or if the Jar output fails.
     */
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

    /**
     * Adds a resource with the given path and with data from a File.
     *
     * @param name the path of the resource.
     * @param file the File that carries the resource data for this resource.
     * @throws IOException if the file reading fails, or if the Jar output
     * fails.
     */
    public void addResource(String name, File file) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            this.addResource(name, is);
        }
    }

    /**
     * Adds all of the classes and resources from <code>artifact</code>, and
     * optionally the manifest.
     *
     * @param artifact the artifact to read data from.
     * @param andManifest if the manifest should be taken from this artifact.
     * @throws IOException if the reading fails, or if the Jar output fails.
     */
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

    /**
     * Finalizes this Jar and converts it to a {@link Jar}.
     *
     * @return the built Jar.
     * @throws IOException if the Jar cannot be converted properly.
     */
    public Jar build() throws IOException {
        jout.close();
        return new Jar(tempOut, preserved);
    }
}
