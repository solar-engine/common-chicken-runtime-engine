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

public class RebuildBuilders {
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
            throw new RuntimeException("Cannot find deployment.Deployment! Perhaps you should add it...");// TODO: autoadd it?
        }
    }

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
                if (annotName.isEmpty()) {
                    annotName = Character.toUpperCase(m.getName().charAt(0)) + m.getName().substring(1);
                }
                rebuild(deployment, annotName, m.getName(), annot.fork());
            }
        }
    }

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
