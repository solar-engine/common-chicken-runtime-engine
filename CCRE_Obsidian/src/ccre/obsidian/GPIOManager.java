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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The main class that handles interfacing with the BB GPIO pins.
 *
 * @author skeggsc
 */
public class GPIOManager {

    /**
     * The shared lock object for modifying GPIO states. This is acquired by all
     * GPIO primitives.
     */
    private static final Object lock = new Object();

    /**
     * A class representing a single GPIO line.
     */
    public static class GPIO implements BooleanOutput, BooleanInputPoll {

        /**
         * The ID of the GPIO.
         */
        public final int ID;
        /**
         * Whether or not the GPIO line is currently exported.
         */
        private boolean isExported;

        /**
         * Create a new GPIO with the specified line number.
         *
         * @param gpioID the GPIO ID.
         */
        private GPIO(int gpioID) {
            this.ID = gpioID;
        }

        /**
         * Export the GPIO line. This is needed before any other operations can
         * work with the line.
         *
         * @throws ObsidianHardwareException if the line is already exported or
         * an IO problem occurs.
         */
        public void export() throws ObsidianHardwareException {
            synchronized (lock) {
                if (isExported) {
                    throw new ObsidianHardwareException("GPIO " + ID + " already exported!");
                }
                try {
                    FileOutputStream fout = new FileOutputStream(exportNode);
                    try {
                        fout.write(Integer.toString(ID).getBytes());
                    } finally {
                        fout.close();
                    }
                } catch (IOException ex) {
                    throw new ObsidianHardwareException("Cannot access export node!", ex);
                }
                isExported = true;
            }
        }

        /**
         * Unexport the GPIO line. This cleans up after an export, and makes it
         * no longer able to be used.
         *
         * @throws ObsidianHardwareException if the line is not exported or an
         * IO problem occurs.
         */
        public void unexport() throws ObsidianHardwareException {
            synchronized (lock) {
                if (!isExported) {
                    throw new ObsidianHardwareException("Cannot unexport unless exported!");
                }
                try {
                    FileOutputStream fout = new FileOutputStream(unexportNode);
                    try {
                        fout.write(Integer.toString(ID).getBytes());
                    } finally {
                        fout.close();
                    }
                } catch (IOException ex) {
                    throw new ObsidianHardwareException("Cannot access unexport node!", ex);
                }
                isExported = false;
            }
        }

        /**
         * Returns whether or not the GPIO line is exported.
         *
         * @return the export status.
         */
        public boolean isExported() {
            return isExported;
        }

        /**
         * Configures the direction (digital input or output) of the GPIO line.
         *
         * @param isOutput if the GPIO should output instead of input.
         * @throws ObsidianHardwareException if the line is not exported or an
         * IO problem occurs.
         */
        public void setDirection(boolean isOutput) throws ObsidianHardwareException {
            synchronized (lock) {
                if (!isExported) {
                    throw new ObsidianHardwareException("Cannot set direction unless exported!");
                }
                byte[] toWrite = isOutput ? B_OUT : B_IN;
                try {
                    FileOutputStream fout = new FileOutputStream("/sys/class/gpio/gpio" + ID + "/direction");
                    try {
                        fout.write(toWrite);
                    } finally {
                        fout.close();
                    }
                } catch (IOException ex) {
                    throw new ObsidianHardwareException("Cannot access direction node!", ex);
                }
            }
        }

        @Override
        public void writeValue(boolean bln) throws ObsidianHardwareException {
            synchronized (lock) {
                if (!isExported) {
                    throw new ObsidianHardwareException("Cannot set value unless exported!");
                }
                char toWrite = bln ? '1' : '0';
                try {
                    FileOutputStream fout = new FileOutputStream("/sys/class/gpio/gpio" + ID + "/value");
                    try {
                        fout.write(toWrite);
                    } finally {
                        fout.close();
                    }
                } catch (IOException ex) {
                    throw new ObsidianHardwareException("Cannot access value node!", ex);
                }
            }
        }

        @Override
        public boolean readValue() {
            synchronized (lock) {
                int c;
                if (!isExported) {
                    throw new ObsidianHardwareException("Cannot get value unless exported!");
                }
                try {
                    FileInputStream fin = new FileInputStream("/sys/class/gpio/gpio" + ID + "/value");
                    try {
                        c = fin.read();
                    } finally {
                        fin.close();
                    }
                    return c != '0';
                } catch (IOException ex) {
                    throw new ObsidianHardwareException("Cannot access value node!", ex);
                }
            }
        }
    }
    /**
     * The array of GPIO lines, all null by default.
     */
    private static GPIO[] gpioes = new GPIO[128];
    /**
     * The path used to export GPIO lines.
     */
    private static final File exportNode = new File("/sys/class/gpio/export");
    /**
     * The path used to unexport GPIO lines.
     */
    private static final File unexportNode = new File("/sys/class/gpio/unexport");
    /**
     * The constant for output direction configuration.
     */
    private static final byte[] B_OUT = "out".getBytes();
    /**
     * The constant for input direction configuration.
     */
    private static final byte[] B_IN = "in".getBytes();

    /**
     * Convenience function to setup a GPIO channel.
     *
     * @param gpioID The GPIO channel number.
     * @param isOutput Should the GPIO line be set to output as opposed to
     * input.
     * @param defaultPullOrValue If an output, the default value. If an input,
     * the default pull status.
     * @return the GPIO channel.
     * @throws ObsidianHardwareException if an internal or IO problem occurs.
     */
    public static GPIO setupChannel(int gpioID, boolean isOutput, boolean defaultPullOrValue) throws ObsidianHardwareException {
        synchronized (lock) {
            GPIO g = getChannel(gpioID);
            g.export();
            g.setDirection(isOutput);
            g.writeValue(defaultPullOrValue);
            return g;
        }
    }

    /**
     * Get a simple reference to the specified GPIO channel. Allocates, but does
     * not export, the channel if it does not exist.
     *
     * @param gpioID The GPIO channel number.
     * @return the GPIO channel.
     */
    public static GPIO getChannel(int gpioID) {
        synchronized (lock) {
            if (gpioes[gpioID] != null) {
                return gpioes[gpioID];
            } else {
                return gpioes[gpioID] = new GPIO(gpioID);
            }
        }
    }

    /**
     * Unexports the specified GPIO channel.
     *
     * @param gpioID The GPIO channel number.
     */
    public static void destroyChannel(int gpioID) {
        synchronized (lock) {
            getChannel(gpioID).unexport();
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                synchronized (lock) {
                    for (GPIO g : gpioes) {
                        if (g.isExported()) {
                            g.unexport();
                        }
                    }
                }
            }
        });
    }
}
