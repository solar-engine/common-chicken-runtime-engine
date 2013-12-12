/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian.comms;

import ccre.chan.BooleanOutput;
import ccre.chan.BooleanStatus;
import ccre.cluck.CluckGlobals;
import ccre.cluck.CluckLink;
import ccre.ctrl.Ticker;
import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.obsidian.ObsidianCore;
import com.rapplogic.xbee.api.XBeeException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 *
 * @author millerv
 */
public class RobotConnection {

    // This is the one with the big antenna.
    public static final int[] addr1 = new int[]{0x00, 0x13, 0xA2, 0x00, 0x40, 0xA1, 0x8F, 0x1B};
    // The pathetic one with the wire antenna.
    public static final int[] addr2 = new int[]{0x00, 0x13, 0xA2, 0x00, 0x40, 0xA8, 0xC4, 0x10};
    public static final BooleanStatus enabled;

    static {
        enabled = new BooleanStatus();
        enabled.writeValue(false);
    }
    public static boolean alive = false;
    private static XBeeRadio radio;
    private static Event heartbeat;
    private static long timeSinceBeat = System.currentTimeMillis();

    public static void testConnection() throws XBeeException {
        ByteBuffer bout = ByteBuffer.allocate(2);
        bout.putShort((short) (-1));
        int[] outarray = new int[bout.position()];
        for (int i = 0; i < outarray.length; i++) {
            outarray[i] = bout.get(i);
        }
        radio.sendPacketUnverified(addr2, outarray);
    }

    public static void startConnection(String port, int baud, boolean verified, final ObsidianCore notify) {
        heartbeat = new Event();
        
        enabled.addTarget(new BooleanOutput() {
            @Override
            public void writeValue(boolean b) {
                if (b && alive) {
                    notify.enabled.eventFired();
                    alive = true;
                    Logger.info("alive");
                } else if (!b) {
                    notify.disabled.eventFired();
                    alive = false;
                    Logger.info("dead");
                }
            }
        });
        
        heartbeat.addListener(new EventConsumer() {
            @Override
            public void eventFired() {
                timeSinceBeat = System.currentTimeMillis();
                if (!alive && enabled.readValue()) {
                    notify.enabled.eventFired();
                    alive = true;
                    Logger.info("alive");
                }
            }
        });

        new Ticker(20).addListener(new EventConsumer() {
            @Override
            public void eventFired() {
                if (System.currentTimeMillis() - timeSinceBeat > 2000 && alive) {
                    notify.disabled.eventFired();
                    alive = false;
                    Logger.info("dead");
                }
            }
        });
        CluckGlobals.ensureInitializedCore();
        radio = new XBeeRadio(port, baud, verified);
        try {
            radio.open();
            Logger.log(LogLevel.INFO, "Found radio");
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not connect", e);
        }
        XBeeLink link = new XBeeLink(radio, addr2, "hub", CluckGlobals.node, 1000, 1000 * 10);
        link.addToNode();
        CluckGlobals.node.publish("beat", (EventConsumer)heartbeat);
        CluckGlobals.node.publish("enable", (BooleanOutput)enabled);
    }
}
