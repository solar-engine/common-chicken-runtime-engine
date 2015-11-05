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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingEventOutput;

public class CluckRMTSubscriberTest {

    private CluckNode node;
    private CountingEventOutput ceo;

    // TODO: check all the logging in this class

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

    private boolean fail = false;

    @Test
    public void testReceiveBroadcast() {
        fail = false;
        CluckRMTSubscriber sub = new CluckRMTSubscriber(node, CluckConstants.RMT_EVENTINPUT) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail = true;
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
            sub.send(CluckConstants.BROADCAST_DESTINATION, "example-2", new byte[] { CluckConstants.RMT_PING });
            vcl.check();
        }
        for (byte[] ignorableMessage : new byte[][] { { CluckConstants.RMT_PING, CluckConstants.RMT_EVENTINPUT }, { CluckConstants.RMT_INVOKE }, {} }) {
            for (int i = 0; i < 5; i++) {
                vcl.ifExpected = false;
                sub.send(CluckConstants.BROADCAST_DESTINATION, "example-2", ignorableMessage);
                vcl.check();
            }
        }
        for (int i = 0; i < 5; i++) {
            vcl.ifExpected = true;
            sub.send(CluckConstants.BROADCAST_DESTINATION, "example-2", new byte[] { CluckConstants.RMT_PING });
            vcl.check();
        }
        if (fail) {
            fail();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCluckRMTSubscriberCluckNodeByteNull() {
        new CluckRMTSubscriber(null, CluckConstants.RMT_INVOKE) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }
        };
    }

    @Test(expected = NullPointerException.class)
    public void testCluckRMTSubscriberCluckNodeByteIntNull() {
        new CluckRMTSubscriber(null, CluckConstants.RMT_INVOKE, 2) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCluckRMTSubscriberCluckNodeByteIntZero() {
        new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE, 0) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                fail();
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCluckRMTSubscriberCluckNodeByteIntNegative() {
        new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE, -1) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                fail();
            }
        };
    }

    @Test
    public void testReceiveValid() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                assertEquals("source", source);
                assertArrayEquals(data, new byte[] { CluckConstants.RMT_INVOKE });
                ceo.event();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                fail();
            }
        };
        ceo.ifExpected = true;
        rs.send(null, "source", new byte[] { CluckConstants.RMT_INVOKE });
        ceo.check();
    }

    @Test
    public void testReceiveValidLonger() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                assertEquals("source", source);
                assertArrayEquals(data, new byte[] { CluckConstants.RMT_INVOKE, 7, 16 });
                ceo.event();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                fail();
            }
        };
        ceo.ifExpected = true;
        rs.send(null, "source", new byte[] { CluckConstants.RMT_INVOKE, 7, 16 });
        ceo.check();
    }

    @Test
    public void testReceiveValidLongerRequired() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE, 3) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                assertEquals("source", source);
                assertArrayEquals(data, new byte[] { CluckConstants.RMT_INVOKE, 7, 16 });
                ceo.event();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                fail();
            }
        };
        ceo.ifExpected = true;
        rs.send(null, "source", new byte[] { CluckConstants.RMT_INVOKE, 7, 16 });
        ceo.check();
    }

    @Test
    public void testReceiveInvalidZero() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                assertEquals("source", source);
                assertArrayEquals(data, new byte[] {});
                ceo.event();
            }
        };
        ceo.ifExpected = true;
        rs.send(null, "source", new byte[] {});
        ceo.check();
    }

    @Test
    public void testReceiveInvalidShort() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE, 2) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                assertEquals("source", source);
                assertArrayEquals(data, new byte[] { CluckConstants.RMT_INVOKE });
                ceo.event();
            }
        };
        ceo.ifExpected = true;
        rs.send(null, "source", new byte[] { CluckConstants.RMT_INVOKE });
        ceo.check();
    }

    @Test
    public void testReceiveInvalidRMT() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                assertEquals("source", source);
                assertArrayEquals(data, new byte[] { CluckConstants.RMT_EVENTINPUT });
                ceo.event();
            }
        };
        ceo.ifExpected = true;
        rs.send(null, "source", new byte[] { CluckConstants.RMT_EVENTINPUT });
        ceo.check();
    }

    @Test
    public void testReceiveInvalidRMTLonger() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }

            @Override
            protected void receiveInvalid(String source, byte[] data) {
                assertEquals("source", source);
                assertArrayEquals(data, new byte[] { CluckConstants.RMT_EVENTINPUT, 9, 0 });
                ceo.event();
            }
        };
        ceo.ifExpected = true;
        rs.send(null, "source", new byte[] { CluckConstants.RMT_EVENTINPUT, 9, 0 });
        ceo.check();
    }

    @Test
    public void testReceiveIgnoreInvalid() {
        CluckRMTSubscriber rs = new CluckRMTSubscriber(node, CluckConstants.RMT_INVOKE) {
            @Override
            protected void receiveValid(String source, byte[] data) {
                fail();
            }
        };
        rs.send(null, "source", new byte[] { CluckConstants.RMT_EVENTINPUT });
    }
}
