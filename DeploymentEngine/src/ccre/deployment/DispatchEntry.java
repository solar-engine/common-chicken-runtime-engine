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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * This class is an entry-point for the Deployment Engine: it allows setting up
 * the Deployment Engine infrastructure and then invoking an arbitrary static
 * method on an arbitrary class.
 *
 * @author skeggsc
 */
public class DispatchEntry {
    /**
     * The main method of the Deployment Engine. This is passed the CCRE root
     * directory and the project root directory, and it is passed a class and
     * method reference for the method to invoke as the main DeploymentEngine
     * method.
     *
     * This is often originally something in
     * {@link ccre.deployment.RebuildBuilders}, which then builds Eclipse
     * External Tools that use this same entrypoint to run individual target
     * tasks.
     *
     * @param args the arguments for the program.
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("DispatchEntry expects four parameters: CCRE root, project root, class name, method name.");
        }
        DepProject.setRoots(new File(args[1]), new File(args[0]));
        try {
            Class.forName(args[2]).getMethod(args[3]).invoke(null);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getCause();
            }
            trimStackTrace(e);
            System.err.println("[some irrelevant stack elements may have been trimmed]");
            e.printStackTrace();
            if (e.getMessage() != null) {
                System.err.println(" \n===> " + e.getMessage() + "\n ");
            } else {
                System.err.println(" \n===> " + e.getClass().getName() + "\n ");
            }
            System.exit(1);
        }
    }

    private static void trimStackTrace(Throwable e) {
        if (e.getCause() != null) {
            trimStackTrace(e.getCause());
        }
        for (Throwable thr : e.getSuppressed()) {
            trimStackTrace(thr);
        }

        StackTraceElement[] stackTrace = e.getStackTrace();
        int last = 0;
        // trim stack trace to only include relevant elements: nothing past this
        // invocation!
        while (last < stackTrace.length - 1 && !(stackTrace[last].getClassName().equals(DispatchEntry.class.getName()) && stackTrace[last].getMethodName().equals("main"))) {
            last++;
        }
        e.setStackTrace(Arrays.copyOf(stackTrace, last + 1));
    }
}
