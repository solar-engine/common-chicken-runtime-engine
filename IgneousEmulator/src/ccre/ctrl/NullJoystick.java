package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;

public class NullJoystick implements IJoystickWithPOV {

    public EventInput getButtonSource(int id) {
        return EventMixing.never;
    }

    public FloatInput getAxisSource(int axis) {
        return FloatMixing.always(0);
    }

    public FloatInputPoll getAxisChannel(int axis) {
        return FloatMixing.always(0);
    }

    public BooleanInputPoll getButtonChannel(int button) {
        return BooleanMixing.alwaysFalse;
    }

    public FloatInputPoll getXChannel() {
        return FloatMixing.always(0);
    }

    public FloatInputPoll getYChannel() {
        return FloatMixing.always(0);
    }

    public FloatInput getXAxisSource() {
        return FloatMixing.always(0);
    }

    public FloatInput getYAxisSource() {
        return FloatMixing.always(0);
    }

    public BooleanInputPoll isPOVPressed(int id) {
        return BooleanMixing.alwaysFalse;
    }

    public FloatInputPoll getPOVAngle(int id) {
        return FloatMixing.always(0);
    }

    public BooleanInput isPOVPressedSource(int id) {
        return BooleanMixing.alwaysFalse;
    }

    public FloatInput getPOVAngleSource(int id) {
        return FloatMixing.always(0);
    }

}
