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

public class DepProject {
    private static File projectRoot, ccreRoot;

    static void setRoots(File root, File ccreRoot) {
        if (projectRoot != null || DepProject.ccreRoot != null) {
            throw new IllegalStateException("Root already initialized!");
        }
        if (root == null || ccreRoot == null) {
            throw new NullPointerException("Root is null!");
        }
        if (!root.exists()) {
            throw new IllegalArgumentException("Project root does not exist!");
        }
        if (!ccreRoot.exists()) {
            throw new IllegalArgumentException("CCRE root does not exist!");
        }
        projectRoot = root;
        DepProject.ccreRoot = ccreRoot;
    }

    public static File root() {
        if (projectRoot == null) {
            throw new IllegalStateException("Root not yet initialized!");
        }
        return projectRoot;
    }

    public static File ccreRoot() {
        if (ccreRoot == null) {
            throw new IllegalStateException("Root not yet initialized!");
        }
        return ccreRoot;
    }

    public static File directoryOrCreate(String name) {
        File f = directory(name);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new RuntimeException("Could not create missing directory: " + f);
            }
        }
        return f;
    }

    public static File directory(String name) {
        File f = new File(root(), name);
        if (f.exists() && !f.isDirectory()) {
            throw new RuntimeException("Directory is not a directory: " + f);
        }
        return f;
    }

    public static File ccreProject(String name) {
        return new File(ccreRoot(), name);
    }

    private DepProject() {
    }

    public static String name() {
        return root().getName();
    }
}
