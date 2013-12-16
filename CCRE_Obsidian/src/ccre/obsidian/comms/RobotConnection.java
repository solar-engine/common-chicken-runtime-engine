/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian.comms;

import ccre.chan.BooleanInput;
import ccre.chan.BooleanOutput;
import ccre.chan.BooleanStatus;
import ccre.ctrl.Ticker;
import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.obsidian.ObsidianCore;
import com.rapplogic.xbee.api.XBeeException;

/**
 *
 * @author millerv
 */
public class RobotConnection {

    // This is the one with the big antenna.
    public static final int[] addr1 = new int[]{0x00, 0x13, 0xA2, 0x00, 0x40, 0xA1, 0x8F, 0x1B};
    // The pathetic one with the wire antenna.
    public static final int[] addr2 = new int[]{0x00, 0x13, 0xA2, 0x00, 0x40, 0xA8, 0xC4, 0x10};

    public static BooleanInput enabled = new BooleanStatus();

    public static boolean alive = false;
    private static XBeeRadio radio;
    private static EventSource heartbeat;
    private static long timeSinceBeat = System.currentTimeMillis();

    public static void startConnection(String port, int baud, boolean verified, boolean beta, final ObsidianCore notify) {
        radio = new XBeeRadio(port, baud, verified);
        try {
            radio.open();
            Logger.log(LogLevel.INFO, "Found radio");
        } catch (XBeeException e) {
            Logger.log(LogLevel.WARNING, "Could not connect", e);
        }
        ObsidianCommsNode.createGlobalNode(false, radio);

        heartbeat = ObsidianCommsNode.globalNode.createEventSource(CommsID.ID_HEARTBEAT);
        enabled = ObsidianCommsNode.globalNode.createBooleanInput(CommsID.ID_ENABLED);

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
                if (System.currentTimeMillis() - timeSinceBeat > 1000 && alive) {
                    notify.disabled.eventFired();
                    alive = false;
                    Logger.info("dead");
                }
            }
        });
    }
}
