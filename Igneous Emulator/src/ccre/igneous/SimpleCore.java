package ccre.igneous;

import ccre.ctrl.IDispatchJoystick;

/**
 * Provides a wrapper over IgneousCore that provides the joysticks as
 * easy-to-access objects.
 *
 * @author skeggsc
 */
public abstract class SimpleCore extends IgneousCore {

    /**
     * The first joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick1;
    /**
     * The second joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick2;
    /**
     * The third joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick3;
    /**
     * The fourth joystick attached to the driver station.
     */
    protected IDispatchJoystick joystick4;

    /**
     * Implement this method - it should set up everything that your robot needs to do.
     */
    protected abstract void createSimpleControl();

    /**
     * Sets up the joysticks and then calls createSimpleControl.
     */
    protected final void createRobotControl() {
        joystick1 = makeDispatchJoystick(1);
        joystick2 = makeDispatchJoystick(2);
        joystick3 = makeDispatchJoystick(3);
        joystick4 = makeDispatchJoystick(4);
        createSimpleControl();
    }
}
