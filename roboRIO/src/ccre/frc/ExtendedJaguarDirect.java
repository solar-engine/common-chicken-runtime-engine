/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.frc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanOutput;
import ccre.channel.DerivedFloatInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.time.Time;

/**
 * A CANJaguar ExtendedMotor interface for the roboRIO.
 *
 * @author skeggsc
 */
public class ExtendedJaguarDirect extends ExtendedMotor implements FloatOutput {
    private static Logger logger = LoggerFactory.getLogger(ExtendedJaguarDirect.class);

    private final CANJaguarMod jaguar;
    private Boolean enableMode = null; // null until something cares. This means that it's not enabled, but could be automatically.
    private boolean isBypassed = false;
    private long bypassUntil = 0;

    /**
     * Allocate a CANJaguar given the CAN bus ID.
     *
     * @param deviceNumber the CAN bus ID.
     * @throws ExtendedMotorFailureException if the CAN Jaguar cannot be
     * allocated.
     * @throws InterruptedException if the thread was interrupted
     */
    public ExtendedJaguarDirect(int deviceNumber) throws ExtendedMotorFailureException, InterruptedException {
        try {
            jaguar = new CANJaguarMod(deviceNumber);
            jaguar.setPercentMode();
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Create", ex);
        }
    }

    @Override
    public void set(float value) {
        if (isBypassed) {
            if (Time.currentTimeMillis() > bypassUntil) {
                isBypassed = false;
            } else {
                return;
            }
        } else if (enableMode != null && !enableMode) {
            return;
        }
        try {
            setUnsafe(value);
        } catch (ExtendedMotorFailureException ex) {
            isBypassed = true;
            bypassUntil = Time.currentTimeMillis() + 3000;
            logger.warn("Motor control failed: CAN Jaguar " + jaguar.m_deviceNumber + ": bypassing for three seconds.", ex);
            try {
                disable();
            } catch (ExtendedMotorFailureException e) {
                logger.warn("Could not bypass CAN Jaguar: " + jaguar.m_deviceNumber, e);
            }
            enableMode = null; // automatically re-enableable.
        }
    }

    /**
     * The same as set, but throws an error on failure instead of temporarily
     * bypassing the motor.
     *
     * @param value the value to set to.
     * @throws ExtendedMotorFailureException if the value cannot be set.
     */
    public void setUnsafe(float value) throws ExtendedMotorFailureException {
        if (enableMode == null) {
            enable();
        }
        try {
            jaguar.set(value);
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Set", ex);
        }
    }

    @Override
    public void enable() throws ExtendedMotorFailureException {
        isBypassed = false;
        try {
            jaguar.enableControl();
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Enable", ex);
        }
        enableMode = true;
    }

    @Override
    public void disable() throws ExtendedMotorFailureException {
        isBypassed = false;
        try {
            jaguar.disableControl();
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Disable", ex);
        }
        enableMode = false;
    }

    @Override
    public BooleanOutput asEnable() {
        return new BooleanOutput() {
            @Override
            public void set(boolean value) {
                if (enableMode == null || enableMode.booleanValue() != value) {
                    try {
                        if (value) {
                            enable();
                        } else {
                            disable();
                        }
                    } catch (ExtendedMotorFailureException ex) {
                        logger.warn("Motor control failed: CAN Jaguar " + jaguar.m_deviceNumber, ex);
                    }
                }
            }
        };
    }

    @Override
    public FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException {
        try {
            switch (mode) {
            case CURRENT_FIXED:
                jaguar.setCurrentMode(1, 0, 0);
                return this;
            case VOLTAGE_FIXED:
                jaguar.setVoltageMode();
                return this;
            case GENERIC_FRACTIONAL:
            case VOLTAGE_FRACTIONAL:
                jaguar.setPercentMode();
                return this;
            default:
                return null;
            }
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Set Mode", ex);
        }
    }

    @Override
    public boolean hasInternalPID() {
        return true;
    }

    @Override
    public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
        try {
            jaguar.setPID(P, I, D);
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Set PID", ex);
        }
    }

    @Override
    public FloatInput asStatus(final StatusType type, EventInput updateOn) {
        switch (type) {
        case BUS_VOLTAGE:
        case OUTPUT_CURRENT:
        case OUTPUT_VOLTAGE:
        case TEMPERATURE:
            return new DerivedFloatInput(updateOn) {
                private boolean zeroed = false;
                private long zeroUntil = 0;

                @Override
                protected float apply() {
                    if (zeroed) {
                        if (Time.currentTimeMillis() > zeroUntil) {
                            zeroed = false;
                        } else {
                            return (float) 0.0;
                        }
                    }
                    try {
                        switch (type) {
                        case BUS_VOLTAGE:
                            return (float) jaguar.getBusVoltage();
                        case OUTPUT_VOLTAGE:
                            return (float) jaguar.getOutputVoltage();
                        case OUTPUT_CURRENT:
                            return (float) jaguar.getOutputCurrent();
                        case TEMPERATURE:
                            return (float) jaguar.getTemperature();
                        }
                    } catch (RuntimeException ex) {
                        zeroed = true;
                        zeroUntil = Time.currentTimeMillis() + 3000;
                        logger.warn("WPILib CANJaguar Failure during status: temporarily zeroing value for three seconds.", ex);
                        return (float) 0.0;
                    }
                    throw new RuntimeException("Invalid internal asStatus setting: " + type); // should never happen as long as the lists match.
                }
            };
        default:
            return null;
        }
    }

    @Override
    public Object getDiagnostics(ExtendedMotor.DiagnosticType type) {
        try {
            switch (type) {
            case CAN_JAGUAR_FAULTS:
            case GENERIC_FAULT_MASK:
                return (long) jaguar.getFaults();
            case BUS_VOLTAGE_FAULT:
                return (jaguar.getFaults() & CANJaguarMod.kBusVoltageFault) != 0;
            case CURRENT_FAULT:
                return (jaguar.getFaults() & CANJaguarMod.kCurrentFault) != 0;
            case GATE_DRIVER_FAULT:
                return (jaguar.getFaults() & CANJaguarMod.kGateDriverFault) != 0;
            case TEMPERATURE_FAULT:
                return (jaguar.getFaults() & CANJaguarMod.kTemperatureFault) != 0;
            case COMMS_FAULT:
                return isBypassed;
            case ANY_FAULT:
                return jaguar.getFaults() != 0 || isBypassed;
            default:
                return null;
            }
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
