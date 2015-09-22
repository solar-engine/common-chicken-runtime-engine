/*
 * Copyright 2015 Colby Skeggs
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
package ccre.drivers.chrobotics;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanStatus;
import ccre.channel.DerivedFloatInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.SerialIO;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.concurrency.ReporterThread;

/**
 * The high-level interface to the UM7-LT orientation sensor from CH Robotics,
 * via RS232.
 *
 * http://www.chrobotics.com/shop/um7-lt-orientation-sensor
 *
 * @author skeggsc
 */
public class UM7LT {
    private static Logger logger = LoggerFactory.getLogger(UM7LT.class);
    
    private final InternalUM7LT internal;

    private final EventStatus eulerUpdateStatus = new EventStatus();
    /**
     * An event that fires whenever new data for the Euler angles becomes
     * available.
     */
    public final EventInput onEulerUpdate = eulerUpdateStatus;
    private final EventStatus healthUpdateStatus = new EventStatus();
    /**
     * An event that fires whenever new data for the Health sensor becomes
     * available.
     */
    public final EventInput onHealthUpdate = healthUpdateStatus;
    /**
     * Whether or not faults should be automatically logged.
     */
    public final BooleanStatus autoreportFaults = new BooleanStatus(true);

    private final CollapsingWorkerThread worker = new CollapsingWorkerThread("UM7LT-dispatcher") {

        private int lastEuler = 0;
        private int lastHealth = 0;

        @Override
        protected void doWork() throws Throwable {
            int newEuler = internal.dregs[InternalUM7LT.DREG_EULER_TIME - InternalUM7LT.DREG_BASE];
            if (lastEuler != newEuler) {
                lastEuler = newEuler;
                eulerUpdateStatus.produce();
            }
            int newHealth = getHealth();
            if (lastHealth != newHealth) {
                lastHealth = newHealth;
                int changed = newHealth ^ lastHealth;
                if (autoreportFaults.get()) {
                    if ((changed & 0x100) != 0) {
                        logger.debug("UM7LT COMM: " + ((newHealth & 0x100) != 0 ? "FAULT" : "nominal"));
                    }
                    if ((changed & 0x22) != 0) {
                        logger.debug("UM7LT MAGN:" + ((newHealth & 0x2) != 0 ? " FAULT" : (newHealth & 0x20) != 0 ? " DISTORTED" : "nominal"));
                    }
                    if ((changed & 0x18) != 0) {
                        logger.debug("UM7LT ACCL:" + ((newHealth & 0x8) != 0 ? " FAULT" : (newHealth & 0x10) != 0 ? " DISTORTED" : "nominal"));
                    }
                    if ((changed & 0x4) != 0) {
                        logger.debug("UM7LT GYRO: " + ((newHealth & 0x4) != 0 ? " FAULT" : "nominal"));
                    }
                }
                healthUpdateStatus.produce();
            }
        }
    };

    /**
     * Connect to the UM7LT on the specified port. Make sure to call start()!
     *
     * @param rs232 the rs232 port to talk over.
     */
    public UM7LT(SerialIO rs232) {
        internal = new InternalUM7LT(rs232, worker);

        roll = new DerivedFloatInput(onEulerUpdate) {
            @Override
            protected float apply() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_CONVERSION_DIVISOR;
            }
        };

