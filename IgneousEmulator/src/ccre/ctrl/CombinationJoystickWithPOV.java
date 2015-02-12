package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;

public class CombinationJoystickWithPOV implements IJoystickWithPOV {

    private IJoystickWithPOV a;
    private final IJoystickWithPOV b;
    
    public void setAlphaJoystick(IJoystickWithPOV alpha) {
        a = alpha;
    }

    public boolean hasAlphaJoystick() {
        return a != null;
    }

    public CombinationJoystickWithPOV(IJoystickWithPOV a, IJoystickWithPOV b) {
        this.a = a;
        this.b = b;
    }
    
    public EventInput getButtonSource(int id) {
        return EventMixing.combine(a.getButtonSource(id), b.getButtonSource(id));
    }

    public FloatInput getAxisSource(int axis) {
        return FloatMixing.addition.of(a.getAxisSource(axis), b.getAxisSource(axis));
    }

    public FloatInputPoll getAxisChannel(int axis) {
        return FloatMixing.addition.of(a.getAxisChannel(axis), b.getAxisChannel(axis));
    }

    public BooleanInputPoll getButtonChannel(int button) {
        return BooleanMixing.xorBooleans(a.getButtonChannel(button), b.getButtonChannel(button));
    }

    public FloatInputPoll getXChannel() {
        return getAxisChannel(1);
    }

    public FloatInputPoll getYChannel() {
        return getAxisChannel(2);
    }

    public FloatInput getXAxisSource() {
        return getAxisSource(1);
    }

    public FloatInput getYAxisSource() {
        return getAxisSource(2);
    }

    public BooleanInputPoll isPOVPressed(int id) {
        return BooleanMixing.orBooleans(a.isPOVPressed(id), b.isPOVPressed(id));
    }

    public FloatInputPoll getPOVAngle(int id) {
        return Mixing.select(a.isPOVPressed(id), b.getPOVAngle(id), a.getPOVAngle(id));
    }

    public BooleanInput isPOVPressedSource(int id) {
        return BooleanMixing.orBooleans(a.isPOVPressedSource(id), b.isPOVPressedSource(id));
    }

    public FloatInput getPOVAngleSource(int id) {
        BooleanInput useA = a.isPOVPressedSource(id);
        FloatInput aa = a.getPOVAngleSource(id);
        FloatInput ba = b.getPOVAngleSource(id);
        return FloatMixing.createDispatch(getPOVAngle(id), EventMixing.combine(FloatMixing.onUpdate(aa), FloatMixing.onUpdate(ba), BooleanMixing.whenBooleanChanges(useA)));
    }
}
