/*
 * Copyright 2014-2015 Colby Skeggs.
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
package ccre.frc;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import javax.swing.JFrame;

import ccre.channel.EventCell;
import ccre.cluck.Cluck;
import ccre.log.FileLogger;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;
import ccre.storage.Storage;

/**
 * The launcher for the DeviceList system.
 *
 * @author skeggsc
 */
public class DeviceListMain {

    /**
     * Start the emulator.
     *
     * @param jarFile the program Jar.
     * @param storageDir the storage directory for logs, etc.
     * @throws Exception
     */
    public static void startEmulator(File jarFile, File storageDir) throws Exception {
        if (!jarFile.isFile()) {
            throw new IllegalArgumentException("File is either not a file or does not exist: " + jarFile);
        }
        JarFile frcJar = new JarFile(jarFile);
        Storage.setBaseDir(storageDir);
        String mainClass;
        try {
            mainClass = frcJar.getManifest().getMainAttributes().getValue("CCRE-Main");
        } finally {
            frcJar.close();
        }
        if (mainClass == null) {
            throw new RuntimeException("Could not find MANIFEST-specified launchee!");
        }
        @SuppressWarnings("resource")
        URLClassLoader classLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, DeviceListMain.class.getClassLoader());
        Class<? extends FRCApplication> asSubclass = classLoader.loadClass(mainClass).asSubclass(FRCApplication.class);
        final JFrame main = new JFrame("CCRE DeviceList-Based Emulator for roboRIO");
        main.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        EventCell onInit = new EventCell();
        DeviceBasedImplementation impl = new DeviceBasedImplementation(onInit);
        main.setContentPane(impl.panel);
        main.setSize(1024, 768);
        java.awt.EventQueue.invokeLater(() -> main.setVisible(true));
        NetworkAutologger.register();
        FileLogger.register();
        FRCImplementationHolder.setImplementation(impl);
        if (!System.getProperty("os.name").toLowerCase().contains("linux") && !System.getProperty("os.name").toLowerCase().contains("mac os")) {
            // Don't try to bind port 80 on Mac/Linux. Only sadness will ensue.
            Cluck.setupServer();
        }
        Cluck.setupServer(1540);
        // give a bit of time for network stuff to try to set itself up.
        Thread.sleep(500);
        try {
            impl.clearLoggingPane();
            Logger.info("Starting application: " + mainClass);
            asSubclass.getConstructor().newInstance().setupRobot();
            onInit.event();
            Logger.info("Hello, " + mainClass + "!");
            impl.panel.start();
        } catch (Throwable thr) {
            Logger.warning("Init failed", thr);
            impl.panel.setErrorDisplay(thr);
        }
    }
}
