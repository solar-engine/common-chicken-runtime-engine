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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.ctrl.Ticker;
import ccre.frc.FRC;
import ccre.holders.TuningContext;
import ccre.log.Logger;

public class CompressorHandler {
    private static final TuningContext pressureTuningContext = new TuningContext("PressureTuner").publishSavingEvent();

    private static FloatInput getPercentPressure(FloatInput pressureSensorVolts) {
        return pressureSensorVolts.normalize(pressureTuningContext.getFloat("LowPressure", 0.494f), pressureTuningContext.getFloat("HighPressure", FRC.isRoboRIO() ? 2.7f : 2.9f)).multipliedBy(100);
    }

    public static void setup() {
        final BooleanInput pressureSwitch = FRC.makeDigitalInput(1);
        final BooleanStatus forceDisableVar = new BooleanStatus();
        Cluck.publish("Compressor Set Disable", forceDisableVar);
        final BooleanInput forceDisable = forceDisableVar.or(Shooter.getShouldDisableDrivingAndCompressor());
        final FloatInput pressureSensorVolts = FRC.makeAnalogInput(FRC.isRoboRIO() ? 0 : 2);
        Cluck.publish("Compressor Pressure Switch", pressureSwitch);
        Cluck.publish("Compressor Pressure Sensor", pressureSensorVolts);
        final FloatInput percentPressure = getPercentPressure(pressureSensorVolts);
        Cluck.publish("Compressor Pressure Percent", percentPressure);
        setupPressureLogger(percentPressure);
        ReadoutDisplay.showPressure(percentPressure, pressureSwitch);
        if (FRC.isRoboRIO()) {
            forceDisable.send(FRC.usePCMCompressor().invert());
        } else {
            FRC.useCustomCompressor(forceDisable.or(pressureSwitch), 1);
        }
    }

    private static void setupPressureLogger(final FloatInput percentPressure) {
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
        FRC.startAuto.send(report);
        FRC.startTele.send(report);
        FRC.startDisabled.send(report);
        new Ticker(10000).send(report);
    }
}
