/*
 * Copyright 2015-2016 Cel Skeggs.
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 * An interface to the Emulator that allows for running an emulator instance on
 * user code.
 *
 * @author skeggsc
 */
public class DepEmulator {
    /**
     * Emulate the specified user code.
     *
     * @param userCode the Artifact for the user code.
     * @throws Exception if an error occurs while setting up emulation.
     */
    public static void emulate(Artifact userCode) throws Exception {
        File jarFile = userCode.toJar(false).toFile();
        File storageDir = DepProject.directoryOrCreate("emulator-logs");
        String mainClass = getMainClassName(jarFile);
        ClassLoader cl = ClassLoader.getSystemClassLoader().getParent();
        @SuppressWarnings("resource")
        URLClassLoader classLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL(), getEmulatorJar().toURI().toURL() }, cl);
        classLoader.loadClass("ccre.frc.DeviceListMain").getMethod("emulate", String.class, File.class).invoke(null, mainClass, storageDir);
    }

    /**
     * Returns the File contain the Jar for the emulator.
     *
     * @return the Jar's path.
     */
    public static File getEmulatorJar() {
        return new File(DepProject.ccreProject("Emulator"), "Emulator.jar");
    }

    private static String getMainClassName(File jarFile) throws IOException {
        try (JarFile frcJar = new JarFile(jarFile)) {
            String className = frcJar.getManifest().getMainAttributes().getValue("CCRE-Main");
            if (className == null) {
                throw new RuntimeException("Could not find MANIFEST-specified launchee!");
            }
            return className;
        }
    }
}
