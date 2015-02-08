package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;

public abstract class AbstractJoystick implements IJoystickWithPOV {

    protected final EventInput check;

    public AbstractJoystick(EventInput check) {
        this.check = check;
    }

    public FloatInputPoll getXChannel() {
        return getAxisChannel(1);
    }

    public FloatInputPoll getYChannel() {
        return getAxisChannel(2);
    }

    public EventInput getButtonSource(int id) {
        return BooleanMixing.whenBooleanBecomes(getButtonChannel(id), true, check);
    }

    public FloatInput getAxisSource(int axis) {
        return FloatMixing.createDispatch(getAxisChannel(axis), check);
    }

    public FloatInput getXAxisSource() {
        return getAxisSource(1);
    }

    public FloatInput getYAxisSource() {
        return getAxisSource(2);
    }

    public BooleanInput isPOVPressedSource(int id) {
        return BooleanMixing.createDispatch(isPOVPressed(id), check);
    }

    public FloatInput getPOVAngleSource(int id) {
        return FloatMixing.createDispatch(getPOVAngle(id), check);
    }
}