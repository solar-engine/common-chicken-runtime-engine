package ccre.ctrl;

import ccre.chan.BooleanInputPoll;
import ccre.chan.FloatInputPoll;

/**
 * A joystick that provides pollable channels for all of its inputs.
 *
 * @see IDispatchJoystick
 * @author skeggsc
 */
public interface ISimpleJoystick {

    /**
     * Get a FloatInputPoll representing the state of the specified axis on this
     * joystick.
     *
     * @param axis the axis ID.
     * @return the FloatInputPoll representing the status of the axis.
     */
    public FloatInputPoll getAxisChannel(int axis);

    /**
     * Get a FloatInputPoll for the X axis.
     *
     * @return the FloatInputPoll representing the status of the X axis.
     */
    public FloatInputPoll getXChannel();

    /**
     * Get a FloatInputPoll for the Y axis.
     *
     * @return the FloatInputPoll representing the status of the Y axis.
     */
    public FloatInputPoll getYChannel();

    /**
     * Get a BooleanInputPoll representing whether or not the given button is
     * pressed.
     *
     * @param button the button ID.
     * @return the BooleanInputPoll representing if the given button is pressed.
     */
    public BooleanInputPoll getButtonChannel(int button);
}
