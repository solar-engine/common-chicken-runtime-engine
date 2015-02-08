package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;

public interface IJoystickWithPOV extends IJoystick {
    public BooleanInputPoll isPOVPressed(int id);

    public FloatInputPoll getPOVAngle(int id);

    public BooleanInput isPOVPressedSource(int id);

    public FloatInput getPOVAngleSource(int id);
}
