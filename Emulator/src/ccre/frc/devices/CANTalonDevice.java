/*
 * Copyright 2014-2016 Cel Skeggs
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
package ccre.frc.devices;

import java.util.HashMap;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.CancelOutput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOperation;
import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.ctrl.Faultable;
import ccre.drivers.ctre.talon.TalonAnalog;
import ccre.drivers.ctre.talon.TalonEncoder;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.drivers.ctre.talon.TalonFeedback;
import ccre.drivers.ctre.talon.TalonGeneralConfig;
import ccre.drivers.ctre.talon.TalonHardLimits;
import ccre.drivers.ctre.talon.TalonPIDConfiguration;
import ccre.drivers.ctre.talon.TalonPulseWidth;
import ccre.drivers.ctre.talon.TalonSoftLimits;
import ccre.frc.Device;
import ccre.frc.DeviceListPanel;
import ccre.log.Logger;

/**
 * A virtual CANTalon, which will contain any statuses, diagnostics, or outputs
 * requested by the application.
 *
 * @author skeggsc
 */
public class CANTalonDevice extends KeyedSubDevice {

    private int id;
    private FloatOutput mainSensorOutV;
    private final FloatOutput mainSensorOut = (x) -> mainSensorOutV.set(x);
    private final FloatCell mainSensorIn = new FloatCell();
    private final FloatCell mainSensorVel = new FloatCell();
    private final FloatIO mainSensor = FloatIO.compose(mainSensorIn, mainSensorOut);
    private CancelOutput cancelLastSensor = CancelOutput.nothing;
    private final EventCell beforeChangeMode = new EventCell();

    private void changeMainSensor(String name, FloatIO io, FloatInput vel) {
        Logger.info("Changed main sensor on " + id + " to " + name);
        mainSensorOutV = io;
        cancelLastSensor.cancel();
        cancelLastSensor = CancelOutput.nothing;
        cancelLastSensor = io.send(mainSensorIn);
        cancelLastSensor = cancelLastSensor.combine(vel.send(mainSensorVel));
    }

    private final class VirtualTalon extends TalonExtendedMotor {
        public void enable() throws ExtendedMotorFailureException {
            asEnable().set(true);
        }

        public void disable() throws ExtendedMotorFailureException {
            asEnable().set(false);
        }

        public BooleanIO asEnable() {
            return getBooleanOutput("ENABLED").cell(false);
        }

        public FloatOutput asMode(OutputControlMode mode) {
            return getFloatOutput("Mode " + mode);
        }

        public FloatInput asStatus(StatusType type, EventInput updateOn) {
            return getFloatInput("Status " + type);
        }

        public Object getDiagnostics(DiagnosticType type) {
            switch (type) {
            case BUS_VOLTAGE_FAULT:
            case ANY_FAULT:
            case TEMPERATURE_FAULT:
                return getDiagnosticChannel(type).get();
            default:
                return null;
            }
        }

        public BooleanInput getDiagnosticChannel(DiagnosticType type) {
            if (type.isBooleanDiagnostic) {
                return getBooleanInput(type.name());
            } else {
                return null;
            }
        }

        public boolean hasInternalPID() {
            return true;
        }

        public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
            this.modPID().getP().set(P);
            this.modPID().getI().set(I);
            this.modPID().getD().set(D);
            this.modPID().getF().set(0);
        }

        @Override
        public Faultable<Faults> modFaults() {
            return new Faultable<Faults>() {
                private HashMap<Faults, BooleanIO> sticky = new HashMap<>();

                @Override
                public Faults[] getPossibleFaults() {
                    return Faults.values();
                }

                @Override
                public BooleanInput getIsFaulting(Faults fault) {
                    return getBooleanInput("Fault " + fault.name());
                }

                @Override
                public BooleanInput getIsStickyFaulting(Faults fault) {
                    if (!sticky.containsKey(fault)) {
                        sticky.put(fault, getBooleanOutput("Sticky Fault " + fault.name()).cell(false));
                    }
                    BooleanIO stick = sticky.get(fault);
                    stick.setTrueWhen(getIsFaulting(fault).onPress());
                    return stick;
                }

                @Override
                public EventOutput getClearStickyFaults() {
                    return () -> {
                        for (BooleanIO bio : sticky.values()) {
                            bio.set(false);
                        }
                    };
                }
            };
        }

