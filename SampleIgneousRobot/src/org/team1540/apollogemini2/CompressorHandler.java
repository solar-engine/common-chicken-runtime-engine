/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 *
 * This file is part of the Revised ApolloGemini2014 project.
 *
 * ApolloGemini2014 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * ApolloGemini2014 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ApolloGemini2014.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.apollogemini2;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventOutput;
import ccre.channel.FloatInputPoll;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Ticker;
import ccre.holders.TuningContext;
import ccre.igneous.Igneous;
import ccre.log.Logger;

public class CompressorHandler {
    private static final TuningContext pressureTuningContext = new TuningContext("PressureTuner").publishSavingEvent();

    private static FloatInputPoll getPercentPressure(FloatInputPoll pressureSensorVolts) {
        return FloatMixing.multiplication.of(100,
                FloatMixing.normalizeFloat(pressureSensorVolts,
                        pressureTuningContext.getFloat("LowPressure", 0.494f),
                        pressureTuningContext.getFloat("HighPressure", Igneous.isRoboRIO() ? 2.7f : 2.9f)));
    }

    public static void setup() {
        final BooleanInputPoll pressureSwitch = Igneous.makeDigitalInput(1);
        final BooleanStatus forceDisableVar = new BooleanStatus();
        Cluck.publish("Compressor Set Disable", forceDisableVar);
        final BooleanInputPoll forceDisable = BooleanMixing.orBooleans(forceDisableVar, Shooter.getShouldDisableDrivingAndCompressor());
        final FloatInputPoll pressureSensorVolts = Igneous.makeAnalogInput(Igneous.isRoboRIO() ? 0 : 2);
        Cluck.publish("Compressor Pressure Switch", BooleanMixing.createDispatch(pressureSwitch, Igneous.globalPeriodic));
        Cluck.publish("Compressor Pressure Sensor", FloatMixing.createDispatch(pressureSensorVolts, Igneous.globalPeriodic));
        final FloatInputPoll percentPressure = getPercentPressure(pressureSensorVolts);
        Cluck.publish("Compressor Pressure Percent", FloatMixing.createDispatch(percentPressure, Igneous.globalPeriodic));
        setupPressureLogger(percentPressure);
        ReadoutDisplay.showPressure(percentPressure, pressureSwitch);
        if (Igneous.isRoboRIO()) {
            BooleanOutput tryToRunCompressor = Igneous.usePCMCompressor();
            BooleanMixing.pumpWhen(Igneous.globalPeriodic, forceDisable, BooleanMixing.invert(tryToRunCompressor));
        } else {
            Igneous.useCustomCompressor(BooleanMixing.orBooleans(forceDisable, pressureSwitch), 1);
        }
    }

    private static void setupPressureLogger(final FloatInputPoll percentPressure) {
        EventOutput report = new EventOutput() {
            private float last;

            public void event() {
                float cur = percentPressure.get();
                if (Math.abs(last - cur) > 0.05) {
                    last = cur;
                    Logger.fine("Pressure: " + cur + "%");
                }
            }
        };
        Igneous.startAuto.send(report);
        Igneous.startTele.send(report);
        Igneous.startDisabled.send(report);
        new Ticker(10000).send(report);
    }
}
