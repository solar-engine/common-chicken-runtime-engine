/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ccre.obsidian;

import ccre.cluck.CluckGlobals;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.obsidian.comms.XBeeLink;
import ccre.obsidian.comms.XBeeRadio;
import com.rapplogic.xbee.api.XBeeException;

/**
 *
 * @author millerv
 */
public class ConnectionInitializer {
    // This is the one with the big antenna.
    public static final int[] addr1 = new int[]{0x00, 0x13, 0xA2, 0x00, 0x40, 0xA1, 0x8F, 0x1B};
    
    // The pathetic one with the wire antenna.
    public static final int[] addr2 = new int[]{0x00, 0x13, 0xA2, 0x00, 0x40, 0xA8, 0xC4, 0x10};
    
    public static void startConnection(String port, int baud, boolean verified) {
        CluckGlobals.ensureInitializedCore();
        XBeeRadio radio = new XBeeRadio(port, baud, verified);
        try {
            radio.open();
            Logger.log(LogLevel.INFO, "Found radio");
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not connect", e);
        }
        XBeeLink link = new XBeeLink(radio, addr2, "hub", CluckGlobals.node,1000, 1000*10);
        link.addToNode();
    }
}
