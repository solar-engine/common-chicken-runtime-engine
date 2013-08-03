package ccre.ctrl;

import ccre.chan.FloatInput;
import ccre.event.EventSource;

/**
 * A joystick that provides asynchronous triggers for its inputs, instead of
 * pollable values.
 *
 * @see ISimpleJoystick
 * @author skeggsc
 */
public interface IDispatchJoystick extends ISimpleJoystick {

    /**
     * Get an EventSource that will be fired when the given button is pressed.
     *
     * @param id the button ID.
     * @return the EventSource representing the button being pressed.
     */
    public EventSource getButtonSource(int id);

    /**
     * Get a FloatInput that represents the given axis.
     *
     * @param axis the axis ID.
     * @return the FloatInput representing the axis.
     */
    public FloatInput getAxisSource(int axis);
}
