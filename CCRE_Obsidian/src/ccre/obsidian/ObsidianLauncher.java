/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian;

import ccre.obsidian.comms.Radio;
import ccre.chan.*;

/**
 * A simple interface for a launcher which gives access to GPIO, PWM, and 
 * Analog I/O. 
 * 
 * @author MillerV
 */
public interface ObsidianLauncher {
    public BooleanOutput makeGPIOOutput(int chan, boolean defaultValue);
    
    public BooleanInputPoll makeGPIOInput(int chan, boolean pullSetting);
    
    public FloatOutput makePWMOutput(String chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity);
    
    public void destroyPWMOutput(String chan);
    
    public FloatInputPoll makeAnalogInput(int chan);
    
    public Radio makeRadio(int usb);
}
