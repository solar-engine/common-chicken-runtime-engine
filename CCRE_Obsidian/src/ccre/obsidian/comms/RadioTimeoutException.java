/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian.comms;

/**
 *
 * @author MillerV
 */
public class RadioTimeoutException extends Exception {
    public RadioTimeoutException(String msg) {
        super(msg);
    }
    
    public RadioTimeoutException() {
        super();
    }
}
