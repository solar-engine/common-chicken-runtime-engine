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
