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

import ccre.chan.FloatOutput;
import ccre.concurrency.ReporterThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * The main class that handles interfacing with the BB PWM pins.
 *
 * @author skeggsc
 */
public class PWMManager {

    /**
     * The path to the directory for the PWMs.
     */
    public static final File pwmDirectory;
    /**
     * The shared lock object for modifying PWM states. This is acquired by all
     * PWM primitives.
     */
    private static final Object lock = new Object();

    /**
     * A class representing a single PWM line.
     */
    public static class PWM implements FloatOutput {

        /**
         * The key for the PWM channel.
         */
        public final String key;
        /**
         * The current period of the PWM in nanoseconds.
         */
        private long period_ns;
        /**
         * The open file for controlling the period.
         */
        private final FileOutputStream period;
        /**
         * The open file for controlling the duty.
         */
        private final FileOutputStream duty;
        /**
         * The open file for controlling the polarity.
         */
        private final FileOutputStream polarity;
        /**
         * Has this channel been destoyed/closed?
         */
        private boolean destroyed = false;

        /**
         * Creates the PWM channel for the specified key, with the specified
         * open files for period, duty, and polarity.
         *
         * @param key The key of the PWM channel.
         * @param period The file for the period of the channel.
         * @param duty The file for the duty of the channel.
         * @param polarity The file for the polarity of the channel.
         */
        private PWM(String key, FileOutputStream period, FileOutputStream duty, FileOutputStream polarity) {
            this.key = key;
            this.period = period;
            this.duty = duty;
            this.polarity = polarity;
        }

        /**
         * Set the frequency for the PWM.
         *
         * @param frequency The frequency for the PWM - this is converted to a
         * period in nanoseconds.
         * @throws ObsidianHardwareException
         */
        public void setFrequency(float frequency) throws ObsidianHardwareException {
            synchronized (lock) {
                if (destroyed) {
                    throw new ObsidianHardwareException("PWM channel destroyed!");
                }
                long nanos = (long) (1e9 / frequency);
                if (nanos != period_ns) {
                    try {
                        String s = String.valueOf(nanos);
                        Logger.fine("Setting period: " + s);
                        period.write(s.getBytes());
                    } catch (IOException ex) {
                        throw new ObsidianHardwareException("Cannot set frequency of PWM!", ex);
                    }
                    period_ns = nanos;
                }
            }
        }

        /**
         * Set the polarity for the PWM.
         *
         * @param zeroPolarity If this is true, then the polarity is set to
         * zero. Otherwise the polarity is set to one.
         * @throws ObsidianHardwareException
         */
        public void setPolarity(boolean zeroPolarity) throws ObsidianHardwareException {
            synchronized (lock) {
                if (destroyed) {
                    throw new ObsidianHardwareException("PWM channel destroyed!");
                }
                try {
                    polarity.write(zeroPolarity ? '0' : '1');
                } catch (IOException ex) {
                    throw new ObsidianHardwareException("Cannot set polarity of PWM!", ex);
                }
            }
        }

        @Override
        public void writeValue(float f) throws ObsidianHardwareException {
            synchronized (lock) {
                if (destroyed) {
                    throw new ObsidianHardwareException("PWM channel destroyed!");
                }
                if (f < 0 || f > 1) {
                    throw new ObsidianHardwareException("Invalid Duty!");
                }
                try {
                    duty.write(String.valueOf((long) (period_ns * f)).getBytes());
                } catch (IOException ex) {
                    throw new ObsidianHardwareException("Cannot set frequency of PWM!", ex);
                }
            }
        }