        pitch = new DerivedFloatInput(onEulerUpdate) {
            @Override
            protected float apply() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA - InternalUM7LT.DREG_BASE])) / InternalUM7LT.EULER_CONVERSION_DIVISOR;
            }
        };

        yaw = new DerivedFloatInput(onEulerUpdate) {
            @Override
            protected float apply() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PSI - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_CONVERSION_DIVISOR;
            }
        };

        // These return numbers in degrees per second.
        rollRate = new DerivedFloatInput(onEulerUpdate) {
            @Override
            protected float apply() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA_DOT - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_RATE_CONVERSION_DIVISOR;
            }
        };

        pitchRate = new DerivedFloatInput(onEulerUpdate) {
            @Override
            protected float apply() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA_DOT - InternalUM7LT.DREG_BASE])) / InternalUM7LT.EULER_RATE_CONVERSION_DIVISOR;
            }
        };

        yawRate = new DerivedFloatInput(onEulerUpdate) {
            @Override
            protected float apply() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PSI_DOT - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_RATE_CONVERSION_DIVISOR;
            }
        };

        // This returns a time in seconds. (?)
        eulerTime = new DerivedFloatInput(onEulerUpdate) {
            @Override
            protected float apply() {
                return Float.intBitsToFloat(internal.dregs[InternalUM7LT.DREG_EULER_TIME - InternalUM7LT.DREG_BASE]);
            }
        };
    }

    private boolean started = false;

    /**
     * Start the main loop to handle tho UM7LT.
     */
    public void start() {
        synchronized (this) {
            if (started) {
                throw new IllegalStateException("Already started!");
            }
            started = true;
        }
        new ReporterThread("UM7LT-io") {
            @Override
            protected void threadBody() throws InterruptedException {
                while (true) {
                    try {
                        internal.dumpSerialData();
                        internal.doReadOperation((byte) 0xAA);
                        while (true) {
                            internal.writeSettings(0, 5, 0, 0, 1);
                            internal.handleRS232Input(100);
                        }
                    } catch (Exception ex) {
                        logger.error("UM7LT thread failed. Resetting after ten seconds...", ex);
                        Thread.sleep(10000);
                    }
                }
            }
        }.start();
    }

    /**
     * Start zeroing the Gyro.
     */
    public final EventOutput zeroGyro = new EventOutput() {
        public void event() {
            try {
                zeroGyro();
            } catch (IOException e) {
                logger.warn("Could not initiate gyro zeroing", e);
            }
        }
    };

    /**
     * Start zeroing the Gyro.
     *
     * @throws IOException if the command could not be sent.
     */
    public void zeroGyro() throws IOException {
        internal.zeroGyros();
    }

    private int getHealth() {
        return internal.dregs[InternalUM7LT.DREG_HEALTH - InternalUM7LT.DREG_BASE];
    }

    /**
     * Check if the sensor is currently experiencing any faults.
     *
     * @return if the sensor is reporting any faults.
     */
    public boolean hasFault() {
        return (getHealth() & 0x13E) != 0;
    }

    /**
     * A structure of the faults that the UM7 can have. Just some data.
     *
     * @see UM7LT#getFaults(Faults)
     *
     * @author skeggsc
     */
    public static final class Faults {
        /**
         * If the communications line got overloaded.
         */
        public boolean comm_overflow;
        /**
         * If the magnetometer is experiencing distortion.
         */
        public boolean magnetometer_distorted;
        /**
         * If the accelerometer is experiencing distortion.
         */
        public boolean accelerometer_distorted;
        /**
         * If the accelerometer has failed.
         */
        public boolean accelerometer_failed;
        /**
         * If the gyro has failed.
         */
        public boolean gyro_failed;
        /**
         * If the magnetometer has failed.
         */
        public boolean magnetometer_failed;
    }

    /**
     * Fetches the current statuses of faults and fills a fault structure with
     * the status.
     *
     * @param faults the fault structure to update.
     */
    public void getFaults(UM7LT.Faults faults) {
        int reg = getHealth();
        faults.comm_overflow = (reg & 0x100) != 0;
        faults.magnetometer_distorted = (reg & 0x20) != 0;
        faults.accelerometer_distorted = (reg & 0x10) != 0;
        faults.accelerometer_failed = (reg & 0x8) != 0;
        faults.gyro_failed = (reg & 0x4) != 0;
        faults.magnetometer_failed = (reg & 0x2) != 0;
    }

    /**
     * The current roll rotation, in degrees.
     */
    public final FloatInput roll;
    /**
     * The current pitch rotation, in degrees.
     */
    public final FloatInput pitch;
    /**
     * The current yaw rotation, in degrees.
     */
    public final FloatInput yaw;

    /**
     * The current roll rate, in degrees per second.
     */
    public final FloatInput rollRate;
    /**
     * The current pitch rate, in degrees per second.
     */
    public final FloatInput pitchRate;
    /**
     * The current yaw rate, in degrees per second.
     */
    public final FloatInput yawRate;

    /**
     * The current time reported by the UM7LT for the measurement of the Euler
     * rotations.
     */
    public final FloatInput eulerTime;
}
