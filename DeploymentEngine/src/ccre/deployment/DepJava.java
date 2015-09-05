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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class DepJava {
    private static final SourceVersion JAVA_SOURCE_VERSION = SourceVersion.RELEASE_8;
    private static final String JAVA_SOURCE_VERSION_OPTION = "1.8";
    private static final String JAVA_TARGET_VERSION_OPTION = "1.8";

    public static Artifact build(File folder, File... classpath) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null) {
            throw new RuntimeException("No java compiler available!");
        }
        if (!javac.getSourceVersions().contains(JAVA_SOURCE_VERSION)) {
            throw new IllegalArgumentException("Source version not supported by local compiler: " + JAVA_SOURCE_VERSION);
        }

        StandardJavaFileManager fileManager = javac.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(walkSourceFiles(folder));

        File destdir = Files.createTempDirectory("jclasses").toFile();
        Runtime.getRuntime().addShutdownHook(new DirectoryDeleterThread(destdir));

        JavaCompiler.CompilationTask task = javac.getTask(null, null, null, Arrays.asList("-classpath", classpathToOption(destdir, classpath), "-d", destdir.getAbsolutePath(), "-source", JAVA_SOURCE_VERSION_OPTION, "-target", JAVA_TARGET_VERSION_OPTION), null, compilationUnits);

        if (!task.call()) {
            throw new RuntimeException("Could not complete compilation! See output for details.");
        }

        return new Folder(destdir);
    }

    private static File[] walkSourceFiles(File folder) throws IOException {
        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            return stream.map(x -> x.toFile()).filter(t -> t.getName().endsWith(".java") && !t.isDirectory()).toArray(len -> new File[len]);
        }
    }

    private static String classpathToOption(File first, File... classpath) {
        if (!first.exists()) {
            throw new IllegalArgumentException("Classpath element does not exist: " + first);
        }
        StringBuilder cp = new StringBuilder(first.getAbsolutePath());
        for (File elem : classpath) {
            if (!elem.exists()) {
                throw new IllegalArgumentException("Classpath element does not exist: " + elem);
            }
            cp.append(File.pathSeparatorChar);
            cp.append(elem.getAbsolutePath());
        }
        return cp.toString();
    }
}
