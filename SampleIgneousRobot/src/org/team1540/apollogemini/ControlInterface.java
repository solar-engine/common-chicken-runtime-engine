package org.team1540.apollogemini;

import ccre.channel.*;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;
import ccre.holders.TuningContext;
import ccre.phidget.PhidgetReader;

public class ControlInterface {

    private final IJoystick joystick1, joystick2;
    private static final FloatFilter driveDeadzone = FloatMixing.deadzone(.1f);
    private final EventStatus forceArmLower = new EventStatus();
    private final EventInput globalPeriodic, robotDisabled;

    public ControlInterface(IJoystick joystick1, IJoystick joystick2, EventInput globalPeriodic, EventInput robotDisabled) {
        this.joystick1 = joystick1;
        this.joystick2 = joystick2;
        this.globalPeriodic = globalPeriodic;
        this.robotDisabled = robotDisabled;
    }

    public EventInput getRearmCatapult() {
        return EventMixing.combine(joystick2.getButtonSource(1), BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(2), true, globalPeriodic));
    }

    public EventInput getFireButton(BooleanInputPoll isKidMode) {
        return EventMixing.combine(new EventInput[] { BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(1), true), EventMixing.filterEvent(isKidMode, false, joystick1.getButtonSource(6)), joystick2.getButtonSource(2) });
    }

    public EventInput getArmRaise() {
        return EventMixing.combine(new EventInput[] { robotDisabled, joystick2.getButtonSource(5), BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(5), true) });
    }

    public EventInput getArmHold() {
        return EventMixing.combine(joystick2.getButtonSource(7), BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(7), true));
    }

    public EventInput getArmLower() {
        return EventMixing.combine(new EventInput[] { forceArmLower, joystick2.getButtonSource(6), BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(0), true) });
    }

    public BooleanInputPoll rollerIn() {
        return BooleanMixing.orBooleans(PhidgetReader.getDigitalInput(3), FloatMixing.floatIsAtLeast(joystick2.getAxisChannel(2), 0.2f));
    }

    public BooleanInputPoll rollerOut() {
        return BooleanMixing.orBooleans(PhidgetReader.getDigitalInput(4), FloatMixing.floatIsAtMost(joystick2.getAxisChannel(2), -0.2f));
    }

    public FloatInputPoll getSlider() {
        final TuningContext tuner = new TuningContext(Cluck.getNode(), "PowerSliderTuner");
        final FloatInput min = tuner.getFloat("Slider Min", 0f);
        final FloatInput max = tuner.getFloat("Slider Max", 1f);
        final FloatInput ai = PhidgetReader.getAnalogInput(4);
        return new FloatInputPoll() {
            public float get() {
                return normalize(min.get(), max.get(), ai.get());
            }
        };
    }

    public BooleanOutput showArmUp() {
        return PhidgetReader.getDigitalOutput(1);
    }

    public BooleanOutput showArmDown() {
        return PhidgetReader.getDigitalOutput(0);
    }

    public void showFiring(BooleanInput canFire) {
        canFire.send(PhidgetReader.getDigitalOutput(2));
    }

    public void displayPressureAndWinch(final FloatInputPoll level, EventInput update, final BooleanInputPoll cprSwitch, final FloatInputPoll winchValue) {
        update.send(new EventOutput() {
            int prevValue = -1000;
            int prevWinchValue = -1000;
            boolean prevValueCpr = cprSwitch.get();
            int ctr = 0;

            public void event() {
                int c = (int) level.get();
                int winch = (int) (winchValue.get());
                boolean cpr = cprSwitch.get();
                if (c == prevValue && (prevValueCpr == cpr) && prevWinchValue == winch && (ctr++ % 100 != 0)) {
                    return;
                }
                prevValue = c;
                prevValueCpr = cpr;
                prevWinchValue = winch;
                String mstr = c <= -10 ? "????" : Integer.toString(c) + "%";
                while (mstr.length() < 4) {
                    mstr = " " + mstr;
                }
                PhidgetReader.getLCDLine(1).println("AIR " + (cpr ? "<" : " ") + mstr + (cpr ? ">" : " ") + " WNCH " + Float.toString(winch));
            }
        });
    }

    public static float normalize(float zero, float one, float value) {
        float range = one - zero;
        return ((value - zero) / range);
    }

    public FloatInputPoll getLeftDriveAxis() {
        return driveDeadzone.wrap(FloatMixing.negate(joystick1.getAxisChannel(2)));
    }

    public FloatInputPoll getRightDriveAxis() {
        return driveDeadzone.wrap(FloatMixing.negate(joystick1.getAxisChannel(6)));
    }

    public FloatInputPoll getForwardDriveAxis() {
        final FloatInputPoll plus = joystick1.getAxisChannel(3), minus = joystick1.getAxisChannel(4);
        return driveDeadzone.wrap(FloatMixing.negate(new FloatInputPoll() {
            public float get() {
                return plus.get() - minus.get();
            }
        }));
    }

    public EventInput getShiftHighButton() {
        return joystick1.getButtonSource(1);
    }

    public EventInput getShiftLowButton() {
        return joystick1.getButtonSource(3);
    }

    public BooleanInputPoll getToggleDisabled() {
        return BooleanMixing.orBooleans(joystick1.getButtonChannel(7), joystick1.getButtonChannel(8));
    }

    public EventOutput forceArmLower() {
        return forceArmLower;
    }

    public BooleanInputPoll shouldBeCollectingBecauseLoader() {
        ExpirationTimer exp = new ExpirationTimer();
        exp.startWhen(BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(0), true));
        BooleanStatus runCollector = new BooleanStatus();
        exp.scheduleBooleanPeriod(10, 510, runCollector, true);
        exp.schedule(520, exp.getStopEvent());

        ExpirationTimer exp2 = new ExpirationTimer();
        exp2.startWhen(BooleanMixing.whenBooleanBecomes(PhidgetReader.getDigitalInput(7), true));
        exp2.scheduleBooleanPeriod(10, 1010, runCollector, true);
        exp2.schedule(1520, exp2.getStopEvent());
        return runCollector;
    }
}
