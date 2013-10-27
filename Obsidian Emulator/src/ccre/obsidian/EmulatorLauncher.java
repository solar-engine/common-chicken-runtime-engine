/*
 * Copyright 2013 Colby Skeggs and Vincent Miller
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
    private EmulatorWorld world;
    private double leftMotorSpeed = 0;
    private double rightMotorSpeed = 0;
    private static final String leftMotorPin = "P8_13";
    private static final String rightMotorPin = "P9_14";
    private final FloatOutput leftMotor;
    private final FloatOutput rightMotor;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (args.length != 1) {
            System.err.println("Expected arguments: <Obsidian-Jar>");
            System.exit(-1);
            return;
        }
        File jarFile = new File(args[0]);
        CluckGlobals.ensureInitializedCore();
        Logger.target = new MultiTargetLogger(new LoggingTarget[]{Logger.target, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger")});
        URL u = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{u}, EmulatorLauncher.class.getClassLoader());

        EmulatorLauncher l = new EmulatorLauncher(classLoader);
    }

    public EmulatorLauncher(ClassLoader coreClass) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(coreClass);
        rightMotor = new FloatOutput() {
            @Override
            public void writeValue(float value) {
                Logger.log(LogLevel.INFO, "Right motor: " + value);
                rightMotorSpeed = value;
                world.updateVelocity(leftMotorSpeed, rightMotorSpeed);
            }
        };
        leftMotor = new FloatOutput() {
            @Override
            public void writeValue(float value) {
                Logger.log(LogLevel.INFO, "Left motor: " + value);
                leftMotorSpeed = value;
                world.updateVelocity(leftMotorSpeed, rightMotorSpeed);
            }
        };
        world = new EmulatorWorld();
        prd.addListener(world);
        run();
    }

    @Override
    public BooleanOutput makeGPIOOutput(int chan, boolean defaultValue) {
        throw new UnsupportedOperationException("GPIO not supported in emulator.");
    }

    @Override
    public BooleanInputPoll makeGPIOInput(int chan, boolean pullSetting) {
        throw new UnsupportedOperationException("GPIO not supported in emulator.");
    }

    @Override
    public FloatOutput makePWMOutput(String chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity) {
        switch (chan) {
            case leftMotorPin:
                return leftMotor;
            case rightMotorPin:
                return rightMotor;
            default:
                throw new IllegalArgumentException("The PWM output you selected is not connected to this emulator.");
        }
    }

    @Override
    public void destroyPWMOutput(String chan) {
        Logger.log(LogLevel.SEVERE, "nope.");
    }

    @Override
    public FloatInputPoll makeAnalogInput(int chan) {
        throw new UnsupportedOperationException("Analog not supported in emulator.");
    }
}
