/*
 * Copyright 2016 Colby Skeggs
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
package org.team1540.cantest;

import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.drivers.ctre.talon.TalonExtendedMotor;
import ccre.frc.FRC;
import ccre.frc.FRCApplication;
import ccre.log.Logger;
import ccre.timers.Ticker;

/**
 * A simple test program that will allow testing advanced control of a CAN
 * Talon.
 *
 * @author skeggsc
 */
public class TalonTest implements FRCApplication {

    @Override
    public void setupRobot() throws ExtendedMotorFailureException {
        TalonExtendedMotor motor = FRC.talonCAN(8);
        /*FRC.talonCAN(9).activateFollowerMode(motor);
        Cluck.publish("Output Voltage", motor.getOutputVoltage());
        Cluck.publish("Output Current", motor.getOutputCurrent());
        Cluck.publish("Temperature", motor.getTemperature());
        Cluck.publish("Bus Voltage", motor.getBusVoltage());
        Cluck.publish("Brake Mode", motor.getBrakeNotCoast());
        Cluck.publish("Closed Loop Error", motor.getClosedLoopError());
        Cluck.publish("Sensor Position", motor.getSensorPosition());
        Cluck.publish("Sensor Velocity", motor.getSensorVelocity());
        Cluck.publish("Throttle", motor.getThrottle());
        {
            TalonAnalog ta = motor.modAnalog();
            Cluck.publish("Analog Position", ta.getAnalogPosition());
            Cluck.publish("Analog Velocity", ta.getAnalogVelocity());
            Cluck.publish("Analog Use Encoder", ta::useAnalogEncoder);
            Cluck.publish("Analog Use Potentiometer", ta::useAnalogPotentiometer);
        }
        {
            TalonEncoder te = motor.modEncoder();
            Cluck.publish("Encoder Position", te.getEncoderPosition());
            Cluck.publish("Encoder Velocity", te.getEncoderVelocity());
            Cluck.publish("Encoder Indexes", te.getNumberOfQuadIndexRises());
            Cluck.publish("Encoder Pin A", te.getQuadAPin());
            Cluck.publish("Encoder Pin B", te.getQuadBPin());
            Cluck.publish("Encoder Pin Index", te.getQuadIndexPin());
            Cluck.publish("Encoder Use Quad", te::useEncoder);
            Cluck.publish("Encoder Use Rising", te::useRisingEdge);
            Cluck.publish("Encoder Use Falling", te::useFallingEdge);
        }
        {
            Faultable<Faults> tf = motor.modFaults();
            for (TalonAll.Faults fault : TalonAll.Faults.values()) {
                Cluck.publish("Fault " + fault.name(), tf.getIsFaulting(fault));
                Cluck.publish("Sticky Fault " + fault.name(), tf.getIsStickyFaulting(fault));
            }
            Cluck.publish("Clear Sticky Faults", tf::getClearStickyFaults);
        }
        {
            TalonHardLimits th = motor.modHardLimits();
            Cluck.publish("Hard Limit Forward Closed", th.getIsForwardLimitSwitchClosed());
            Cluck.publish("Hard Limit Reverse Closed", th.getIsReverseLimitSwitchClosed());
        }
        {
            TalonPIDConfiguration tp = motor.modPID();
            Cluck.publish("PID P", tp.getP());
            Cluck.publish("PID I", tp.getI());
            Cluck.publish("PID D", tp.getD());
            Cluck.publish("PID F", tp.getF());
            Cluck.publish("PID Integral Accumulator", tp.getIAccum());
            Cluck.publish("PID Close Loop Ramp Rate", tp.getCloseLoopRampRate());
            Cluck.publish("PID Integral Bounds", tp.getIntegralBounds());
            Cluck.publish("PID Secondary Profile", tp.getIsSecondaryProfileActive());
        }
        {
            TalonPulseWidth tpw = motor.modPulseWidth();
            Cluck.publish("Pulse Position", tpw.getPulseWidthPosition());
            Cluck.publish("Pulse Velocity", tpw.getPulseWidthVelocity());
            Cluck.publish("Pulse Rise-to-Rise", tpw.getPulseWidthRiseToRiseMicroseconds());
            Cluck.publish("Pulse Rise-to-Fall", tpw.getPulseWidthRiseToFallMicroseconds());
            Cluck.publish("Pulse Present", tpw.getPulseWidthOrCtreMagEncoderPresent());
        }
        {
            TalonSoftLimits ts = motor.modSoftLimits();
            Cluck.publish("Soft Limit Enable Forward", ts.getEnableForwardSoftLimit());
            Cluck.publish("Soft Limit Enable Reverse", ts.getEnableReverseSoftLimit());
            Cluck.publish("Soft Limit Threshold Forward", ts.getForwardSoftLimit());
            Cluck.publish("Soft Limit Threshold Reverse", ts.getReverseSoftLimit());
        }*/
        new Ticker(5000).send(() -> {
            Logger.info("Firmware: " + motor.modFeedback().GetFirmwareVersion());
        });
        /*Cluck.publish("Motor Enable", motor.asEnable());
        FloatSink out = new FloatSink(motor.asMode(OutputControlMode.VOLTAGE_FRACTIONAL));
        for (OutputControlMode ocm : OutputControlMode.values()) {
            Cluck.publish("Motor Set Mode " + ocm.name(), () -> {
                try {
                    out.setSink(motor.asMode(ocm));
                } catch (Exception e) {
                    Logger.severe("Cannot change mode", e);
                }
            });
        }
        Cluck.publish("Motor Control", out);*/
    }

    public static class FloatSink implements FloatOutput {
        protected FloatOutput sink;

        public FloatSink() {
            this.sink = FloatOutput.ignored;
        }

        public FloatSink(FloatOutput sink) {
            if (sink == null) {
                throw new NullPointerException();
            }
            this.sink = sink;
        }

        public void setSink(FloatOutput output) {
            if (output == null) {
                throw new NullPointerException();
            }
            sink = output;
        }

        @Override
        public void set(float value) {
            sink.set(value);
        }
    }
}
