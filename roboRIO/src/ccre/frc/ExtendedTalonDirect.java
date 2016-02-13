/*
 * Copyright 2014-2016 Cel Skeggs
 * Partially based on code Copyright 2016 FIRST - see below copyright.
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
// Original copyright for the CANTalon implementation in WPILib:
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2016. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package ccre.frc;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanIO;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.DerivedFloatIO;
import ccre.channel.DerivedFloatInput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatIO;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.concurrency.ReporterThread;
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
import ccre.timers.Ticker;
import edu.wpi.first.wpilibj.hal.CanTalonJNI;

class ExtendedTalonDirect extends TalonExtendedMotor {

    private static final int PARAMETER_REQUEST_PERIOD_MILLIS = 19;
    // based on the magic number in WPILib. Is this the best? WHO KNOWS
    private static final int SOLICITED_SIGNAL_LATENCY_MILLIS = 4;
    // null until something cares. This means that it's not enabled, but could
    // be automatically.
    private Boolean enableMode = null;
    private final EventCell updateMode = new EventCell();
    private final int deviceID;
    private final long handle;
    private final BooleanCell secondaryProfileActive = new BooleanCell();
    private float m_codesPerRev;
    private float potentiometerRotationsPerSweep;
    private int activation_mode = MODE_PERCENT;
    private FeedbackDevice feedback = FeedbackDevice.QuadEncoder;
    private final Ticker updateTicker = new Ticker(1); // TODO: choose timing
                                                       // more purposefully
    private final Ticker slowerTicker = new Ticker(10);

    private static final int ANALOG_TICKS = 1024;
    private static final int PULSE_WIDTH_TICKS = 4096;
    private static final int GENERAL_FRAME = 0;
    private static final int FEEDBACK_FRAME = 1;
    private static final int QUAD_ENCODER_FRAME = 2;
    private static final int ANALOG_TEMPERATURE_BATTERY_FRAME = 3;
    private static final int PULSE_WIDTH_FRAME = 4;

    private static final float MINUTES_PER_100_MILLIS = 1 / 600f;

    private static final int[] values = new int[] {

            CanTalonJNI.param_t.eProfileParamSoftLimitForThreshold.value, CanTalonJNI.param_t.eProfileParamSoftLimitRevThreshold.value,

            CanTalonJNI.param_t.eProfileParamSoftLimitForEnable.value, CanTalonJNI.param_t.eProfileParamSoftLimitRevEnable.value,

            CanTalonJNI.param_t.eProfileParamSlot0_P.value, CanTalonJNI.param_t.eProfileParamSlot1_P.value,

            CanTalonJNI.param_t.eProfileParamSlot0_I.value, CanTalonJNI.param_t.eProfileParamSlot1_I.value,

            CanTalonJNI.param_t.eProfileParamSlot0_D.value, CanTalonJNI.param_t.eProfileParamSlot1_D.value,

            CanTalonJNI.param_t.eProfileParamSlot0_F.value, CanTalonJNI.param_t.eProfileParamSlot1_F.value,

            CanTalonJNI.param_t.eProfileParamSlot0_IZone.value, CanTalonJNI.param_t.eProfileParamSlot1_IZone.value,

            CanTalonJNI.param_t.eProfileParamSlot0_CloseLoopRampRate.value, CanTalonJNI.param_t.eProfileParamSlot1_CloseLoopRampRate.value,

            CanTalonJNI.param_t.eFirmVers.value, CanTalonJNI.param_t.ePidIaccum.value,

    };

    private int getProfileID() {
        return secondaryProfileActive.get() ? 1 : 0;
    }

    // Feedback direct quote - add WPILib credit if reused
    public enum FeedbackDevice {
        QuadEncoder(0), AnalogPot(2), AnalogEncoder(3), EncRising(4), EncFalling(5), CtreMagEncoder_Relative(6), CtreMagEncoder_Absolute(7), PulseWidth(8);

        public int value;

        public static FeedbackDevice valueOf(int value) {
            for (FeedbackDevice mode : values()) {
                if (mode.value == value) {
                    return mode;
                }
            }

            return null;
        }

        private FeedbackDevice(int value) {
            this.value = value;
        }
    }

    private static final int MODE_PERCENT = 0, MODE_POSITION = 1, MODE_SPEED = 2, MODE_CURRENT = 3, MODE_VOLTAGE = 4, MODE_FOLLOWER = 5, MODE_MOTION_PROFILE = 6, MODE_DISABLED = 15;

    public ExtendedTalonDirect(int deviceID) {
        this.deviceID = deviceID;
        // TODO: specify controlPeriodMs and enablePeriodMs.
        handle = CanTalonJNI.new_CanTalonSRX(deviceID);
        if (handle == 0) {
            throw new RuntimeException("Failed to initialize CanTalonSRX");
        }
        this.secondaryProfileActive.send((secondary) -> {
            CanTalonJNI.SetProfileSlotSelect(handle, secondary ? 1 : 0);
        });

        CanTalonJNI.RequestParam(handle, CanTalonJNI.param_t.eFirmVers.value);

        try {
            Thread.sleep(SOLICITED_SIGNAL_LATENCY_MILLIS);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }

        new ReporterThread("CANTalonRequestor") {
            @Override
            protected void threadBody() throws Throwable {
                for (int value : values) {
                    CanTalonJNI.RequestParam(handle, value);
                    try {
                        Thread.sleep(PARAMETER_REQUEST_PERIOD_MILLIS);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }.start();
    }

    private void setFeedbackDevice(FeedbackDevice device) {
        CanTalonJNI.SetFeedbackDeviceSelect(handle, device.value);
    }

    @Override
    public Faultable<Faults> modFaults() {
        return new Faultable<Faults>() {
            // not actually sticky on the Talon, so fake it!
            private boolean hardware_failure_sticky = false;

            @Override
            public Faults[] getPossibleFaults() {
                return Faults.values();
            }

            @Override
            public BooleanInput getIsStickyFaulting(Faults fault) {
                return new DerivedBooleanInput(slowerTicker) {
                    protected boolean apply() {
                        switch (fault) {
                        case FORWARD_HARD_LIMIT:
                            return CanTalonJNI.GetStckyFault_ForLim(handle) != 0;
                        case FORWARD_SOFT_LIMIT:
                            return CanTalonJNI.GetStckyFault_ForSoftLim(handle) != 0;
                        case HARDWARE_FAILURE:
                            if (!hardware_failure_sticky) {
                                hardware_failure_sticky = CanTalonJNI.GetFault_HardwareFailure(handle) != 0;
                            }
                            return hardware_failure_sticky;
                        case OVER_TEMPERATURE:
                            return CanTalonJNI.GetStckyFault_OverTemp(handle) != 0;
                        case REVERSE_HARD_LIMIT:
                            return CanTalonJNI.GetStckyFault_RevLim(handle) != 0;
                        case REVERSE_SOFT_LIMIT:
                            return CanTalonJNI.GetStckyFault_RevSoftLim(handle) != 0;
                        case UNDER_VOLTAGE:
                            return CanTalonJNI.GetStckyFault_UnderVoltage(handle) != 0;
                        default:
                            // since DerivedBooleanInput calls apply()
                            // immediately, this will always happen at the right
                            // time.
                            throw new IllegalArgumentException("Invalid fault: " + fault);
                        }
                    }
                };
            }

            @Override
            public BooleanInput getIsFaulting(Faults fault) {
                return new DerivedBooleanInput(slowerTicker) {
                    protected boolean apply() {
                        switch (fault) {
                        // some of these reused below
                        case FORWARD_HARD_LIMIT:
                            return CanTalonJNI.GetFault_ForLim(handle) != 0;
                        case FORWARD_SOFT_LIMIT:
                            return CanTalonJNI.GetFault_ForSoftLim(handle) != 0;
                        case HARDWARE_FAILURE:
                            return CanTalonJNI.GetFault_HardwareFailure(handle) != 0;
                        case OVER_TEMPERATURE:
                            return CanTalonJNI.GetFault_OverTemp(handle) != 0;
                        case REVERSE_HARD_LIMIT:
                            return CanTalonJNI.GetFault_RevLim(handle) != 0;
                        case REVERSE_SOFT_LIMIT:
                            return CanTalonJNI.GetFault_RevSoftLim(handle) != 0;
                        case UNDER_VOLTAGE:
                            return CanTalonJNI.GetFault_UnderVoltage(handle) != 0;
                        default:
                            // since DerivedBooleanInput calls apply()
                            // immediately, this will always happen at the right
                            // time.
                            throw new IllegalArgumentException("Invalid fault: " + fault);
                        }
                    }
                };
            }

            @Override
            public EventOutput getClearStickyFaults() {
                return () -> {
                    hardware_failure_sticky = false;
                    CanTalonJNI.ClearStickyFaults(handle);
                };
            }
        };
    }

    @Override
    public TalonAnalog modAnalog() {
        return new TalonAnalog() {
            @Override
            public void useAnalogEncoder() {
                setFeedbackDevice(FeedbackDevice.AnalogEncoder);
            }

            @Override
            public void useAnalogPotentiometer() {
                setFeedbackDevice(FeedbackDevice.AnalogPot);
            }

            @Override
            public FloatInput getAnalogVelocity() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRPM(FeedbackDevice.AnalogEncoder, CanTalonJNI.GetAnalogInVel(handle));
                    }
                };
            }

            @Override
            public FloatIO getAnalogPosition() {
                return new DerivedFloatIO(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRotations(FeedbackDevice.AnalogPot, CanTalonJNI.GetAnalogInWithOv(handle) & 0x3FF);
                    }

                    public void set(float f) {
                        f = rotationsToNative(FeedbackDevice.AnalogPot, f);
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eAinPosition.value, f);
                    }
                };
            }

            @Override
            public FloatIO getAnalogPositionEncoder() {
                return new DerivedFloatIO(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRotations(FeedbackDevice.AnalogEncoder, CanTalonJNI.GetAnalogInWithOv(handle));
                    }

                    public void set(float f) {
                        f = rotationsToNative(FeedbackDevice.AnalogEncoder, f);
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eAinPosition.value, f);
                    }
                };
            }

            @Override
            public void configureAnalogUpdateRate(int millis) {
                CanTalonJNI.SetStatusFrameRate(handle, ANALOG_TEMPERATURE_BATTERY_FRAME, millis);
            }

            @Override
            public void configurePotentiometerTurns(float rotations) {
                potentiometerRotationsPerSweep = rotations;
                // only used for backreporting, not real functionality
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eAinPosition.value, rotations);
            }
        };
    }

    @Override
    public TalonEncoder modEncoder() {
        return new TalonEncoder() {
            @Override
            public void useEncoder() {
                setFeedbackDevice(FeedbackDevice.QuadEncoder);
            }

            @Override
            public void useFallingEdge() {
                setFeedbackDevice(FeedbackDevice.EncFalling);
            }

            @Override
            public void useRisingEdge() {
                setFeedbackDevice(FeedbackDevice.EncRising);
            }

            @Override
            public BooleanInput getQuadIndexPin() {
                return new DerivedBooleanInput(updateTicker) {
                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetQuadIdxpin(handle) != 0;
                    }
                };
            }

            @Override
            public BooleanInput getQuadBPin() {
                return new DerivedBooleanInput(updateTicker) {
                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetQuadBpin(handle) != 0;
                    }
                };
            }

            @Override
            public BooleanInput getQuadAPin() {
                return new DerivedBooleanInput(updateTicker) {
                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetQuadApin(handle) != 0;
                    }
                };
            }

            @Override
            public FloatInput getNumberOfQuadIndexRises() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return CanTalonJNI.GetEncIndexRiseEvents(handle);
                    }
                };
            }

            @Override
            public FloatInput getEncoderVelocity() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRPM(FeedbackDevice.QuadEncoder, CanTalonJNI.GetEncVel(handle));
                    }
                };
            }

            @Override
            public FloatIO getEncoderPosition() {
                return new DerivedFloatIO(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRotations(FeedbackDevice.QuadEncoder, CanTalonJNI.GetEncPosition(handle));
                    }

                    @Override
                    public void set(float f) {
                        f = rotationsToNative(FeedbackDevice.QuadEncoder, f);
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eEncPosition.value, f);
                    }
                };
            }

            @Override
            public void configureEncoderUpdateRate(int millis) {
                CanTalonJNI.SetStatusFrameRate(handle, QUAD_ENCODER_FRAME, millis);
            }

            @Override
            public void configureEncoderCodesPerRev(float perRev) {
                m_codesPerRev = perRev;
                // only used for backreporting, not real functionality
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eNumberEncoderCPR.value, perRev);
            }
        };
    }

    @Override
    public TalonHardLimits modHardLimits() {
        return new TalonHardLimits() {
            @Override
            public BooleanInput getIsReverseLimitSwitchClosed() {
                return new DerivedBooleanInput(updateTicker) {
                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetLimitSwitchClosedRev(handle) == 0;
                    }
                };
            }

            @Override
            public BooleanInput getIsForwardLimitSwitchClosed() {
                return new DerivedBooleanInput(updateTicker) {
                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetLimitSwitchClosedFor(handle) == 0;
                    }
                };
            }

            @Override
            public void configureLimitSwitches(boolean forwardEnable, boolean forwardNC, boolean reverseEnable, boolean reverseNC) {
                CanTalonJNI.SetOverrideLimitSwitchEn(handle, 4 | (forwardEnable ? 2 : 0) | (reverseEnable ? 1 : 0));
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eOnBoot_LimitSwitch_Forward_NormallyClosed.value, forwardNC ? 1 : 0);
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eOnBoot_LimitSwitch_Reverse_NormallyClosed.value, reverseNC ? 1 : 0);
            }
        };
    }

    @Override
    public TalonPIDConfiguration modPID() {
        return new TalonPIDConfiguration() {
            // these are attached to the query loop above because they aren't
            // autosent
            @Override
            public FloatIO getP() {
                return new DerivedFloatIO(slowerTicker, secondaryProfileActive) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetPgain(handle, getProfileID());
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetPgain(handle, getProfileID(), f);
                    }
                };
            }

            @Override
            public FloatIO getI() {
                return new DerivedFloatIO(slowerTicker, secondaryProfileActive) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetIgain(handle, getProfileID());
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetIgain(handle, getProfileID(), f);
                    }
                };
            }

            @Override
            public FloatIO getD() {
                return new DerivedFloatIO(slowerTicker, secondaryProfileActive) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetDgain(handle, getProfileID());
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetDgain(handle, getProfileID(), f);
                    }
                };
            }

            @Override
            public FloatIO getF() {
                return new DerivedFloatIO(slowerTicker, secondaryProfileActive) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetFgain(handle, getProfileID());
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetFgain(handle, getProfileID(), f);
                    }
                };
            }

            @Override
            public FloatIO getIntegralBounds() {
                return new DerivedFloatIO(slowerTicker, secondaryProfileActive) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetIzone(handle, getProfileID());
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetIzone(handle, getProfileID(), (int) f);
                    }
                };
            }

            @Override
            public FloatIO getCloseLoopRampRate() {
                return new DerivedFloatIO(slowerTicker, secondaryProfileActive) {
                    @Override
                    protected float apply() {
                        return CanTalonJNI.GetCloseLoopRampRate(handle, getProfileID()) * 12 * 1000 / 1023f;
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetCloseLoopRampRate(handle, getProfileID(), (int) (f * 1023 / 12 / 1000));
                    }
                };
            }

            @Override
            public FloatIO getIAccum() {
                return new DerivedFloatIO(slowerTicker) {
                    @Override
                    protected float apply() {
                        return CanTalonJNI.GetParamResponseInt32(handle, CanTalonJNI.param_t.ePidIaccum.value);
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.ePidIaccum.value, f);
                    }
                };
            }

            @Override
            public BooleanIO getIsSecondaryProfileActive() {
                return secondaryProfileActive;
            }

            @Override
            public void configureRampRates(float throttle, float voltageCompensation) {
                CanTalonJNI.SetRampThrottle(handle, (int) (throttle * 1023 / 12 / 100));
                CanTalonJNI.SetVoltageCompensationRate(handle, voltageCompensation / 1000);
            }
        };
    }

    @Override
    public TalonPulseWidth modPulseWidth() {
        return new TalonPulseWidth() {
            @Override
            public void configurePulseWidthUpdateRate(int millis) {
                CanTalonJNI.SetStatusFrameRate(handle, PULSE_WIDTH_FRAME, millis);
            }

            @Override
            public void usePulseWidth() {
                setFeedbackDevice(FeedbackDevice.PulseWidth);
            }

            @Override
            public void useAbsoluteCtreMagEncoder() {
                setFeedbackDevice(FeedbackDevice.CtreMagEncoder_Absolute);
            }

            @Override
            public void useRelativeCtreMagEncoder() {
                setFeedbackDevice(FeedbackDevice.CtreMagEncoder_Relative);
            }

            @Override
            public FloatIO getPulseWidthPosition() {
                return new DerivedFloatIO(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRotations(FeedbackDevice.PulseWidth, CanTalonJNI.GetPulseWidthPosition(handle));
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.ePwdPosition.value, rotationsToNative(FeedbackDevice.PulseWidth, f));
                    }
                };
            }

            @Override
            public FloatInput getPulseWidthVelocity() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRPM(FeedbackDevice.PulseWidth, CanTalonJNI.GetPulseWidthVelocity(handle));
                    }
                };
            }

            @Override
            public FloatInput getPulseWidthRiseToFallMicroseconds() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return CanTalonJNI.GetPulseWidthRiseToFallUs(handle);
                    }
                };
            }

            @Override
            public FloatInput getPulseWidthRiseToRiseMicroseconds() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return CanTalonJNI.GetPulseWidthRiseToRiseUs(handle);
                    }
                };
            }

            @Override
            public BooleanInput getPulseWidthOrCtreMagEncoderPresent() {
                return new DerivedBooleanInput(updateTicker) {
                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.IsPulseWidthSensorPresent(handle) != 0;
                    }
                };
            }
        };
    }

    @Override
    public TalonSoftLimits modSoftLimits() {
        return new TalonSoftLimits() {
            @Override
            public FloatIO getForwardSoftLimit() {
                return new DerivedFloatIO(slowerTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRotations(FeedbackDevice.PulseWidth, CanTalonJNI.GetParamResponseInt32(handle, CanTalonJNI.param_t.eProfileParamSoftLimitForThreshold.value));
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eProfileParamSoftLimitForThreshold.value, rotationsToNative(FeedbackDevice.PulseWidth, f));
                    }
                };
            }

            @Override
            public BooleanIO getEnableForwardSoftLimit() {
                return new DerivedBooleanIO(slowerTicker) {
                    @Override
                    public void set(boolean enable) {
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eProfileParamSoftLimitForEnable.value, enable ? 1 : 0);
                    }

                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetParamResponseInt32(handle, CanTalonJNI.param_t.eProfileParamSoftLimitForEnable.value) != 0;
                    }
                };
            }

            @Override
            public FloatIO getReverseSoftLimit() {
                return new DerivedFloatIO(slowerTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRotations(FeedbackDevice.PulseWidth, CanTalonJNI.GetParamResponseInt32(handle, CanTalonJNI.param_t.eProfileParamSoftLimitRevThreshold.value));
                    }

                    @Override
                    public void set(float f) {
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eProfileParamSoftLimitForThreshold.value, rotationsToNative(FeedbackDevice.PulseWidth, f));
                    }
                };
            }

            @Override
            public BooleanIO getEnableReverseSoftLimit() {
                return new DerivedBooleanIO(slowerTicker) {
                    @Override
                    public void set(boolean enable) {
                        CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eProfileParamSoftLimitRevEnable.value, enable ? 1 : 0);
                    }

                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetParamResponseInt32(handle, CanTalonJNI.param_t.eProfileParamSoftLimitRevEnable.value) != 0;
                    }
                };
            }
        };
    }

    @Override
    public TalonFeedback modFeedback() {
        return new TalonFeedback() {
            @Override
            public FloatInput getBusVoltage() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetBatteryV(handle);
                    }
                };
            }

            @Override
            public FloatInput getOutputVoltage() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return (float) (CanTalonJNI.GetBatteryV(handle) * CanTalonJNI.GetAppliedThrottle(handle) / 1023);
                    }
                };
            }

            @Override
            public FloatInput getOutputCurrent() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetCurrent(handle);
                    }
                };
            }

            @Override
            public FloatIO getSensorPosition() {
                return new DerivedFloatIO(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRotations(feedback, CanTalonJNI.GetSensorPosition(handle));
                    }

                    @Override
                    public void set(float value) {
                        CanTalonJNI.SetSensorPosition(handle, rotationsToNative(feedback, value));
                    }
                };
            }

            @Override
            public FloatInput getSensorVelocity() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return nativeToRPM(feedback, CanTalonJNI.GetSensorVelocity(handle));
                    }
                };
            }

            @Override
            public FloatInput getThrottle() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return CanTalonJNI.GetAppliedThrottle(handle) / 1023f;
                    }
                };
            }

            @Override
            public FloatInput getClosedLoopError() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        int raw = CanTalonJNI.GetCloseLoopErr(handle);
                        switch (activation_mode) {
                        case MODE_CURRENT:
                            return raw / 1000f;
                        case MODE_SPEED:
                            return nativeToRotations(feedback, raw);
                        case MODE_POSITION:
                            return nativeToRPM(feedback, raw);
                        default:
                            // TODO: what now?
                            return raw;
                        }
                    }
                };
            }

            @Override
            public FloatInput getTemperature() {
                return new DerivedFloatInput(updateTicker) {
                    @Override
                    protected float apply() {
                        return (float) CanTalonJNI.GetTemp(handle);
                    }
                };
            }

            @Override
            public long GetFirmwareVersion() {
                return CanTalonJNI.GetParamResponseInt32(handle, CanTalonJNI.param_t.eFirmVers.value);
            }
        };
    }

    @Override
    public TalonGeneralConfig modGeneralConfig() {
        return new TalonGeneralConfig() {
            @Override
            public BooleanIO getBrakeNotCoast() {
                return new DerivedBooleanIO(slowerTicker) {
                    @Override
                    protected boolean apply() {
                        return CanTalonJNI.GetBrakeIsEnabled(handle) != 0;
                    }

                    @Override
                    public void set(boolean brake) {
                        // TODO: perhaps I should provide access to setting it
                        // to zero?
                        // Which might cancel an override?
                        CanTalonJNI.SetOverrideBrakeType(handle, brake ? 2 : 1);
                    }
                };
            }

            @Override
            public void configureReversed(boolean flipSensor, boolean flipOutput) {
                CanTalonJNI.SetRevFeedbackSensor(handle, flipSensor ? 1 : 0);
                CanTalonJNI.SetRevMotDuringCloseLoopEn(handle, flipOutput ? 1 : 0);
            }

            @Override
            public void configureAllowableClosedLoopError(float allowableError) {
                int param = secondaryProfileActive.get() ? CanTalonJNI.param_t.eProfileParamSlot1_AllowableClosedLoopErr.value : CanTalonJNI.param_t.eProfileParamSlot0_AllowableClosedLoopErr.value;
                switch (activation_mode) {
                case MODE_CURRENT:
                    // takes amps
                    CanTalonJNI.SetParam(handle, param, allowableError * 1000);
                    break;
                case MODE_SPEED:
                    // takes RPM
                    CanTalonJNI.SetParam(handle, param, rpmToNative(feedback, allowableError));
                    break;
                case MODE_POSITION:
                    // takes position
                    CanTalonJNI.SetParam(handle, param, rotationsToNative(feedback, allowableError));
                    break;
                default:
                    // TODO: what now?
                    CanTalonJNI.SetParam(handle, param, allowableError);
                }
            }

            @Override
            public void configureGeneralFeedbackUpdateRate(int millisGeneral, int millisFeedback) {
                CanTalonJNI.SetStatusFrameRate(handle, GENERAL_FRAME, millisGeneral);
                CanTalonJNI.SetStatusFrameRate(handle, FEEDBACK_FRAME, millisFeedback);
            }

            @Override
            public void configureMaximumOutputVoltage(float forwardVoltage, float reverseVoltage) {
                forwardVoltage = Math.max(0, Math.min(12, forwardVoltage));
                reverseVoltage = Math.max(-12, Math.min(0, reverseVoltage));
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.ePeakPosOutput.value, 1023 * forwardVoltage / 12);
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.ePeakNegOutput.value, 1023 * reverseVoltage / 12);
            }

            @Override
            public void configureNominalOutputVoltage(float forwardVoltage, float reverseVoltage) {
                forwardVoltage = Math.max(0, Math.min(12, forwardVoltage));
                reverseVoltage = Math.max(-12, Math.min(0, reverseVoltage));
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eNominalPosOutput.value, 1023 * forwardVoltage / 12);
                CanTalonJNI.SetParam(handle, CanTalonJNI.param_t.eNominalNegOutput.value, 1023 * reverseVoltage / 12);
            }

            @Override
            public void activateFollowerMode(int talonID) {
                disable();
                activation_mode = MODE_FOLLOWER;
                CanTalonJNI.SetDemand(handle, talonID);
                enable();
            }
        };
    }

    @Override
    public int getDeviceID() {
        return deviceID;
    }

    @Override
    public synchronized void enable() {
        if (enableMode != null && enableMode) {
            return;
        }
        enableMode = true;
        updateMode.event();
        CanTalonJNI.SetModeSelect(handle, activation_mode);
    }

    @Override
    public synchronized void disable() {
        if (enableMode != null && !enableMode) {
            return;
        }
        enableMode = false;
        updateMode.event();
        CanTalonJNI.SetModeSelect(handle, MODE_DISABLED);
    }

    @Override
    public BooleanIO asEnable() {
        return new DerivedBooleanIO(updateMode) {
            @Override
            public void set(boolean enable) {
                if (enable) {
                    enable();
                } else {
                    disable();
                }
            }

            @Override
            protected boolean apply() {
                return enableMode != null && enableMode;
            }
        };
    }

    private void potentiallyEnable() {
        if (enableMode == null) {
            enable();
        }
    }

    private void changeMode(int mode) throws ExtendedMotorFailureException {
        boolean enabled = enableMode == null ? false : enableMode;
        if (enabled) {
            disable();
        }
        activation_mode = mode;
        if (mode == MODE_PERCENT) {
            CanTalonJNI.Set(handle, 0);
        } else {
            CanTalonJNI.SetDemand(handle, 0);
        }
        if (enabled) {
            enable();
        }
    }

    @Override
    public FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException {
        try {
            switch (mode) {
            case CURRENT_FIXED:
                changeMode(MODE_CURRENT);
                return (f) -> {
                    // convert to milliamps
                    CanTalonJNI.SetDemand(handle, (int) (f * 1000));
                    potentiallyEnable();
                };
            case VOLTAGE_FIXED:
                changeMode(MODE_VOLTAGE);
                return (f) -> {
                    // convert to volts, in 8.8 fixed point real
                    CanTalonJNI.SetDemand(handle, (int) (f * 256));
                    potentiallyEnable();
                };
            case GENERIC_FRACTIONAL:
            case VOLTAGE_FRACTIONAL:
                changeMode(MODE_PERCENT);
                return (f) -> {
                    CanTalonJNI.Set(handle, f);
                    potentiallyEnable();
                };
            case POSITION_FIXED:
                changeMode(MODE_POSITION);
                return (f) -> {
                    CanTalonJNI.SetDemand(handle, rotationsToNative(feedback, f));
                    potentiallyEnable();
                };
            case SPEED_FIXED:
                changeMode(MODE_SPEED);
                return (f) -> {
                    CanTalonJNI.SetDemand(handle, rpmToNative(feedback, f));
                    potentiallyEnable();
                };
            // TODO: Support motion profiles.
            default:
                return null;
            }
        } catch (RuntimeException ex) {
            throw new ExtendedMotorFailureException("WPILib CANTalon Failure: Set Mode", ex);
        }
    }

    @Override
    public FloatInput asStatus(StatusType type, EventInput updateOn) {
        switch (type) {
        case BUS_VOLTAGE:
            return modFeedback().getBusVoltage();
        case OUTPUT_CURRENT:
            return modFeedback().getOutputCurrent();
        case OUTPUT_VOLTAGE:
            return modFeedback().getOutputVoltage();
        case TEMPERATURE:
            return modFeedback().getTemperature();
        default:
            return null;
        }
    }

    @Override
    public Object getDiagnostics(DiagnosticType type) {
        try {
            switch (type) {
            case ANY_FAULT:
                return CanTalonJNI.GetFault_UnderVoltage(handle) != 0 || CanTalonJNI.GetFault_OverTemp(handle) != 0 || CanTalonJNI.GetFault_HardwareFailure(handle) != 0;
            case BUS_VOLTAGE_FAULT:
                return CanTalonJNI.GetFault_UnderVoltage(handle) != 0;
            case TEMPERATURE_FAULT:
                return CanTalonJNI.GetFault_OverTemp(handle) != 0;
            case HARDWARE_FAULT:
                return CanTalonJNI.GetFault_HardwareFailure(handle) != 0;
            default:
                return null;
            }
        } catch (RuntimeException ex) {
            return null;
        }
    }

    @Override
    public boolean hasInternalPID() {
        return true;
    }

    @Override
    @Deprecated
    public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
        CanTalonJNI.SetPgain(handle, getProfileID(), P);
        CanTalonJNI.SetIgain(handle, getProfileID(), I);
        CanTalonJNI.SetDgain(handle, getProfileID(), D);
        CanTalonJNI.SetFgain(handle, getProfileID(), 0);
    }

    // conversions

    private int rotationsToNative(FeedbackDevice device, float fullRotations) {
        return (int) (fullRotations * GetNativeUnitsPerRotationScalar(device));
    }

    private int rpmToNative(FeedbackDevice device, float rpm) {
        return (int) (rpm * MINUTES_PER_100_MILLIS * GetNativeUnitsPerRotationScalar(device));
    }

    private float nativeToRotations(FeedbackDevice device, int nativePos) {
        return nativePos / GetNativeUnitsPerRotationScalar(device);
    }

    private float nativeToRPM(FeedbackDevice device, int nativeVel) {
        return nativeVel / (GetNativeUnitsPerRotationScalar(device) * MINUTES_PER_100_MILLIS);
    }

    private float GetNativeUnitsPerRotationScalar(FeedbackDevice device) {
        switch (device) {
        case QuadEncoder:
            // TODO: include the complex algorithm about CPR defaults? If it
            // ever gets fixed up?
            if (0 == m_codesPerRev) {
                return 1; // native units
            } else {
                return m_codesPerRev * 4;
            }
        case EncRising:
        case EncFalling:
            if (0 == m_codesPerRev) {
                return 1; // native units
            } else {
                return m_codesPerRev * 1;
            }
        case AnalogPot:
        case AnalogEncoder:
            if (0 == potentiometerRotationsPerSweep) {
                return 1; // native units
            } else {
                return ANALOG_TICKS / potentiometerRotationsPerSweep;
            }
        case CtreMagEncoder_Relative:
        case CtreMagEncoder_Absolute:
        case PulseWidth:
            return PULSE_WIDTH_TICKS;
        }
        return 1; // native units
    }
}