        @Override
        public TalonAnalog modAnalog() {
            return new TalonAnalog() {
                @Override
                public void configureAnalogUpdateRate(int millis) {
                    Logger.info("CAN Talon SRX " + id + ": Analog Frame Rate " + millis);
                }

                @Override
                public void useAnalogEncoder() {
                    changeMainSensor("Analog Encoder", this.getAnalogPositionEncoder(), this.getAnalogVelocity());
                }

                @Override
                public void useAnalogPotentiometer() {
                    changeMainSensor("Analog Potentiometer", this.getAnalogPosition(), this.getAnalogVelocity());
                }

                @Override
                public FloatIO getAnalogPosition() {
                    return FloatIO.compose(FloatOperation.modulation.of(this.getAnalogPositionEncoder().asInput(), 1.0f), this.getAnalogPositionEncoder());
                }

                @Override
                public FloatIO getAnalogPositionEncoder() {
                    return getFloatInputSpinner("Analog Input");
                }

                private FloatInput enc;

                @Override
                public FloatInput getAnalogVelocity() {
                    if (enc == null) {
                        enc = this.getAnalogPositionEncoder().derivative(100);
                    }
                    return enc;
                }

                @Override
                public void configurePotentiometerTurns(float rotations) {
                    Logger.info("CAN Talon SRX " + id + ": Potentiometer Turns " + rotations);
                }
            };
        }

        @Override
        public TalonEncoder modEncoder() {
            return new TalonEncoder() {
                @Override
                public void configureEncoderUpdateRate(int millis) {
                    Logger.info("CAN Talon SRX " + id + ": Encoder Frame Rate " + millis);
                }

                @Override
                public void useEncoder() {
                    changeMainSensor("Encoder", this.getEncoderPosition(), this.getEncoderVelocity());
                }

                @Override
                public void useRisingEdge() {
                    changeMainSensor("Rising Edge", this.getEncoderPosition(), this.getEncoderVelocity());
                }

                @Override
                public void useFallingEdge() {
                    changeMainSensor("Falling Edge", this.getEncoderPosition(), this.getEncoderVelocity());
                }

                @Override
                public FloatIO getEncoderPosition() {
                    return getFloatInputSpinner("Encoder");
                }

                private FloatInput enc;

                @Override
                public FloatInput getEncoderVelocity() {
                    if (enc == null) {
                        enc = this.getEncoderPosition().derivative(100);
                    }
                    return enc;
                }

                @Override
                public void configureEncoderCodesPerRev(float perRev) {
                    Logger.info("CAN Talon SRX " + id + ": Encoder Codes per Revolution: " + perRev);
                }

                private FloatIO quadIndexRises;

                @Override // only an IO here
                public FloatIO getNumberOfQuadIndexRises() {
                    if (quadIndexRises == null) {
                        EventInput inc = getQuadIndexPin().onPress();
                        quadIndexRises = getFloatInput("Encoder Index Pin Rises");
                        quadIndexRises.accumulateWhen(inc, 1);
                    }
                    return quadIndexRises;
                }

                @Override
                public BooleanInput getQuadAPin() {
                    return getBooleanInput("Encoder A Pin");
                }

                @Override
                public BooleanInput getQuadBPin() {
                    return getBooleanInput("Encoder B Pin");
                }

                @Override
                public BooleanInput getQuadIndexPin() {
                    return getBooleanInput("Encoder Index Pin");
                }
            };
        }

        @Override
        public TalonHardLimits modHardLimits() {
            return new TalonHardLimits() {
                @Override
                public void configureLimitSwitches(boolean forwardEnable, boolean forwardNC, boolean reverseEnable, boolean reverseNC) {
                    Logger.info("CAN Talon SRX " + id + ": Limit configuration: Forward: " + (forwardEnable ? "ENABLE " : "DISABLE ") + (reverseNC ? "NORM CLOSED" : "NORM OPEN") + " Reverse: " + (reverseEnable ? "ENABLE " : "DISABLE ") + (reverseNC ? "NORM CLOSED" : "NORM OPEN"));
                }

                @Override
                public BooleanInput getIsForwardLimitSwitchClosed() {
                    return getBooleanInput("Forward Hard Limit Switch");
                }

                @Override
                public BooleanInput getIsReverseLimitSwitchClosed() {
                    return getBooleanInput("Reverse Hard Limit Switch");
                }
            };
        }

