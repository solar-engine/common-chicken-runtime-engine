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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import ccre.util.Utils;

/**
 * A class that takes care of rebuilding {@link DepTask} tasks for a project,
 * based on the static methods available in its
 * <code>deployment.Deployment</code> class.
 *
 * These tasks are put into the Eclipse external tools listing, where the user
 * can select between them.
 *
 * @author skeggsc
 */
public class RebuildBuilders {
    /**
     * Rebuilds the {@link DepTask} tasks for the current project, unless it is
     * TemplateRobot.
     *
     * @throws IOException
     */
    public static void rebuild() throws IOException {
        if ("TemplateRobot".equals(DepProject.name())) {
            System.out.println("Not building launchers for TemplateRobot.");
            return;
        }
        File[] launches = DepProject.directory("launches").listFiles();
        if (launches != null) {
            for (File f : launches) {
                f.delete();
            }
        }
        System.out.println("Rebuilding launchers...");
        try {
            rebuild(Class.forName("deployment.Deployment"));
        } catch (ClassNotFoundException e) {
            // TODO: autoadd it?
            throw new RuntimeException("Cannot find deployment.Deployment! Perhaps you should add it...");
        }
    }

    /**
     * Rebuilds the {@link DepTask} tasks for the current project, based on the
     * static methods available in the deployment class <code>deployment</code>.
     *
     * @param deployment the deployment class that includes the deployment
     * tasks.
     * @throws IOException
     */
    public static void rebuild(Class<?> deployment) throws IOException {
        for (Method m : deployment.getMethods()) {
            if (m.isAnnotationPresent(DepTask.class)) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    throw new RuntimeException("Invalid method: " + m + ": annotated with @DepTask without being static!");
                }
                if (m.getParameterCount() != 0) {
                    throw new RuntimeException("Invalid method: " + m + ": annotated with @DepTask and doesn't have zero parameters!");
                }
                DepTask annot = m.getAnnotation(DepTask.class);
                String annotName = annot.name();
                String fullName = m.getName();
                // Split the name by capital letters. Weird regex magic here
                List<String> words = Arrays.asList(fullName.split("(?<!^)(?=[A-Z])"));
                if (annotName.isEmpty()) {
                    for (int i = 0; i < words.size(); i++) {
                        String name = words.get(i);
                        words.set(i, Character.toUpperCase(name.charAt(0)) + name.substring(1));
                    }
                    annotName = Utils.joinStrings(words, " ");
                }

                rebuild(deployment, annotName, m.getName(), annot.fork());
            }
        }
    }

    /**
     * Rebuilds the specified parsed DepTask by adding a new External Tool
     * launcher to Eclipse to run <code>methodName</code> on
     * <code>deployment</code>, with a <code>displayName</code> and optionally
     * forking if <code>fork</code>.
     *
     * @param deployment the deployment class that includes the deployment
     * tasks.
     * @param displayName the name to display the launcher as.
     * @param methodName the method to invoke on the deployment class.
     * @param fork if a new JVM should be forked to run the deployment task when
     * it is launched.
     * @throws IOException
     */
    public static void rebuild(Class<?> deployment, String displayName, String methodName, boolean fork) throws IOException {
        InputStream resource = RebuildBuilders.class.getResourceAsStream("/ccre/deployment/invocation-template.xml");
        if (resource == null) {
            throw new FileNotFoundException("Cannot find /ccre/deployment/invocation-template.xml!");
        }
        File launcher = new File(DepProject.directoryOrCreate("launches"), DepProject.name() + " " + displayName + ".launch");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(resource))) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(launcher))) {
                String line;
                while ((line = in.readLine()) != null) {
                    out.write(line.replace("PROJECT_ROOT", DepProject.name()).replace("CLASS_NAME", deployment.getName()).replace("METHOD_NAME", methodName).replace("FORK_NEEDED", Boolean.toString(fork)).replace("\n", "") + "\n");
                }
            }
        }
    }
}
