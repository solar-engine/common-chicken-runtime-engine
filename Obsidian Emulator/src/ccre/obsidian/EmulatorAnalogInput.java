/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ccre.obsidian;

import ccre.chan.FloatInputPoll;

/**
 *
 * @author millerv
 */
public class EmulatorAnalogInput implements FloatInputPoll {
    private final EmulatorPin pin;
    
    public EmulatorAnalogInput(EmulatorPin pin) {
        this.pin = pin;
    }

    @Override
    public float readValue() {
        if (pin.getMode() == EmulatorPin.Mode.ANALOG_IN) {
            return pin.getFloat();
        } else {
            throw new UnsupportedOperationException("Cannot read float in mode: " + pin.getMode().name());
        }
    }
}
