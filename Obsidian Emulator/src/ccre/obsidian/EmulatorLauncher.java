/*
 * Copyright 2013 Colby Skeggs
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

package ccre.obsidian;

import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.cluck.CluckGlobals;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.log.MultiTargetLogger;
import ccre.log.NetworkAutologger;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 *
 * @author Vincent Miller
 */
public class EmulatorLauncher extends ObsidianLauncher {

    /**
     * The settings loaded during the launch process.
     */
    public static Properties settings;

    /*
     private EmulatorWorld world;
     private double leftMotorSpeed = 0;
     private double rightMotorSpeed = 0;
     private final FloatOutput leftMotor;
     private final FloatOutput rightMotor;
     */
    EmulatorGUI guiWindow;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (args.length < 1) {
            System.err.println("Expected arguments: <Obsidian-Jar> [Run-GUI]");
            System.exit(-1);
            return;
        }
        CluckGlobals.ensureInitializedCore();
        NetworkAutologger.register();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()}, EmulatorLauncher.class.getClassLoader());

        boolean gui;
        if (args.length > 1) {
            gui = Boolean.parseBoolean(args[1]);
        } else {
            gui = false;
        }

        EmulatorLauncher l = new EmulatorLauncher(classLoader, gui);
        new EmulatorLauncher(classLoader, gui).main();
    }

    public EmulatorLauncher(ClassLoader coreClass, boolean gui) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(coreClass);
        /*
         rightMotor = new FloatOutput() {
         @Override
         public void writeValue(float value) {
         rightMotorSpeed = value;
         world.updateVelocity(leftMotorSpeed, rightMotorSpeed);
         }
         };
         leftMotor = new FloatOutput() {
         @Override
         public void writeValue(float value) {
         leftMotorSpeed = value;
         world.updateVelocity(leftMotorSpeed, rightMotorSpeed);
         }
         };
         world = new EmulatorWorld();
         prd.addListener(world);
         */
        Logger.log(LogLevel.INFO, "Launching GUI.");
        guiWindow = new EmulatorGUI();
        if (gui) {
            guiWindow.setVisible(true);
        }
    }

    @Override
    public BooleanOutput makeGPIOOutput(int chan, boolean defaultValue) {
        EmulatorPin pin = guiWindow.getPin(chan);
        pin.setMode(EmulatorPin.Mode.GPIO_OUT);
        pin.set(defaultValue);
        return new EmulatorGPIOOutput(pin);
    }

    @Override
    public BooleanInputPoll makeGPIOInput(int chan, boolean pullSetting) {
        EmulatorPin pin = guiWindow.getPin(chan);
        pin.setMode(EmulatorPin.Mode.GPIO_IN);
        pin.set(pullSetting);
        return new EmulatorGPIOInput(pin);
    }

    @Override
    public FloatOutput makePWMOutput(PWMPin chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity) {
        int pinChan = Integer.parseInt(chan.name().substring(3));
        if (chan.name().substring(0, 2).equals("P9")) {
            pinChan += 46;
        }
        EmulatorPin pin = guiWindow.getPin(pinChan);
        pin.setMode(EmulatorPin.Mode.PWM);
        pin.set(defaultValue);
        return new EmulatorPWMOutput(pin);
    }

    @Override
    public void destroyPWMOutput(PWMPin chan) {
        Logger.log(LogLevel.SEVERE, "nope.");
    }

    @Override
    public FloatInputPoll makeAnalogInput(int chan) {
        EmulatorPin pin = guiWindow.getPin(chan);
        pin.setMode(EmulatorPin.Mode.ANALOG_IN);
        pin.set(0.0f);
        return new EmulatorAnalogInput(pin);
    }
}