        /**
         * Destroy the PWM channel. It will not be able to be used afterwards.
         *
         * @throws ObsidianHardwareException
         */
        public void destroy() throws ObsidianHardwareException {
            synchronized (lock) {
                Logger.info("Destroying: " + this.key);
                if (destroyed) {
                    return;
                }
                ObsidianHardwareException out = null;
                try {
                    DeviceTree.unloadCapeManager("bone_pwm_" + key);
                } catch (IOException ex) {
                    out = new ObsidianHardwareException("Cannot unload PWM!", ex);
                }
                try {
                    period.close();
                } catch (IOException ex) {
                    if (out != null) {
                        out.addSuppressed(ex);
                    } else {
                        out = new ObsidianHardwareException("Cannot unload PWM!", ex);
                    }
                }
                try {
                    duty.close();
                } catch (IOException ex) {
                    if (out != null) {
                        out.addSuppressed(ex);
                    } else {
                        out = new ObsidianHardwareException("Cannot unload PWM!", ex);
                    }
                }
                try {
                    polarity.close();
                } catch (IOException ex) {
                    if (out != null) {
                        out.addSuppressed(ex);
                    } else {
                        out = new ObsidianHardwareException("Cannot unload PWM!", ex);
                    }
                }
                if (out != null) {
                    throw out;
                }
                pwms.remove(key);
                destroyed = true;
            }
        }
    }
    /**
     * The map of PWM lines.
     */
    private static HashMap<String, PWM> pwms = new HashMap<String, PWM>();

    /**
     * Open up a PWM output to the specified channel.
     *
     * @param key The channel name.
     * @param def The default value in the range 0 to 1.
     * @param frequency The frequency to start the PWM at.
     * @param zeroPolarity The polarity setting for the PWM.
     * @return the output that writes to this PWM using the range 0 to 1.
     * @throws ObsidianHardwareException
     */
    public static FloatOutput createPWMOutput(String key, float def, float frequency, boolean zeroPolarity) throws ObsidianHardwareException {
        synchronized (lock) {
            if (pwms.containsKey(key)) {
                throw new ObsidianHardwareException("PWM channel already acquired!");
            }
            try {
                DeviceTree.loadCapeManager("bone_pwm_" + key);
                File tpath = DeviceTree.autocompletePath(pwmDirectory, "pwm_test_" + key);
                FileOutputStream period = null, duty = null, polarity = null;
                period = new FileOutputStream(new File(tpath, "period"));
                duty = new FileOutputStream(new File(tpath, "duty"));
                polarity = new FileOutputStream(new File(tpath, "polarity"));
                PWM p = new PWM(key, period, duty, polarity);
                pwms.put(key, p);
                p.setPolarity(zeroPolarity);
                p.setFrequency(frequency);
                p.writeValue(def);
                return p;
            } catch (IOException ex) {
                throw new ObsidianHardwareException("Cannot load PWM: " + key, ex);
            }
        }
    }

    /**
     * Destroy the specified channel. It will no longer be able to be used, and
     * will throw errors if this is attempted. You can then later reopen the
     * channel as if it had never been opened.
     *
     * @param key the key of the PWM channel to open
     * @throws ObsidianHardwareException
     */
    public static void destroyChannel(String key) throws ObsidianHardwareException {
        synchronized (lock) {
            PWM p = pwms.get(key);
            if (p == null) {
                throw new ObsidianHardwareException("PWM not loaded: " + key);
            }
            p.destroy();
        }
    }

    static {
        File ocpdir = null;
        try {
            // Init PWM
            DeviceTree.loadCapeManager("am33xx_pwm");
            ocpdir = DeviceTree.autocompletePath(new File("/sys/devices"), "ocp");
        } catch (IOException ex) {
            throw new RuntimeException("Cannot load PWM Cape Manager!", ex);
        }
        pwmDirectory = ocpdir;
        Runtime.getRuntime().addShutdownHook(new ReporterThread("Cleanup-PWMS") {
            @Override
            protected void threadBody() {
                synchronized (lock) {
                    if (!pwms.isEmpty()) {
                        PWM[] ps = pwms.values().toArray(new PWM[pwms.size()]);
                        for (PWM p : ps) {
                            p.destroy();
                        }
                    }
                    if (!pwms.isEmpty()) {
                        RuntimeException ex = new ObsidianHardwareException("Could not clean up all PWMS! [a]");
                        System.err.println("Could not clean up all PWMS! [b]");
                        Logger.log(LogLevel.SEVERE, "Could not clean up all PWMS! [c]", ex);
                        throw ex;
                    }
                }
            }
        });
    }
}
