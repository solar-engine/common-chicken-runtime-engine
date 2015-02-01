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
package ccre.igneous.direct;

import ccre.channel.BooleanOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.log.Logger;

/**
 * A CANTalon ExtendedMotor interface for the roboRIO.
 * 
 * @author skeggsc
 */
public class ExtendedTalonDirect extends ExtendedMotor implements FloatOutput {

    private final CANTalonMod talon;
    private Boolean enableMode = null; // null until something cares. This means that it's not enabled, but could be automatically.
    private boolean isBypassed = false;
    private long bypassUntil = 0;

    /**
     * Allocate a CANTalon given the CAN bus ID.
     * 
     * @param deviceNumber the CAN bus ID.
     * @throws ExtendedMotorFailureException if the CAN Talon cannot be
     * allocated.
     */
    public ExtendedTalonDirect(int deviceNumber) throws ExtendedMotorFailureException {
        try {
            talon = new CANTalonMod(deviceNumber);
            talon.changeControlMode(CANTalonMod.ControlMode.PercentVbus);
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANTalon Failure: Create", ex);
        }
    }

    @Override
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
            Logger.warning("Motor control failed: CAN Talon " + talon.m_deviceNumber + ": bypassing for three seconds.", ex);
            try {
                disable();
            } catch (ExtendedMotorFailureException e) {
                Logger.warning("Could not bypass CAN Talon: " + talon.m_deviceNumber, e);
            }
            enableMode = null; // automatically re-enableable.
        }
    }

    public void setUnsafe(float value) throws ExtendedMotorFailureException {
        if (enableMode == null) {
            enable();
        }
        try {
            talon.set(value);
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANTalon Failure: Set", ex);
        }
    }

    @Override
    public void enable() throws ExtendedMotorFailureException {
        try {
            talon.enableControl();
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANTalon Failure: Enable", ex);
        }
        enableMode = true;
    }

    @Override
    public void disable() throws ExtendedMotorFailureException {
        try {
            talon.disableControl();
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANTalon Failure: Disable", ex);
        }
        enableMode = false;
    }

    @Override
    public BooleanOutput asEnable() throws ExtendedMotorFailureException {
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
                        Logger.warning("Motor control failed: CAN Talon " + talon.m_deviceNumber, ex);
                    }
                }
            }
        };
    }

    public FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException {
        try {
            switch (mode) {
            case CURRENT_FIXED:
                talon.changeControlMode(CANTalonMod.ControlMode.Current);
                return this;
            case VOLTAGE_FIXED:
                talon.changeControlMode(CANTalonMod.ControlMode.Voltage);
                return this;
            case GENERIC_FRACTIONAL:
            case VOLTAGE_FRACTIONAL:
                talon.changeControlMode(CANTalonMod.ControlMode.PercentVbus);
                return this;
                // TODO: Support more modes.
            default:
                return null;
            }
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANTalon Failure: Set Mode", ex);
        }
    }

    @Override
    public boolean hasInternalPID() {
        return true;
    }

    @Override
    public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
        try {
            talon.setPID(P, I, D);
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANTalon Failure: Set PID", ex);
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
                            return (float) talon.getBusVoltage();
                        case OUTPUT_VOLTAGE:
                            return (float) talon.getOutputVoltage();
                        case OUTPUT_CURRENT:
                            return (float) talon.getOutputCurrent();
                        case TEMPERATURE:
                            return (float) talon.getTemp();
                            // TODO: Provide the rest of the options.
                        }
                    } catch (RuntimeException ex) {
                        zeroed = true;
                        zeroUntil = System.currentTimeMillis() + 3000;
                        Logger.warning("WPILib CANTalon Failure during status: temporarily zeroing value for three seconds.", ex);
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
            case ANY_FAULT:
                return talon.getFaultHardwareFailure() != 0 || talon.getFaultOverTemp() != 0 || talon.getFaultUnderVoltage() != 0 || isBypassed;
            case BUS_VOLTAGE_FAULT:
                return talon.getFaultUnderVoltage() != 0;
            case TEMPERATURE_FAULT:
                return talon.getFaultOverTemp() != 0;
            case COMMS_FAULT:
                return isBypassed;
            default:
                return null;
            }
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
