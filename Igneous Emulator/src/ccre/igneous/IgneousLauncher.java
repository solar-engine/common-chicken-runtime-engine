package ccre.igneous;

import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.ctrl.IDispatchJoystick;
import ccre.ctrl.ISimpleJoystick;
import ccre.event.EventSource;

/**
 * This is a launcher for an Igneous application. The reason for this is so that
 * the main program can be ran without a robot. Documentation for all the
 * methods here can be found in IgneousCore, where they are invoked.
 *
 * @author skeggsc
 */
public interface IgneousLauncher {

    public ISimpleJoystick makeSimpleJoystick(int id);

    public IDispatchJoystick makeDispatchJoystick(int id, EventSource source);

    public FloatOutput makeJaguar(int id, boolean negate);

    public FloatOutput makeVictor(int id, boolean negate);

    public FloatOutput makeTalon(int id, boolean negate);

    public BooleanOutput makeSolenoid(int id);

    public FloatInputPoll makeAnalogInput(int id, int averageBits);

    public FloatInputPoll makeAnalogInput_ValuedBased(int id, int averageBits);

    public BooleanInputPoll makeDigitalInput(int id);

    public FloatOutput makeServo(int id, float minInput, float maxInput);

    public FloatOutput makeDSFloatReadout(String prefix, int line);

    public BooleanInputPoll getIsDisabled();

    public BooleanInputPoll getIsAutonomous();

    public void useCustomCompressor(BooleanInputPoll shouldDisable, int compressorRelayChannel);
}
