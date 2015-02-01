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
package ccre.igneous;

import ccre.channel.BooleanOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.log.Logger;
import edu.wpi.first.wpilibj.CANJaguar;

/**
 * A CANJaguar ExtendedMotor interface for the cRIO.
 * 
 * @author skeggsc
 */
public class ExtendedJaguar extends ExtendedMotor implements FloatOutput {

    private final CANJaguar jaguar;
    private final int jaguarID;
    private Boolean enableMode = null; // null until something cares. This means that it's not enabled, but could be automatically.
    private boolean isBypassed = false;
    private long bypassUntil = 0;

    /**
     * Allocate a CANJaguar given the CAN bus ID.
     * 
     * @param deviceNumber the CAN bus ID.
     * @throws ExtendedMotorFailureException if the CAN Jaguar cannot be
     * allocated.
     */
    public ExtendedJaguar(int deviceNumber) throws ExtendedMotorFailureException {
        try {
            jaguar = new CANJaguar(deviceNumber);
            jaguar.changeControlMode(CANJaguar.ControlMode.kPercentVbus);
            jaguarID = deviceNumber;
        } catch (Exception e) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Create", e);
        }
    }

    public void set(float value) {
        if (isBypassed) {
            if (System.currentTimeMillis() > bypassUntil) {
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
            bypassUntil = System.currentTimeMillis() + 3000;
            Logger.warning("Motor control failed: CAN Jaguar " + jaguarID + ": bypassing for three seconds.", ex);
            try {
                disable();
            } catch (ExtendedMotorFailureException e) {
                Logger.warning("Could not bypass CAN Jaguar: " + jaguarID, e);
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
            jaguar.setX(value);
        } catch (Exception ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Set", ex);
        }
    }

    public void enable() throws ExtendedMotorFailureException {
        try {
            jaguar.enableControl();
        } catch (Exception ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Enable", ex);
        }
        enableMode = true;
    }

    public void disable() throws ExtendedMotorFailureException {
        try {
            jaguar.disableControl();
        } catch (Exception ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Disable", ex);
        }
        enableMode = false;
    }

    public BooleanOutput asEnable() {
        return new BooleanOutput() {
            public void set(boolean value) {
                if (enableMode == null || enableMode.booleanValue() != value) {
                    try {
                        if (value) {
                            enable();
                        } else {
                            disable();
                        }
                    } catch (ExtendedMotorFailureException ex) {
                        Logger.warning("Motor control failed: CAN Jaguar " + jaguarID, ex);
                    }
                }
            }
        };
    }

    public FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException {
        try {
            switch (mode) {
            case CURRENT_FIXED:
                jaguar.changeControlMode(CANJaguar.ControlMode.kCurrent);
                return this;
            case VOLTAGE_FIXED:
                jaguar.changeControlMode(CANJaguar.ControlMode.kVoltage);
                return this;
            case GENERIC_FRACTIONAL:
            case VOLTAGE_FRACTIONAL:
                jaguar.changeControlMode(CANJaguar.ControlMode.kVoltage);
                return this;
            default:
                return null;
            }
        } catch (Exception ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Set Mode", ex);
        }
    }

    public boolean hasInternalPID() {
        return true;
    }

    public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
        try {
            jaguar.setPID(P, I, D);
        } catch (Exception ex) {
            throw new ExtendedMotorFailureException("WPILib CANJaguar Failure: Set PID", ex);
        }
    }

    public FloatInputPoll asStatus(final StatusType type) {
        switch (type) {
        case BUS_VOLTAGE:
        case OUTPUT_CURRENT:
        case OUTPUT_VOLTAGE:
        case TEMPERATURE:
            return new FloatInputPoll() {
                private boolean zeroed = false;
                private long zeroUntil = 0;
                public float get() {
                    if (zeroed) {
                        if (System.currentTimeMillis() > zeroUntil) {
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
                    } catch (Exception ex) {
                        zeroed = true;
                        zeroUntil = System.currentTimeMillis() + 3000;
                        Logger.warning("WPILib CANJaguar Failure during status: temporarily zeroing value for three seconds.", ex);
                        return (float) 0.0;
                    }
                    throw new RuntimeException("Invalid internal asStatus setting: " + type); // should never happen as long as the lists match.
                }
            };
        default:
            return null;
        }
    }

    public Object getDiagnostics(ExtendedMotor.DiagnosticType type) {
        try {
            switch (type) {
            case CAN_JAGUAR_FAULTS:
            case GENERIC_FAULT_MASK:
                return (long) jaguar.getFaults();
            case BUS_VOLTAGE_FAULT:
                return (jaguar.getFaults() & CANJaguar.Faults.kBusVoltageFault.value) != 0;
            case CURRENT_FAULT:
                return (jaguar.getFaults() & CANJaguar.Faults.kCurrentFault.value) != 0;
            case GATE_DRIVER_FAULT:
                return (jaguar.getFaults() & CANJaguar.Faults.kGateDriverFault.value) != 0;
            case TEMPERATURE_FAULT:
                return (jaguar.getFaults() & CANJaguar.Faults.kTemperatureFault.value) != 0;
            case ANY_FAULT:
                return jaguar.getFaults() != 0;
            default:
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }
}
