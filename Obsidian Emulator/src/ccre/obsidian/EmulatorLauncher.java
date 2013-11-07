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
    private EmulatorWorld world;
    private double leftMotorSpeed = 0;
    private double rightMotorSpeed = 0;
    private final FloatOutput leftMotor;
    private final FloatOutput rightMotor;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (args.length < 1) {
            System.err.println("Expected arguments: <Obsidian-Jar> [Run-GUI]");
            System.exit(-1);
            return;
        }
        CluckGlobals.ensureInitializedCore();
        NetworkAutologger.register();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()}, EmulatorLauncher.class.getClassLoader());

        boolean gui = args.length < 2 ? true : Boolean.parseBoolean(args[1]);
        
        new EmulatorLauncher(classLoader, gui).main();
    }

    public EmulatorLauncher(ClassLoader coreClass, boolean gui) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        super(coreClass);
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
    public FloatOutput makePWMOutput(PWMPin chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity) {
        switch (chan) {
            case P8_13:
                return leftMotor;
            case P9_14:
                return rightMotor;
            default:
                throw new IllegalArgumentException("The PWM output you selected is not connected to this emulator.");
        }
    }

    @Override
    public void destroyPWMOutput(PWMPin chan) {
        Logger.log(LogLevel.SEVERE, "nope.");
    }

    @Override
    public FloatInputPoll makeAnalogInput(int chan) {
        throw new UnsupportedOperationException("Analog not supported in emulator.");
    }
}
