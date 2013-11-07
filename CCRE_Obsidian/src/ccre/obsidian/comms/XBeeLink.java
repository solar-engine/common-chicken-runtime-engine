/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ccre.obsidian.comms;

import ccre.cluck.CluckLink;
import ccre.cluck.CluckNode;

/**
 *
 * @author MillerV
 */
public class XBeeLink implements CluckLink {
    private CluckNode node;
    private String linkName;
    
    @Override
    public boolean transmit(String rest, String source, byte[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
