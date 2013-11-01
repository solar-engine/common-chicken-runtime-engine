/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ccre.obsidian;

import ccre.chan.BooleanInputPoll;

/**
 *
 * @author millerv
 */
public class EmulatorGPIOInput implements BooleanInputPoll {
    private final EmulatorPin pin;
    
    public EmulatorGPIOInput(EmulatorPin pin) {
        this.pin = pin;
    }
    
    @Override
    public boolean readValue() {
        if (pin.getMode() == EmulatorPin.Mode.GPIO_IN) {
            return pin.getBoolean();
        } else {
            throw new UnsupportedOperationException("Cannot read boolean in mode: " + pin.getMode().name());
        }
    }
}
