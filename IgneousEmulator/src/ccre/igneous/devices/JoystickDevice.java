package ccre.igneous.devices;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.IJoystick;
import ccre.igneous.DeviceGroup;

public class JoystickDevice extends DeviceGroup implements IJoystick {

    private FloatControlDevice[] axes = new FloatControlDevice[6];

    public JoystickDevice(String name) {
        add(new HeadingDevice(name));
    }
    
    public JoystickDevice(int id) {
        add(new HeadingDevice("Joystick " + id));
    }
    
    private FloatControlDevice getAxis(int id) {
        if (id < 1 || id > axes.length) {
            throw new IllegalArgumentException("Invalid axis number: " + id);
        }
        if (axes[id-1] == null) {
            axes[id-1] = new FloatControlDevice("Axis " + id);
            add(axes[id-1]);
        }
        return axes[id-1];
    }
    
    public EventInput getButtonSource(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    public FloatInput getAxisSource(int axis) {
        return getAxis(axis);
    }

    public FloatInputPoll getAxisChannel(int axis) {
        return getAxis(axis);
    }

    public BooleanInputPoll getButtonChannel(int button) {
        // TODO Auto-generated method stub
        return null;
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

}
