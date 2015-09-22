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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.slf4j.LoggerFactory;

import ccre.channel.EventStatus;
import ccre.cluck.Cluck;
import ccre.log.FileLogger;
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
     * @throws InterruptedException if the main thread is somehow interrupted.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Expected arguments: <FRC-Jar>");
            System.exit(-1);
            return;
        }
        File jarFile = new File(args[0]);
        JarFile frcJar = new JarFile(jarFile);
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
        EventStatus onInit = new EventStatus();
        DeviceBasedImplementation impl = new DeviceBasedImplementation(onInit);
        main.setContentPane(impl.panel);
        main.setSize(1024, 768);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                main.setVisible(true);
            }
        });
        NetworkAutologger.register();
        FileLogger.register();
        FRCImplementationHolder.setImplementation(impl);
        if (!System.getProperty("os.name").toLowerCase().contains("linux") && !System.getProperty("os.name").toLowerCase().contains("mac os")) {
            // Don't try to bind to port 80 on Mac or Linux - only sadness will ensue.
            Cluck.setupServer();
        }
        Cluck.setupServer(1540);
        Thread.sleep(500);// give a bit of time for network stuff to try to set itself up.
        try {
            impl.clearLoggingPane();
            LoggerFactory.getLogger(DeviceListMain.class).info("Starting application: " + mainClass);
            asSubclass.getConstructor().newInstance().setupRobot();
            onInit.event();
            LoggerFactory.getLogger(DeviceListMain.class).info("Hello, " + mainClass + "!");
            impl.panel.start();
        } catch (Throwable thr) {
            LoggerFactory.getLogger(DeviceListMain.class).warn("Init failed", thr);
            impl.panel.setErrorDisplay(thr);
        }
    }
}