        @Override
        public TalonPIDConfiguration modPID() {
            return new TalonPIDConfiguration() {
                @Override
                public FloatIO getP() {
                    return getFloatOutput("PID Proportional Term").cell(0);
                }

                @Override
                public FloatIO getI() {
                    return getFloatOutput("PID Integral Term").cell(0);
                }

                @Override
                public FloatIO getD() {
                    return getFloatOutput("PID Derivative Term").cell(0);
                }

                @Override
                public FloatIO getF() {
                    return getFloatOutput("PID Feedforward Term").cell(0);
                }

                @Override
                public FloatIO getIntegralBounds() {
                    return getFloatOutput("PID Integral Boundaries").cell(0);
                }

                @Override
                public BooleanIO getIsSecondaryProfileActive() {
                    FloatIO[] vs = new FloatIO[] { getP(), getI(), getD(), getF(), getIntegralBounds(), getCloseLoopRampRate() };
                    return new BooleanOutput() {
                        private boolean lastProfile;
                        private final float[] v = new float[vs.length];

                        @Override
                        public synchronized void set(boolean profile) {
                            if (profile == lastProfile) {
                                return;
                            }
                            lastProfile = profile;
                            for (int i = 0; i < vs.length; i++) {
                                float vc = v[i];
                                v[i] = vs[i].get();
                                vs[i].set(vc);
                            }
                        }
                    }.cell(false);
                }

                @Override
                public FloatIO getCloseLoopRampRate() {
                    return getFloatOutput("PID Close Loop Ramp Rate").cell(0);
                }

                @Override
                public FloatIO getIAccum() {
                    return getFloatOutput("PID Integer Accumulator").cell(0);
                }

                @Override
                public void configureRampRates(float throttle, float voltageCompensation) {
                    Logger.info("CAN Talon SRX " + id + ": Ramp rate configuration: Throttle: " + throttle + " Voltage Comp: " + voltageCompensation);
                }
            };
        }

        @Override
        public TalonPulseWidth modPulseWidth() {
            return new TalonPulseWidth() {
                @Override
                public void configurePulseWidthUpdateRate(int millis) {
                    Logger.info("CAN Talon SRX " + id + ": Pulse Width Frame Rate " + millis);
                }

                @Override
                public void usePulseWidth() {
                    changeMainSensor("Pulse Width", this.getPulseWidthPosition(), this.getPulseWidthVelocity());
                }

                // TODO: is anything else required here? I'm not sure this is
                // correct.
                @Override
                public void useRelativeCtreMagEncoder() {
                    changeMainSensor("CTRE Mag Encoder Relative", this.getPulseWidthPosition(), this.getPulseWidthVelocity());
                }

                @Override
                public void useAbsoluteCtreMagEncoder() {
                    changeMainSensor("CTRE Mag Encoder Absolute", this.getPulseWidthPosition(), this.getPulseWidthVelocity());
                }

                @Override
                public FloatIO getPulseWidthPosition() {
                    FloatIO width = getFloatInputSpinner("Pulse Width");
                    return FloatIO.compose(width.dividedBy(100), FloatOperation.multiplication.of(width.asOutput(), 100));
                }

                private FloatInput enc;

                @Override
                public FloatInput getPulseWidthVelocity() {
                    if (enc == null) {
                        enc = this.getPulseWidthPosition().derivative(100);
                    }
                    return enc;
                }

                @Override
                public FloatInput getPulseWidthRiseToFallMicroseconds() {
                    FloatInput withinTurn = FloatOperation.modulation.of(this.getPulseWidthPosition().asInput(), 1);
                    return this.getPulseWidthRiseToRiseMicroseconds().multipliedBy(withinTurn);
                }

                @Override
                public FloatInput getPulseWidthRiseToRiseMicroseconds() {
                    return getFloatInput("Pulse Width RiseToRise");
                }

                @Override
                public BooleanInput getPulseWidthOrCtreMagEncoderPresent() {
                    return getPulseWidthRiseToRiseMicroseconds().outsideRange(0, 0);
                }
            };
        }

        @Override
        public TalonSoftLimits modSoftLimits() {
            return new TalonSoftLimits() {
                private FloatCell forwardSoftLimit, reverseSoftLimit;
                private BooleanCell forwardEnabled, reverseEnabled;

                @Override
                public FloatIO getForwardSoftLimit() {
                    if (forwardSoftLimit == null) {
                        forwardSoftLimit = new FloatCell(getFloatOutput("Forward Soft Limit"));
                    }
                    return forwardSoftLimit;
                }

                @Override
                public BooleanIO getEnableForwardSoftLimit() {
                    if (forwardEnabled == null) {
                        forwardEnabled = new BooleanCell(getBooleanOutput("Forward Soft Limit Enabled"));
                    }
                    return forwardEnabled;
                }

                @Override
                public FloatIO getReverseSoftLimit() {
                    if (reverseSoftLimit == null) {
                        reverseSoftLimit = new FloatCell(getFloatOutput("Reverse Soft Limit"));
                    }
                    return reverseSoftLimit;
                }

                @Override
                public BooleanIO getEnableReverseSoftLimit() {
                    if (reverseEnabled == null) {
                        reverseEnabled = new BooleanCell(getBooleanOutput("Reverse Soft Limit Enabled"));
                    }
                    return reverseEnabled;
                }
            };
        }

