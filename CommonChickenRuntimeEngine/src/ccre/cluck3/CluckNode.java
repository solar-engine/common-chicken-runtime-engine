/*
 * Copyright 2013 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package ccre.cluck3;

import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.util.CHashMap;
import ccre.workarounds.ThrowablePrinter;

public class CluckNode {

    public final CHashMap<String, CluckLink> links = new CHashMap<String, CluckLink>();

    public void transmit(String target, String source, byte[] data) {
        if (target == null) {
            Logger.log(LogLevel.WARNING, "Received message addressed to unreceving node (source: " + source + ")", new Exception("Embedded Traceback"));
            return;
        } else if (target.equals("*")) {
            // Broadcast
            for (String key : links) {
                CluckLink cl = links.get(key);
                cl.transmit("*", source, data);
            }
            return;
        }
        int t = target.indexOf('/');
        String base, rest;
        if (t == -1) {
            base = target;
            rest = null;
        } else {
            base = target.substring(0, t);
            rest = target.substring(t + 1);
        }
        CluckLink link = links.get(base);
        if (!link.transmit(rest, source, data)) {
            links.put(base, null);
        }
    }

    public String getLinkName(CluckNullLink link) {
        if (link == null) {
            throw new NullPointerException();
        }
        for (String key : links) {
            if (links.get(key) == link) {
                return key;
            }
        }
        throw new RuntimeException("No such link!");
    }

    public void addLink(CluckLink link, String linkName) {
        if (links.get(linkName) != null) {
            throw new IllegalStateException("Link name already used!");
        }
        links.put(linkName, link);
    }

    public void addOrReplaceLink(CluckLink link, String linkName) {
        if (links.get(linkName) != null) {
            Logger.fine("Replaced current link on: " + linkName);
        }
        links.put(linkName, link);
    }

    public void publish(String name, CluckPublishable pub) {
        pub.publish(name, this);
    }
    public static final byte RMT_PING = 0;
    public static final byte RMT_EVENTCONSUMER = 1;
    public static final byte RMT_EVENTSOURCE = 2;
    public static final byte RMT_EVENTSOURCERESP = 3;
    public static final byte RMT_LOGTARGET = 3;

    public void publish(String name, final EventConsumer consum) {
        new CluckSubscriber() {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_EVENTCONSUMER)) {
                    consum.eventFired();
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_EVENTCONSUMER);
            }
        }.attach(this, name);
    }

    public EventConsumer subscribeEC(final String path) {
        return new EventConsumer() {
            public void eventFired() {
                transmit(path, null, new byte[]{RMT_EVENTCONSUMER});
            }
        };
    }
    private static Object empty = new Object();

    public void publish(final String name, EventSource source) {
        final CHashMap<String, Object> remotes = new CHashMap<String, Object>();
        source.addListener(new EventConsumer() {
            public void eventFired() {
                for (String remote : remotes) {
                    transmit(remote, name, new byte[]{RMT_EVENTSOURCERESP});
                }
            }
        });
        new CluckSubscriber() {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_EVENTSOURCE)) {
                    remotes.put(src, empty);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_EVENTSOURCE);
            }
        }.attach(this, name);
    }
    private static int localIDs = 0;

    public EventSource subscribeES(final String path) {
        final String linkName = "src-" + Long.toHexString(System.nanoTime() & 0xFFFF) + "-" + localIDs++;
        final Event e = new Event() {
            private boolean sent = false; // TODO: What if the remote end gets rebooted? Would this re-send the request?

            @Override
            public boolean addListener(EventConsumer cns) {
                boolean out = super.addListener(cns);
                if (!sent) {
                    sent = true;
                    transmit(path, linkName, new byte[]{RMT_EVENTSOURCE});
                }
                return out;
            }
        };
        new CluckSubscriber() {
            @Override
            protected void receive(String src, byte[] data) {
                if (requireRMT(src, data, RMT_EVENTSOURCERESP)) {
                    e.produce();
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }
        }.attach(this, linkName);
        return e;
    }

    public void publish(String name, final LoggingTarget lt) {
        new CluckSubscriber() {
            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, RMT_LOGTARGET)) {
                    int l1, l2;
                    if (data.length < 10) {
                        Logger.warning("Not enough data to Logging Target!");
                        return;
                    }
                    l1 = (data[2] << 24) | (data[3] << 16) | (data[4] << 8) | data[5];
                    l2 = (data[6] << 24) | (data[7] << 16) | (data[8] << 8) | data[9];
                    if (l1 + l2 + 10 != data.length) {
                        Logger.warning("Bad data length to Logging Target!");
                        if (l1 + l2 + 10 > data.length) {
                            if (l1 + 10 <= data.length) {
                                l2 = 0; // Just keep the 'message', in case it's helpful, and is all there.
                            } else {
                                return;
                            }
                        }
                    }
                    String message = new String(data, 10, l1);
                    String extended;
                    if (l2 == 0) {
                        extended = null;
                    } else {
                        extended = new String(data, 10 + l1, l2);
                    }
                    lt.log(LogLevel.fromByte(data[1]), message, extended);
                }
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                defaultBroadcastHandle(source, data, RMT_LOGTARGET);
            }
        }.attach(this, name);
    }

    public LoggingTarget subscribeLT(final String path, final LogLevel minimum) {
        return new LoggingTarget() {
            public void log(LogLevel level, String message, Throwable throwable) {
                log(level, message, ThrowablePrinter.toStringThrowable(throwable));
            }

            public void log(LogLevel level, String message, String extended) {
                if (level.atLeastAsImportant(minimum)) {
                    byte[] msg = message.getBytes();
                    byte[] ext = extended == null ? new byte[0] : extended.getBytes();
                    byte[] out = new byte[10 + msg.length + ext.length];
                    out[0] = RMT_LOGTARGET;
                    out[1] = LogLevel.toByte(level);
                    int lm = msg.length;
                    out[2] = (byte) (lm >> 24);
                    out[3] = (byte) (lm >> 16);
                    out[4] = (byte) (lm >> 8);
                    out[5] = (byte) (lm);
                    int le = ext.length;
                    out[6] = (byte) (le >> 24);
                    out[7] = (byte) (le >> 16);
                    out[8] = (byte) (le >> 8);
                    out[9] = (byte) (le);
                    System.arraycopy(msg, 0, out, 10, msg.length);
                    System.arraycopy(ext, 0, out, 10 + msg.length, ext.length);
                    transmit(path, null, out);
                }
            }
        };
    }
}
