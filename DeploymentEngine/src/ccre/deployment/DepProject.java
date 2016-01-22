/*
 * Copyright 2015 Colby Skeggs, 2016 Alexander Mackworth.
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

/**
 * A set of utilities to track the active project and CCRE projects. Paths to
 * these are stored in this class statically.
 *
 * @author skeggsc
 */
public class DepProject {
    private static File projectRoot, ccreRoot;

    /**
     * Set the project root and the CCRE root to use during Deployment Engine
     * execution.
     *
     * @param root the project root directory.
     * @param ccreRoot the CCRE root directory, which contains its projects.
     */
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

    /**
     * Provides the folder for the current project.
     *
     * @return the project folder as a File.
     * @throws IllegalStateException if the project folder has not yet been set.
     */
    public static File root() throws IllegalStateException {
        if (projectRoot == null) {
            throw new IllegalStateException("Root not yet initialized!");
        }
        return projectRoot;
    }

    /**
     * Provides the folder containing the CCRE projects.
     *
     * @return the CCRE root folder as a file.
     * @throws IllegalStateException if the CCRE folder has not yet been set.
     */
    public static File ccreRoot() {
        if (ccreRoot == null) {
            throw new IllegalStateException("Root not yet initialized!");
        }
        return ccreRoot;
    }

    /**
     * Finds or creates the directory named <code>name</code> in the project
     * root directory.
     *
     * If the named file exists but is not a directory, an exception is thrown.
     *
     * @param name the name of the directory.
     * @return the File representing the found or created directory.
     */
    public static File directoryOrCreate(String name) {
        File f = directory(name);
        if (!f.exists()) {
            if (!f.mkdir()) {
                throw new RuntimeException("Could not create missing directory: " + f);
            }
        }
        return f;
    }

    /**
     * Finds the directory named <code>name</code> in the project root
     * directory. If it doesn't exist, it returns it anyway.
     *
     * If the named file exists but is not a directory, an exception is thrown.
     *
     * @param name the name of the directory.
     * @return the File representing the found or created directory.
     */
    public static File directory(String name) {
        File f = new File(root(), name);
        if (f.exists() && !f.isDirectory()) {
            throw new RuntimeException("Directory is not a directory: " + f);
        }
        return f;
    }

    /**
     * Finds the root directory for the specified CCRE project.
     *
     * @param name the name of the CCRE project.
     * @return the root directory as a file.
     */
    public static File ccreProject(String name) {
        return new File(ccreRoot(), name);
    }

    private DepProject() {
    }

    /**
     * Finds the name of the current project's directory.
     *
     * @return the last element of the current project's folder path.
     */
    public static String name() {
        return root().getName();
    }

    /**
     * Locates the DeploymentEngine's own jar.
     *
     * @return the File representing the DeploymentEngine jar.
     */
    public static File getDepEngineJar() {
        return new File(DepProject.ccreProject("DeploymentEngine"), "DepEngine.jar");
    }

}