        @Override
        public TalonFeedback modFeedback() {
            return new TalonFeedback() {
                @Override
                public FloatInput getBusVoltage() {
                    FloatIO fio = getFloatInput("Bus Voltage");
                    fio.set(12f);
                    return fio;
                }

                @Override
                public FloatInput getOutputVoltage() {
                    // TODO: simulate this
                    return getFloatInput("Output Voltage");
                }

                @Override
                public FloatInput getOutputCurrent() {
                    return getFloatInput("Output Current");
                }

                @Override
                public FloatIO getSensorPosition() {
                    return mainSensor;
                }

                @Override
                public FloatInput getSensorVelocity() {
                    return mainSensorVel;
                }

                @Override
                public FloatInput getThrottle() {
                    return getOutputVoltage().dividedBy(getBusVoltage());
                }

                @Override
                public FloatInput getClosedLoopError() {
                    // TODO: simulate this
                    return getFloatInput("Closed Loop Error");
                }

                @Override
                public FloatInput getTemperature() {
                    return getFloatInput("Temperature (Celsius)");
                }

                @Override
                public long GetFirmwareVersion() {
                    return 512;
                }
            };
        }

        @Override
        public TalonGeneralConfig modGeneralConfig() {
            return new TalonGeneralConfig() {
                private boolean reg = false;

                @Override
                public void activateFollowerMode(int talonID) {
                    if (!reg) {
                        reg = true;
                        beforeChangeMode.send(getBooleanOutput("Following " + talonID).eventSet(false));
                    }
                    beforeChangeMode.event();
                    getBooleanOutput("Following " + talonID).set(true);
                }

                @Override
                public void configureReversed(boolean flipSensor, boolean flipOutput) {
                    getBooleanOutput("Reverse Sensor").set(flipSensor);
                    getBooleanOutput("Reverse Output").set(flipOutput);
                }

                @Override
                public void configureAllowableClosedLoopError(float allowableCloseLoopError) {
                    getFloatOutput("Allowable Closed Loop Error").set(allowableCloseLoopError);
                }

                @Override
                public void configureGeneralFeedbackUpdateRate(int millisGeneral, int millisFeedback) {
                    Logger.info("CAN Talon SRX " + id + ": General Update Rate " + millisGeneral + " / Feedback Update Rate " + millisFeedback);
                }

                @Override
                public void configureMaximumOutputVoltage(float forwardVoltage, float reverseVoltage) {
                    getFloatOutput("Maximum Forward Output Voltage").set(forwardVoltage);
                    getFloatOutput("Maximum Reverse Output Voltage").set(reverseVoltage);
                }

                @Override
                public void configureNominalOutputVoltage(float forwardVoltage, float reverseVoltage) {
                    getFloatOutput("Nominal Forward Output Voltage").set(forwardVoltage);
                    getFloatOutput("Nominal Reverse Output Voltage").set(reverseVoltage);
                }

                @Override
                public BooleanIO getBrakeNotCoast() {
                    return getBooleanInput("Brake Mode (not Coast)");
                }
            };
        }

        @Override
        public int getDeviceID() {
            return id;
        }
    }

    /**
     * Creates a new CANTalonDevice described as a CAN Talon of the given ID
     * with a specified DeviceListPanel to contain this device.
     *
     * Make sure to call addToMaster - don't add this directly.
     *
     * @param id the CAN bus ID of the device.
     * @param master the panel that contains this.
     * @see #addToMaster()
     */
    public CANTalonDevice(int id, DeviceListPanel master) {
        super("Talon SRX on CAN " + id, master);
        this.id = id;
        if (id < 0 || id > 62) {
            throw new IllegalArgumentException("Invalid CAN id: " + id);
        }
        this.value.modEncoder().useEncoder();
    }

    private final TalonExtendedMotor value = new VirtualTalon();

    /**
     * Gets the ExtendedMotor interface to pass to the emulated program.
     *
     * @return the ExtendedMotor of this CANTalon.
     */
    public TalonExtendedMotor getMotor() {
        return value;
    }

    @Override
    public void notifyDisabled(boolean disabled) {
        for (Device d : this) {
            if (d instanceof Disableable) {
                ((Disableable) d).notifyDisabled(disabled);
            }
        }
    }

    public CANTalonDevice addToMaster() {
        super.addToMaster();
        return this;
    }
}
