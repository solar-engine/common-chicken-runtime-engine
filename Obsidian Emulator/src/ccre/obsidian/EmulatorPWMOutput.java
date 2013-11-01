/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ccre.obsidian;

import ccre.chan.FloatOutput;

/**
 *
 * @author millerv
 */
public class EmulatorPWMOutput implements FloatOutput {
    private final EmulatorPin pin;
    
    public EmulatorPWMOutput(EmulatorPin pin) {
        this.pin = pin;
    }

    @Override
    public void writeValue(float value) {
        if (pin.getMode() == EmulatorPin.Mode.PWM) {
            pin.set(value);
        } else {
            throw new UnsupportedOperationException("Cannot write float in mode: " + pin.getMode().name());
        }
    }
    
}
