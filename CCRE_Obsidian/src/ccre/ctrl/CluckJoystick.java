/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ccre.ctrl;

import ccre.chan.BooleanInput;
import ccre.chan.BooleanInputPoll;
import ccre.chan.FloatInput;
import ccre.chan.FloatInputPoll;
import ccre.cluck.CluckGlobals;
import ccre.event.EventSource;

/**
 *
 * @author MillerV
 */
public class CluckJoystick implements IDispatchJoystick {
    @Override
    public EventSource getButtonSource(int id) {
        BooleanInput input = CluckGlobals.node.subscribeBIP("joystick" + 1 + "-button" + id);
        EventSource es;
        return null;
    }

    @Override
    public FloatInput getAxisSource(int axis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FloatInputPoll getAxisChannel(int axis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FloatInputPoll getXChannel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FloatInputPoll getYChannel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BooleanInputPoll getButtonChannel(int button) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
