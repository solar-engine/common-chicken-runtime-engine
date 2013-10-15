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

import ccre.obsidian.comms.Radio;
import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.cluck.CluckGlobals;
import ccre.event.Event;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.log.MultiTargetLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Obsidian launcher. This is the class that is ran by the Java virtual
 * machine.
 *
 * @author skeggsc
 */
public class ObsidianLauncherImpl implements ObsidianLauncher {

    /**
     * The settings loaded during the launch process.
     */
    public static Properties settings;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean watch = false;
        if (args.length != 0) {
            if (!args[0].equals("use-watcher")) {
                ccre.launcher.Launcher.main(args);
                return;
            }
            watch = true;
        }
        CluckGlobals.ensureInitializedCore();
        Logger.target = new MultiTargetLogger(new LoggingTarget[]{Logger.target, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger")});
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
        ObsidianCore core = (ObsidianCore) Class.forName(name).newInstance();
        core.properties = p;
        core.launcher = new ObsidianLauncherImpl();
        CluckGlobals.initializeServer(80);
        final Event prd = new Event();
        core.periodic = prd;
        final Timer t = new Timer();
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
        if (watch) {
            final File watchee = new File("remote-watcher");
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (watchee.exists()) {
                        watchee.delete();
                        Logger.info("Shutting down due to watcher notification.");
                        System.exit(0);
                    }
                }
            }, 500, 1000);
        }
        try {
            core.createRobotControl();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Exception caught at top level during initialization - robots don't quit!", thr);
        }
    }
    
    /**
     * Open the specified GPIO channel for output.
     *
     * @param chan The channel ID to open. See GPIOChannels.
     * @param defaultValue the initial value for the output.
     * @return the BooleanOutput representing the GPIO channel.
     * @see ccre.obsidian.GPIOChannels
     */
    @Override
    public BooleanOutput makeGPIOOutput(int chan, boolean defaultValue) {
        return GPIOManager.setupChannel(chan, true, defaultValue);
    }

    /**
     * Open the specified GPIO channel for input with the specified pull state.
     *
     * @param chan The channel ID to open. See GPIOChannels.
     * @param pullSetting the setting for the pull resistors.
     * @return the BooleanInputPoll representing the GPIO channel.
     */
    @Override
    public BooleanInputPoll makeGPIOInput(int chan, boolean pullSetting) {
        return GPIOManager.setupChannel(chan, false, pullSetting);
    }

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
    @Override
    public FloatOutput makePWMOutput(String chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity) throws ObsidianHardwareException {
        if (defaultValue < -1) {
            defaultValue = -1;
        } else if (defaultValue > 1) {
            defaultValue = 1;
        }
        final FloatOutput raw = PWMManager.createPWMOutput(chan, ((defaultValue + 1) / 2) * (calibrateN2 - calibrateN1) + calibrateN1, frequency, zeroPolarity);
        return new FloatOutput() {
            @Override
            public void writeValue(float f) {
                if (f < -1) {
                    f = -1;
                } else if (f > 1) {
                    f = 1;
                }
                float a = ((f + 1) / 2) * (calibrateN2 - calibrateN1) + calibrateN1;
                raw.writeValue(a);
            }
        };
    }

    /**
     * Close the specified PWM channel. The channel will throw errors if
     * accessed once this is called. You can then later reopen the channel as if
     * it had never been opened.
     *
     * @param chan The channel to close.
     * @throws ObsidianHardwareException
     */
    @Override
    public void destroyPWMOutput(String chan) throws ObsidianHardwareException {
        PWMManager.destroyChannel(chan);
    }

    /**
     * Open the specified analog channel for input.
     *
     * @param chan The channel number for the analog input.
     * @return a FloatInputPoll that represents the current uncalibrated value
     * of the analog input, from 0.0 to 1.0.
     * @throws ObsidianHardwareException
     */
    @Override
    public FloatInputPoll makeAnalogInput(int chan) throws ObsidianHardwareException {
        return ADCManager.getChannel(chan);
    }
}
