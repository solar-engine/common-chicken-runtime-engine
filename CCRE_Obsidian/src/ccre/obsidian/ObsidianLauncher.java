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
import ccre.log.LoggingTarget;
import ccre.log.MultiTargetLogger;
import static ccre.obsidian.ObsidianLauncherImpl.settings;
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

    protected final Timer t;

    public ObsidianLauncher() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        this(null);
    }

    public ObsidianLauncher(ObsidianCore core) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        CluckGlobals.ensureInitializedCore();
        Logger.target = new MultiTargetLogger(new LoggingTarget[]{Logger.target, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger")});
        if (core == null) {
            Properties p = new Properties();
            InputStream inst = ObsidianLauncherImpl.class.getResourceAsStream("/obsidian-conf.properties");
            if (inst == null) {
                throw new IOException("Could not find configuration file!");
            }
            p.load(inst);
            settings = p;
            String name = p.getProperty("Obsidian-Main");
            if (name == null) {
                throw new IOException("Could not find configuration-specified launchee!");
            }

            core = (ObsidianCore) Class.forName(name).newInstance();
            
            core.properties = p;
        }
        core.launcher = this;
        CluckGlobals.initializeServer(80);
        final Event prd = new Event();
        core.periodic = prd;
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    prd.produce();
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
     * FloatInputProducer that will update ever ~20 milliseconds with the value
     * of the axis.
     *
     * @param axis The index of the axis to retrieve, from 1 to 4.
     * @return A FloatInputProducer that provides the value of that axis.
     */
    public FloatInputProducer getJoystickAxis(int axis) {
        return CluckGlobals.encoder.subscribeFloatInputProducer("joystick-axis" + axis, 0.0F);
    }

    /**
     * Retrieves the specified joystick button from the network, returning a
     * BooleanInputProducer that will update ever ~20 milliseconds with the
     * value of the button.
     *
     * @param button The index of the button to retrieve, from 1 to 12.
     * @return A BooleanInputProducer that provides the value of that button.
     */
    public BooleanInputProducer getJoystickButton(int button) {
        return CluckGlobals.encoder.subscribeBooleanInputProducer("joystick-button" + button, false);
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
     * @param calibrateLow The low end of the calibration. Becomes 0% duty.
     * @param calibrateHigh The high end of the calibration. Becomes 100% duty.
     * @param frequency The frequency to write.
     * @param zeroPolarity Should the polarity be zero? Otherwise one.
     * @return the output that writes to the PWM.
     * @throws ObsidianHardwareException
     */
    public abstract FloatOutput makePWMOutput(String chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity);

    /**
     * Close the specified PWM channel. The channel will throw errors if
     * accessed once this is called. You can then later reopen the channel as if
     * it had never been opened.
     *
     * @param chan The channel to close.
     * @throws ObsidianHardwareException
     */
    public abstract void destroyPWMOutput(String chan);

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
