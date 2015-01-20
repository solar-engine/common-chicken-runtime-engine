package ccre.drivers.chrobotics;

import java.io.IOException;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.SerialIO;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.concurrency.ReporterThread;
import ccre.ctrl.FloatMixing;
import ccre.log.Logger;

/**
 * The high-level interface to the UM7-LT orientation sensor from CH Robotics,
 * via RS232.
 * 
 * http://www.chrobotics.com/shop/um7-lt-orientation-sensor
 * 
 * @author skeggsc
 */
public class UM7LT {
    private final InternalUM7LT internal;

    private final EventStatus eulerUpdateStatus = new EventStatus();
    public final EventInput onEulerUpdate = eulerUpdateStatus;
    private final EventStatus healthUpdateStatus = new EventStatus();
    public final EventInput onHealthUpdate = healthUpdateStatus;
    public final BooleanStatus autoreportFaults = new BooleanStatus();

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
                        Logger.fine("UM7LT COMM: " + ((newHealth & 0x100) != 0 ? "FAULT" : "nominal"));
                    }
                    if ((changed & 0x22) != 0) {
                        Logger.fine("UM7LT MAGN:" + ((newHealth & 0x2) != 0 ? " FAULT" : (newHealth & 0x20) != 0 ? " DISTORTED" : "nominal"));
                    }
                    if ((changed & 0x18) != 0) {
                        Logger.fine("UM7LT ACCL:" + ((newHealth & 0x8) != 0 ? " FAULT" : (newHealth & 0x10) != 0 ? " DISTORTED" : "nominal"));
                    }
                    if ((changed & 0x4) != 0) {
                        Logger.fine("UM7LT GYRO: " + ((newHealth & 0x4) != 0 ? " FAULT" : "nominal"));
                    }
                }
                healthUpdateStatus.produce();
            }
        }
    };

    public UM7LT(SerialIO rs232) {
        internal = new InternalUM7LT(rs232, worker);

        roll = FloatMixing.createDispatch(new FloatInputPoll() {
            public float get() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_CONVERSION_DIVISOR;
            }
        }, onEulerUpdate);

        pitch = FloatMixing.createDispatch(new FloatInputPoll() {
            public float get() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA - InternalUM7LT.DREG_BASE])) / InternalUM7LT.EULER_CONVERSION_DIVISOR;
            }
        }, onEulerUpdate);

        yaw = FloatMixing.createDispatch(new FloatInputPoll() {
            public float get() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PSI - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_CONVERSION_DIVISOR;
            }
        }, onEulerUpdate);

        // These return numbers in degrees per second.
        rollRate = FloatMixing.createDispatch(new FloatInputPoll() {
            public float get() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA_DOT - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_RATE_CONVERSION_DIVISOR;
            }
        }, onEulerUpdate);

        pitchRate = FloatMixing.createDispatch(new FloatInputPoll() {
            public float get() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PHI_THETA_DOT - InternalUM7LT.DREG_BASE])) / InternalUM7LT.EULER_RATE_CONVERSION_DIVISOR;
            }
        }, onEulerUpdate);

        yawRate = FloatMixing.createDispatch(new FloatInputPoll() {
            public float get() {
                return ((short) (internal.dregs[InternalUM7LT.DREG_EULER_PSI_DOT - InternalUM7LT.DREG_BASE] >> 16)) / InternalUM7LT.EULER_RATE_CONVERSION_DIVISOR;
            }
        }, onEulerUpdate);

        // This returns a time in seconds. (?)
        eulerTime = FloatMixing.createDispatch(new FloatInputPoll() {
            public float get() {
                return Float.intBitsToFloat(internal.dregs[InternalUM7LT.DREG_EULER_TIME - InternalUM7LT.DREG_BASE]);
            }
        }, onEulerUpdate);
    }

    private boolean started = false;

    public void start() {
        synchronized (this) {
            if (started) {
                throw new IllegalStateException("Already started!");
            }
            started = true;
        }
        new ReporterThread("UM7LT-io") {
            @Override
            protected void threadBody() throws IOException {
                internal.dumpSerialData();
                internal.doReadOperation((byte) 0xAA);
                while (true) {
                    internal.writeSettings(0, 5, 0, 0, 1);
                    internal.handleRS232Input(100);
                }
            }
        }.start();
    }

    private final BooleanStatus zeroingStatus = new BooleanStatus();
    public final BooleanInput isZeroing = zeroingStatus;

    public final EventOutput zeroGyro = new EventOutput() {
        public void event() {
            try {
                zeroGyro(false);
            } catch (IOException e) {
                Logger.warning("Could not initiate gyro zeroing", e);
            }
        }
    };

    public void zeroGyro() throws IOException {
        zeroingStatus.set(true);
        internal.zeroGyros();
    }

    private int getHealth() {
        return internal.dregs[InternalUM7LT.DREG_HEALTH - InternalUM7LT.DREG_BASE];
    }

    public boolean hasFault() {
        return (getHealth() & 0x13E) != 0;
    }

    public static final class Faults {
        public boolean comm_overflow, magnetometer_distorted,
                accelerometer_distorted, accelerometer_failed, gyro_failed,
                magnetometer_failed;
    }

    public void getFaults(UM7LT.Faults faults) {
        int reg = getHealth();
        faults.comm_overflow = (reg & 0x100) != 0;
        faults.magnetometer_distorted = (reg & 0x20) != 0;
        faults.accelerometer_distorted = (reg & 0x10) != 0;
        faults.accelerometer_failed = (reg & 0x8) != 0;
        faults.gyro_failed = (reg & 0x4) != 0;
        faults.magnetometer_failed = (reg & 0x2) != 0;
    }

    // These return numbers in degrees.
    public final FloatInput roll, pitch, yaw;

    // These return numbers in degrees per second.
    public final FloatInput rollRate, pitchRate, yawRate;

    // This returns a time in seconds. (?)
    public final FloatInput eulerTime;
}
