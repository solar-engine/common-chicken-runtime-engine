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

import ccre.chan.*;
import ccre.cluck.CluckGlobals;
import ccre.event.Event;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple abstract class for a launcher which gives access to all functions of
 * the BeagleBone Black and Obsidian.
 *
 * @author MillerV
 */
public abstract class ObsidianLauncher {
    
    protected String hubPath;

    /**
     * The settings loaded during the launch process.
     */
    public static Properties settings;
    /**
     * Periodically is fired by the main loop to update the user program.
     */
    protected final Event periodic;
    /**
     * The core obsidian program.
     */
    public final ObsidianCore core;

    /**
     * Create a new obsidian launcher,
     *
     * @param loader The loader used to find the properties file.
     * @param hubPath The name of the link to the control hub (empty when using TCP).
     * @throws IOException If an issue occurs when trying to access the
     * properties file.
     * @throws ClassNotFoundException If an issue occurs while loading the main
     * program class.
     * @throws InstantiationException If an issue occurs while loading the main
     * program class.
     * @throws IllegalAccessException If an issue occurs while loading the main
     * program class.
     */
    public ObsidianLauncher(ClassLoader loader, String hubPath) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.hubPath = hubPath;
        CluckGlobals.ensureInitializedCore();
        NetworkAutologger.register();
        Properties p = new Properties();
        InputStream inst = loader.getResourceAsStream("obsidian-conf.properties");
        if (inst == null) {
            throw new IOException("Could not find configuration file!");
        }
        p.load(inst);
        settings = p;
        String name = p.getProperty("Obsidian-Main");
        if (name == null) {
            throw new IOException("Could not find configuration-specified launchee!");
        }
        CluckGlobals.setupServer();
        core = (ObsidianCore) loader.loadClass(name).newInstance();
        core.properties = p;
        core.launcher = this;
        periodic = new Event();
        core.periodic = periodic;
        core.enabled = new Event();
        core.disabled = new Event();
    }

    /**
     * Set up the robot
     */
    protected void main() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    periodic.produce();
                } catch (Throwable thr) {
                    Logger.log(LogLevel.SEVERE, "Exception caught in execution loop - robots don't quit!", thr);
                }
            }
        }, 10, 20);
        try {
            core.createRobotControl();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Exception caught at top level during initialization - robots don't quit!", thr);
        }
    }

    /**
     * Retrieves the specified joystick axis from the network, returning a
     * FloatInputProducer that will update every ~20 milliseconds with the value
     * of the axis.
     *
     * @param axis The index of the axis to retrieve, from 1 to 4.
     * @return A FloatInputProducer that provides the value of that axis.
     */
    public FloatStatus getJoystickAxis(int axis) {
        //return CluckGlobals.node.subscribeFIP("hub/joystick" + 1 + "-axis" + axis);
        FloatStatus stick = new FloatStatus();
        CluckGlobals.node.publish("joystick1-axis" + axis, (FloatOutput)stick);
        return stick;
    }

    /**
     * Retrieves the specified joystick button from the network, returning a
     * BooleanInputProducer that will update every ~20 milliseconds with the
     * value of the button.
     *
     * @param index The index of the button to retrieve, from 1 to 12.
     * @return A BooleanInputProducer that provides the value of that button.
     */
    public BooleanStatus getJoystickButton(int index) {
        //return CluckGlobals.node.subscribeBIP("hub/joystick" + 1 + "-button" + button);
        BooleanStatus button = new BooleanStatus();
        CluckGlobals.node.publish("joystick1-button" + button, (BooleanOutput)button);
        return button;
    }

    /**
     * Open the specified GPIO channel for output.
     *
     * @param chan The channel ID to open. See GPIOChannels.
     * @param defaultValue the initial value for the output.
     * @return the BooleanOutput representing the GPIO channel.
     * @see ccre.obsidian.GPIOChannels
     */
    public abstract BooleanOutput makeGPIOOutput(int chan, boolean defaultValue);

    /**
     * Open the specified GPIO channel for input with the specified pull state.
     *
     * @param chan The channel ID to open. See GPIOChannels.
     * @param pullSetting the setting for the pull resistors.
     * @return the BooleanInputPoll representing the GPIO channel.
     */
    public abstract BooleanInputPoll makeGPIOInput(int chan, boolean pullSetting);

    /**
     * Open the specified PWM channel for output with the specified default
     * value, calibration, frequency, and polarity.
     *
     * @param chan The channel name for the PWM.
     * @param defaultValue The default value (in the range calibrateLow ...
     * calibrateHigh)
     * @param calibrateN1 The low end of the calibration. Becomes 0% duty.
     * @param calibrateN2 The high end of the calibration. Becomes 100% duty.
     * @param frequency The frequency to write.
     * @param zeroPolarity Should the polarity be zero? Otherwise one.
     * @return the output that writes to the PWM.
     * @throws ObsidianHardwareException
     */
    public abstract FloatOutput makePWMOutput(PWMPin chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity);

    /**
     * Close the specified PWM channel. The channel will throw errors if
     * accessed once this is called. You can then later reopen the channel as if
     * it had never been opened.
     *
     * @param chan The channel to close.
     * @throws ObsidianHardwareException
     */
    public abstract void destroyPWMOutput(PWMPin chan);

    /**
     * Open the specified analog channel for input.
     *
     * @param chan The channel number for the analog input.
     * @return a FloatInputPoll that represents the current uncalibrated value
     * of the analog input, from 0.0 to 1.0.
     * @throws ObsidianHardwareException
     */
    public abstract FloatInputPoll makeAnalogInput(int chan);
;
}
