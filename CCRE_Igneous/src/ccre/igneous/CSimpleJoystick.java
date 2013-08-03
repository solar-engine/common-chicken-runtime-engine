package ccre.igneous;

import ccre.chan.BooleanInputPoll;
import ccre.chan.FloatInputPoll;
import ccre.ctrl.ISimpleJoystick;
import edu.wpi.first.wpilibj.Joystick;

/**
 * An ISimpleJoystick implementation that allows reading from a joystick on the
 * driver station.
 *
 * @author skeggsc
 */
class CSimpleJoystick implements ISimpleJoystick {

    /**
     * The joystick object that is read from.
     */
    protected Joystick joy;

    /**
     * Create a CSimpleJoystick that reads from the specified joystick index.
     *
     * @param joystick the joystick ID, from 1 to 4, inclusive.
     */
    CSimpleJoystick(int joystick) {
        joy = new Joystick(joystick);
    }

    public FloatInputPoll getAxisChannel(final int axis) {
        return new FloatInputPoll() {
            public float readValue() {
                return (float) joy.getRawAxis(axis);
            }
        };
    }

    public FloatInputPoll getXChannel() {
        return new FloatInputPoll() {
            public float readValue() {
                return (float) joy.getX();
            }
        };
    }

    public FloatInputPoll getYChannel() {
        return new FloatInputPoll() {
            public float readValue() {
                return (float) joy.getY();
            }
        };
    }

    public BooleanInputPoll getButtonChannel(final int button) {
        return new BooleanInputPoll() {
            public boolean readValue() {
                return joy.getRawButton(button);
            }
        };
    }
}
