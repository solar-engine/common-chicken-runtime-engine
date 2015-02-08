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
package ccre.igneous;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import javax.swing.JFrame;

import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPServer;
import ccre.log.BootLogger;
import ccre.log.FileLogger;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;

/**
 * The launcher for the DeviceList system.
 *
 * @author skeggsc
 */
public class DeviceListMain {

    /**
     * Start the emulator.
     *
     * @param args a single-element array containing only the path to the main
     * Jar file for the emulated program.
     * @throws IOException if the jar file cannot be properly accessed
     * @throws ClassNotFoundException if a reflection error occurs
     * @throws InstantiationException if a reflection error occurs
     * @throws NoSuchMethodException if a reflection error occurs
     * @throws IllegalAccessException if a reflection error occurs
     * @throws InvocationTargetException if a reflection error occurs
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (args.length != 2 || !("roboRIO".equals(args[1]) || "cRIO".equals(args[1]))) {
            System.err.println("Expected arguments: <Igneous-Jar> (roboRIO|cRIO)");
            System.exit(-1);
            return;
        }
        boolean isRoboRIO = "roboRIO".equals(args[1]);
        Logger.info("Setting up emulator for platform: " + (isRoboRIO ? "roboRIO" : "cRIO"));
        File jarFile = new File(args[0]);
        JarFile igneousJar = new JarFile(jarFile);
        String mainClass;
        try {
            mainClass = igneousJar.getManifest().getMainAttributes().getValue("Igneous-Main");
        } finally {
            igneousJar.close();
        }
        if (mainClass == null) {
            throw new RuntimeException("Could not find MANIFEST-specified launchee!");
        }
        @SuppressWarnings("resource")
        URLClassLoader classLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() }, DeviceListMain.class.getClassLoader());
        Class<? extends IgneousApplication> asSubclass = classLoader.loadClass(mainClass).asSubclass(IgneousApplication.class);
        final JFrame main = new JFrame("CCRE DeviceList-Based Emulator for " + (isRoboRIO ? "roboRIO" : "cRIO"));
        main.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        DeviceBasedLauncher launcher = new DeviceBasedLauncher(isRoboRIO);
        main.setContentPane(launcher.panel);
        main.setSize(1024, 768);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                main.setVisible(true);
            }
        });
        NetworkAutologger.register();
        BootLogger.register();
        FileLogger.register();
        IgneousLauncherHolder.setLauncher(launcher);
        Cluck.setupServer();
        new CluckTCPServer(Cluck.getNode(), 1540).start();
        try {
            Logger.info("Starting application: " + mainClass);
            asSubclass.getConstructor().newInstance().setupRobot();
            Logger.info("Hello, " + mainClass + "!");
            launcher.panel.start();
        } catch (Throwable thr) {
            launcher.panel.setErrorDisplay(thr);
        }
    }
}
