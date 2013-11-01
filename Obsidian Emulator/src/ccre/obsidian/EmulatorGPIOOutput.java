/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ccre.obsidian;

import ccre.chan.BooleanOutput;

/**
 *
 * @author millerv
 */
public class EmulatorGPIOOutput implements BooleanOutput {
    private final EmulatorPin pin;
    
    public EmulatorGPIOOutput(EmulatorPin pin) {
        this.pin = pin;
    }

    @Override
    public void writeValue(boolean val) {
        if (pin.getMode() == EmulatorPin.Mode.GPIO_OUT) {
            pin.set(val);
        } else {
            throw new UnsupportedOperationException("Cannot write boolean in mode: " + pin.getMode().name());
        }
    }
}
