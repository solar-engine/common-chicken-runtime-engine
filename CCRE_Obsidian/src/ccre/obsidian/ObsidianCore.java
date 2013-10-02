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
import ccre.chan.FloatOutput;
import ccre.event.EventSource;
import java.util.Properties;

/**
 * A Core class for Obsidian. Extend this in order to write an application to
 * run on the BeagleBone Black.
 *
 * @author skeggsc
 */
public abstract class ObsidianCore implements GPIOChannels {

    /**
     * Produced about every twenty milliseconds. This timing is subject to
     * change.
     */
    protected EventSource periodic;
    /**
     * The properties loaded automatically for Obsidian.
     */
    protected Properties properties;

    /**
     * Implement this method - it should set up everything that your robot needs
     * to do.
     */
    protected abstract void createRobotControl();

    /**
     * Open the specified GPIO channel for output.
     *
     * @param chan The channel ID to open. See GPIOChannels.
     * @param defaultValue the initial value for the output.
     * @return the BooleanOutput representing the GPIO channel.
     * @see ccre.obsidian.GPIOChannels
     */
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
    public FloatOutput makePWMOutput(String chan, float defaultValue, final float calibrateLow, final float calibrateHigh, float frequency, boolean zeroPolarity) throws ObsidianHardwareException {
        final FloatOutput raw = PWMManager.createPWMOutput(chan, ((defaultValue - calibrateLow) / (calibrateHigh - calibrateLow)), frequency, zeroPolarity);
        return new FloatOutput() {
            @Override
            public void writeValue(float f) {
                float a = ((f - calibrateLow) / (calibrateHigh - calibrateLow));
                if (a < 0) {
                    a = 0;
                } else if (a > 1) {
                    a = 1;
                }
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
    public void destroyPWMOutput(String chan) throws ObsidianHardwareException {
        PWMManager.destroyChannel(chan);
    }
}
