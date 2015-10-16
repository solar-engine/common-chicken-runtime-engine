/*
 * Copyright 2015 Colby Skeggs
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
package ccre.cluck;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingEventOutput;
import ccre.util.Values;

public class CluckSubscriberTest {

    private CluckNode node;
    private CountingEventOutput ceo;

    @Before
    public void setUp() throws Exception {
        node = new CluckNode();
        ceo = new CountingEventOutput();
    }

    @After
    public void tearDown() throws Exception {
        ceo = null;
        node = null;
    }

    @Test(expected = NullPointerException.class)
    public void testCluckSubscriberNull() {
        new CluckSubscriber(null) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }

            @Override
            protected void receive(String source, byte[] data) {
            }
        };
    }

    @Test
    public void testSendAlwaysTrue() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }

            @Override
            protected void receive(String source, byte[] data) {
            }
        };
        assertTrue(sub.send(null, null, new byte[] { CluckConstants.RMT_NOTIFY }));
        assertTrue(sub.send(CluckConstants.BROADCAST_DESTINATION, null, new byte[] { CluckConstants.RMT_NOTIFY }));
        assertTrue(sub.send("side-channel", null, new byte[] { CluckConstants.RMT_NOTIFY }));
    }

    @Test(expected = NullPointerException.class)
    public void testAttachNull() {
        new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }

            @Override
            protected void receive(String source, byte[] data) {
            }
        }.attach(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAttachTwice() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }

            @Override
            protected void receive(String source, byte[] data) {
            }
        };
        sub.attach("test");
        sub.attach("test2");
    }

    @Test(expected = IllegalStateException.class)
    public void testAttachSameTwice() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }

            @Override
            protected void receive(String source, byte[] data) {
            }
        };
        sub.attach("test");
        sub.attach("test");
    }

    @Test
    public void testAttach() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
            }

            @Override
            protected void receive(String source, byte[] data) {
            }
        };
        sub.attach("example-1");
        assertTrue(node.hasLink("example-1"));
        assertEquals("example-1", node.getLinkName(sub));
    }

    @Test
    public void testRequireRMTPing() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE)) {
                    fail();
                }
            }
        };
        sub.attach("example-1");
        VerifyingCluckLink vcl = new VerifyingCluckLink();
        vcl.expectedDestination = null;
        vcl.expectedMessage = new byte[] { CluckConstants.RMT_PING, CluckConstants.RMT_INVOKE };
        vcl.expectedSource = "example-1";
        vcl.ifExpected = true;
        node.addLink(vcl, "bounce");
        sub.send(null, "bounce", new byte[] { CluckConstants.RMT_PING });
        vcl.check();
    }

    @Test
    public void testRequireRMTNACK() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE)) {
                    fail();
                }
            }
        };
        sub.attach("example-1");
        VerifyingCluckLink vcl = new VerifyingCluckLink();
        vcl.ifExpected = false;
        node.addLink(vcl, "bounce");
        sub.send(null, "bounce", new byte[] { CluckConstants.RMT_NEGATIVE_ACK });
        vcl.check();
    }

    @Test
    public void testRequireRMT() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE)) {
                    ceo.event();
                } else {
                    fail();
                }
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        sub.send(null, "bounce", new byte[] { CluckConstants.RMT_INVOKE });
        ceo.check();
    }

    @Test
    public void testRequireRMTEmpty() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE)) {
                    fail();
                } else {
                    ceo.event();
                }
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        // TODO: test logging
        sub.send(null, "bounce", new byte[] {});
        ceo.check();
    }

    @Test
    public void testRequireRMTWrongRMT() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE)) {
                    fail();
                } else {
                    ceo.event();
                }
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        // TODO: test logging
        sub.send(null, "bounce", new byte[] { CluckConstants.RMT_INVOKE_REPLY });
        ceo.check();
    }

    @Test
    public void testRequireRMTPingResponse() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE)) {
                    fail();
                } else {
                    ceo.event();
                }
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        // TODO: test logging
        sub.send(null, "bounce", new byte[] { CluckConstants.RMT_PING, CluckConstants.RMT_INVOKE });
        ceo.check();
    }

    @Test
    public void testRequireRMTInsufficientlyLong() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE, 2)) {
                    fail();
                } else {
                    ceo.event();
                }
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        // TODO: test logging
        sub.send(null, "bounce", new byte[] { CluckConstants.RMT_INVOKE });
        ceo.check();
    }

    @Test
    public void testRequireRMTSufficientlyLong() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                if (requireRMT(source, data, CluckConstants.RMT_INVOKE, 2)) {
                    ceo.event();
                } else {
                    fail();
                }
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        sub.send(null, "bounce", new byte[] { CluckConstants.RMT_INVOKE, 0 });
        ceo.check();
    }

    @Test
    public void testDefaultBroadcastHandle() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveSideChannel(String dest, String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                fail();
            }
        };
        sub.attach("example-1");
        VerifyingCluckLink vcl = new VerifyingCluckLink();
        vcl.expectedDestination = null;
        vcl.expectedSource = "example-1";
        vcl.expectedMessage = new byte[] { CluckConstants.RMT_PING, CluckConstants.RMT_EVENTINPUT };
        node.addLink(vcl, "example-2");
        for (int i = 0; i < 5; i++) {
            vcl.ifExpected = true;
            sub.defaultBroadcastHandle("example-2", new byte[] { CluckConstants.RMT_PING }, CluckConstants.RMT_EVENTINPUT);
            vcl.check();
        }
        for (byte[] ignorableMessage : new byte[][] { { CluckConstants.RMT_PING, CluckConstants.RMT_EVENTINPUT }, { CluckConstants.RMT_INVOKE }, {} }) {
            for (int i = 0; i < 5; i++) {
                vcl.ifExpected = false;
                sub.defaultBroadcastHandle("example-2", ignorableMessage, CluckConstants.RMT_EVENTINPUT);
                vcl.check();
            }
        }
        for (int i = 0; i < 5; i++) {
            vcl.ifExpected = true;
            sub.defaultBroadcastHandle("example-2", new byte[] { CluckConstants.RMT_PING }, CluckConstants.RMT_EVENTINPUT);
            vcl.check();
        }
    }

    @Test
    public void testReceive() {
        byte[] expected = Values.getRandomBytes(10);
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveSideChannel(String dest, String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                assertEquals("example-2", source);
                assertArrayEquals(data, expected);
                ceo.event();
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        node.transmit("example-1", "example-2", expected);
        ceo.check();
    }

    @Test(expected = NoSuchElementException.class)
    public void testReceiveError() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveSideChannel(String dest, String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                throw new NoSuchElementException();
            }
        };
        sub.attach("example-1");
        sub.send(null, "example-2", Values.getRandomBytes(10));
    }

    @Test
    public void testReceiveBroadcast() {
        byte[] expected = Values.getRandomBytes(10);
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                assertEquals("example-2", source);
                assertArrayEquals(data, expected);
                ceo.event();
            }

            @Override
            protected void receiveSideChannel(String dest, String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                fail();
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        node.transmit(CluckConstants.BROADCAST_DESTINATION, "example-2", expected);
        ceo.check();
    }

    @Test(expected = NoSuchElementException.class)
    public void testReceiveBroadcastError() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                throw new NoSuchElementException();
            }

            @Override
            protected void receiveSideChannel(String dest, String source, byte[] data) {
                fail();
            }

            @Override
            protected void receive(String source, byte[] data) {
                fail();
            }
        };
        sub.attach("example-1");
        sub.send(CluckConstants.BROADCAST_DESTINATION, "example-2", Values.getRandomBytes(10));
    }

    @Test
    public void testReceiveSideChannel() {
        byte[] expected = Values.getRandomBytes(10);
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveSideChannel(String dest, String source, byte[] data) {
                assertEquals("side-channel-test", dest);
                assertEquals("example-2", source);
                assertArrayEquals(data, expected);
                ceo.event();
            }

            @Override
            protected void receive(String source, byte[] data) {
                fail();
            }
        };
        sub.attach("example-1");
        ceo.ifExpected = true;
        node.transmit("example-1/side-channel-test", "example-2", expected);
        ceo.check();
    }

    @Test(expected = NoSuchElementException.class)
    public void testReceiveSideChannelError() {
        CluckSubscriber sub = new CluckSubscriber(node) {
            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveSideChannel(String dest, String source, byte[] data) {
                throw new NoSuchElementException();
            }

            @Override
            protected void receive(String source, byte[] data) {
                fail();
            }
        };
        sub.attach("example-1");
        sub.send("example-1/side-channel-test", "example-2", Values.getRandomBytes(10));
    }
}
