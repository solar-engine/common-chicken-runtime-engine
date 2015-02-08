package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;

/**
 * A joystick with axes, buttons, and POV hats.
 *
 * @author skeggsc
 */
public interface IJoystickWithPOV extends IJoystick {
    /**
     * Get a channel representing whether or not the specified POV hat (starting
     * at one) is pressed.
     * 
     * @param id the POV hat id.
     * @return the channel representing whether or not the POV hat is pressed.
     */
    public BooleanInputPoll isPOVPressed(int id);

    /**
     * Get a channel representing the angle of the POV hat, if it is pressed.
     * 
     * The angle is in degrees from 0 to 360, with zero at the top and
     * increasing clockwise, at least on our tested models.
     * 
     * If not pressed (as determined by isPOVPressed), the value is undefined.
     * It can be, but is not necessarily, outside the range of 0-360.
     * 
     * @param id the POV hat id.
     * @return the channel representing the POV's angle.
     */
    public FloatInputPoll getPOVAngle(int id);

    /**
     * Get a source representing whether or not the specified POV hat (starting
     * at one) is pressed.
     * 
     * @param id the POV hat id.
     * @return the source representing whether or not the POV hat is pressed.
     */
    public BooleanInput isPOVPressedSource(int id);

    /**
     * Get a source representing the angle of the POV hat, if it is pressed.
     * 
     * The angle is in degrees from 0 to 360, with zero at the top and
     * increasing clockwise, at least on our tested models.
     * 
     * If not pressed (as determined by isPOVPressedSource), the value is
     * undefined. It can be, but is not necessarily, outside the range of 0-360.
     * 
     * @param id the POV hat id.
     * @return the source representing the POV's angle.
     */
    public FloatInput getPOVAngleSource(int id);
}
