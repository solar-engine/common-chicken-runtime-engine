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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ccre.testing.CountingEventOutput;
import ccre.util.Values;

public class CluckNodeTest {

    private CluckNode node;

    @Before
    public void setUp() throws Exception {
        node = new CluckNode();
    }

    @After
    public void tearDown() throws Exception {
        node = null;
    }

    @Test
    public void testNotifyNetworkModified() {
        CountingEventOutput ceo = new CountingEventOutput();
        node.addLink(new CluckLink() {
            @Override
            public boolean send(String dest, String source, byte[] data) {
                assertEquals(dest, CluckConstants.BROADCAST_DESTINATION);
                // note: this is an implementation detail
                assertEquals(source, "#modsrc");
                assertEquals(data.length, 1);
                assertEquals(data[0], CluckConstants.RMT_NOTIFY);
                ceo.event();
                return true;
            }
        }, "example-nm-link");
        ceo.ifExpected = true;
        node.notifyNetworkModified();
        ceo.check();
    }

    @Test
    public void testBroadcast() {
        VerifyingCluckLink[] vcls = new VerifyingCluckLink[10];
        for (int i = 0; i < vcls.length; i++) {
            vcls[i] = new VerifyingCluckLink();
            node.addLink(vcls[i], "example-" + i);
        }
        for (byte[] message : Values.getRandomByteses(10, 1, 256)) {
            for (String source : Values.getRandomStrings(10)) {
                for (int i = 0; i < vcls.length; i++) {
                    vcls[i].expectedSource = source;
                    vcls[i].expectedMessage = message;
                    vcls[i].expectedDestination = CluckConstants.BROADCAST_DESTINATION;
                    vcls[i].ifExpected = true;
                }
                node.broadcast(source, message, null);
                for (int i = 0; i < vcls.length; i++) {
                    vcls[i].check();
                }
            }
        }
    }

    @Test
    public void testBroadcastDenies() {
        VerifyingCluckLink[] vcls = new VerifyingCluckLink[10];
        for (int i = 0; i < vcls.length; i++) {
            vcls[i] = new VerifyingCluckLink();
            node.addLink(vcls[i], "example-" + i);
        }
        int ti = 0;
        for (byte[] message : Values.getRandomByteses(11, 1, 256)) {
            for (String source : Values.getRandomStrings(7)) {
                int dropi = ti++ % vcls.length;
                for (int i = 0; i < vcls.length; i++) {
                    vcls[i].expectedSource = source;
                    vcls[i].expectedMessage = message;
                    vcls[i].expectedDestination = CluckConstants.BROADCAST_DESTINATION;
                    vcls[i].ifExpected = dropi != i;
                }
                node.broadcast(source, message, vcls[dropi]);
                for (int i = 0; i < vcls.length; i++) {
                    vcls[i].check();
                }
            }
        }
    }

    @Test
    public void testTransmitBroadcastDenies() {
        VerifyingCluckLink[] vcls = new VerifyingCluckLink[10];
        for (int i = 0; i < vcls.length; i++) {
            vcls[i] = new VerifyingCluckLink();
            node.addLink(vcls[i], "example-" + i);
        }
        int ti = 0;
        for (byte[] message : Values.getRandomByteses(11, 1, 256)) {
            for (String source : Values.getRandomStrings(7)) {
                int dropi = ti++ % vcls.length;
                for (int i = 0; i < vcls.length; i++) {
                    vcls[i].expectedSource = source;
                    vcls[i].expectedMessage = message;
                    vcls[i].expectedDestination = CluckConstants.BROADCAST_DESTINATION;
                    vcls[i].ifExpected = dropi != i;
                }
                node.transmit(CluckConstants.BROADCAST_DESTINATION, source, message, vcls[dropi]);
                for (int i = 0; i < vcls.length; i++) {
                    vcls[i].check();
                }
            }
        }
    }

    @Test
    public void testBroadcastDetach() {
        VerifyingCluckLink[] vcls = new VerifyingCluckLink[10];
        for (int i = 0; i < vcls.length; i++) {
            vcls[i] = new VerifyingCluckLink();
            node.addLink(vcls[i], "example-" + i);
        }
        int ti = 0;
        for (byte[] message : Values.getRandomByteses(11, 1, 256)) {
            for (String source : Values.getRandomStrings(7)) {
                int dropi = ti++ % vcls.length;
                for (int i = 0; i < vcls.length; i++) {
                    vcls[i].expectedSource = source;
                    vcls[i].expectedMessage = message;
                    vcls[i].expectedDestination = CluckConstants.BROADCAST_DESTINATION;
                    if (vcls[i].keepAlive) {
                        vcls[i].keepAlive = i != dropi;
                        vcls[i].ifExpected = true;
                    }
                }
                node.broadcast(source, message, null);
                for (int i = 0; i < vcls.length; i++) {
                    if (!vcls[i].keepAlive) {
                        assertFalse(node.hasLink("example-" + i));
                    } else {
                        assertTrue(node.hasLink("example-" + i));
                    }
                    vcls[i].check();
                }
            }
        }
    }

    @Test
    public void testBroadcastError() {
        VerifyingCluckLink[] vcls = new VerifyingCluckLink[50];
        for (int i = 0; i < vcls.length; i++) {
            vcls[i] = new VerifyingCluckLink();
            node.addLink(vcls[i], "example-" + i);
        }
        CountingEventOutput ceo = new CountingEventOutput();
        CluckLink evil = new CluckLink() {
            @Override
            public boolean send(String dest, String source, byte[] data) {
                ceo.event();
                throw new RuntimeException("Purposeful failure.");
            }
        };
        node.addLink(evil, "evil");
        for (int c = 0; c < 5; c++) {
            byte[] message = Values.getRandomBytes(10);
            for (int i = 0; i < vcls.length; i++) {
                vcls[i].expectedDestination = CluckConstants.BROADCAST_DESTINATION;
                vcls[i].expectedMessage = message;
                vcls[i].ifExpected = true;
            }
            ceo.ifExpected = true;
            node.broadcast(null, message, null);
            // TODO: test logging
            ceo.check();
            for (int i = 0; i < vcls.length; i++) {
                vcls[i].check();
            }
        }
    }

    @Test
    public void testTransmitDirect() {
        VerifyingCluckLink vcl = new VerifyingCluckLink();
        vcl.expectedSource = "testy testy";
        node.addLink(vcl, "example");
        for (int i = 0; i < 5; i++) {
            byte[] message = Values.getRandomBytes(10);
            vcl.expectedMessage = message;
            vcl.ifExpected = true;
            node.transmit("example", "testy testy", message);
            vcl.check();
        }
    }

    @Test
    public void testTransmitUnsub() {
        VerifyingCluckLink vcl = new VerifyingCluckLink();
        vcl.expectedSource = "testy testy";
        node.addLink(vcl, "example");
        for (int i = 0; i < 6; i++) {
            byte[] message = Values.getRandomBytes(10);
            vcl.expectedMessage = message;
            vcl.ifExpected = i < 5;
            vcl.keepAlive = i < 4;
            node.transmit("example", "testy testy", message);
            vcl.check();
        }
    }

    @Test
    public void testTransmitError() {
        node.addLink(new CluckLink() {
            @Override
            public boolean send(String dest, String source, byte[] data) {
                throw new RuntimeException("Purposeful failure!");
            }
        }, "evil");
        // TODO: check logging
        node.transmit("evil", null, new byte[] { CluckConstants.RMT_NOTIFY });
    }

    @Test
    public void testTransmitSideChannel() {
        VerifyingCluckLink vcl = new VerifyingCluckLink();
        vcl.expectedSource = "testy testy";
        vcl.expectedDestination = "side-channel";
        node.addLink(vcl, "example");
        for (int i = 0; i < 5; i++) {
            byte[] message = Values.getRandomBytes(10);
            vcl.expectedMessage = message;
            vcl.ifExpected = true;
            node.transmit("example/side-channel", "testy testy", message);
            vcl.check();
        }
    }

    @Test
    public void testTransmitSideChannelMulti() {
        VerifyingCluckLink vcl = new VerifyingCluckLink();
        vcl.expectedSource = "testy testy";
        vcl.expectedDestination = "side-channel/extra/other/hi";
        node.addLink(vcl, "example");
        for (int i = 0; i < 5; i++) {
            byte[] message = Values.getRandomBytes(10);
            vcl.expectedMessage = message;
            vcl.ifExpected = true;
            node.transmit("example/side-channel/extra/other/hi", "testy testy", message);
            vcl.check();
        }
    }

    @Test
    public void testTransmitNAKNAK() {
        // makes sure that we don't get an infinite loop
        node.transmit("example", "example", new byte[] { CluckConstants.RMT_NOTIFY });
    }

    @Test
    public void testTransmitNAK() {
        VerifyingCluckLink nackTarget = new VerifyingCluckLink();
        nackTarget.expectedDestination = null;
        nackTarget.expectedMessage = new byte[] { CluckConstants.RMT_NEGATIVE_ACK };
        nackTarget.expectedSource = "example";
        node.addLink(nackTarget, "nacker");
        for (int i = 0; i < 10; i++) {
            nackTarget.ifExpected = true;
            // TODO: check logging
            node.transmit("example", "nacker", new byte[] { CluckConstants.RMT_NOTIFY });
            nackTarget.check();
        }
    }

    @Test
    public void testTransmitNoNAKNAK() {
        VerifyingCluckLink nackTarget = new VerifyingCluckLink();
        node.addLink(nackTarget, "nacker");
        for (int i = 0; i < 10; i++) {
            nackTarget.ifExpected = false;
            node.transmit("example", "nacker", new byte[] { CluckConstants.RMT_NEGATIVE_ACK });
            nackTarget.check();
        }
    }

    @Test
    public void testTransmitNAKSideChannel() {
        VerifyingCluckLink nackTarget = new VerifyingCluckLink();
        nackTarget.expectedDestination = "other-side-channel";
        nackTarget.expectedMessage = new byte[] { CluckConstants.RMT_NEGATIVE_ACK };
        nackTarget.expectedSource = "example/side-channel";
        node.addLink(nackTarget, "nacker");
        for (int i = 0; i < 10; i++) {
            nackTarget.ifExpected = true;
            node.transmit("example/side-channel", "nacker/other-side-channel", new byte[] { CluckConstants.RMT_NOTIFY });
            nackTarget.check();
        }
    }

    @Test
    public void testSubscribeToStructureNotifications() {
        CountingEventOutput ceo = new CountingEventOutput();
        node.subscribeToStructureNotifications("receiver-1", ceo);

        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            node.notifyNetworkModified();
            ceo.check();
        }
        for (String source : Values.getRandomStrings(10)) {
            ceo.ifExpected = true;
            node.transmit(CluckConstants.BROADCAST_DESTINATION, source, new byte[] { CluckConstants.RMT_NOTIFY });
            ceo.check();
        }
        for (String source : Values.getRandomStrings(10)) {
            ceo.ifExpected = false;
            node.transmit(CluckConstants.BROADCAST_DESTINATION, source, new byte[] { CluckConstants.RMT_NOTIFY, 0 });
            ceo.check();
        }
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = false;
            node.transmit(CluckConstants.BROADCAST_DESTINATION, "#modsrc", new byte[] { CluckConstants.RMT_PING });
            ceo.check();
        }
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = false;
            node.transmit(CluckConstants.BROADCAST_DESTINATION, "arbitrary", new byte[] { CluckConstants.RMT_PING });
            ceo.check();
        }
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = false;
            node.transmit(CluckConstants.BROADCAST_DESTINATION, "arbitrary", new byte[] { CluckConstants.RMT_BOOLINPUT });
            ceo.check();
        }
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = false;
            node.transmit("receiver-1", "arbitrary", new byte[] { CluckConstants.RMT_BOOLINPUT });
            ceo.check();
        }
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = false;
            node.transmit("receiver-1", "arbitrary", new byte[] { CluckConstants.RMT_NOTIFY });
            ceo.check();
        }
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = false;
            node.transmit("receiver-1", "#modsrc", new byte[] { CluckConstants.RMT_NOTIFY });
            ceo.check();
        }
    }

    private boolean failInvoke = false;

    @Test
    public void testGetLinkName() {
        failInvoke = false;
        CluckLink cl = new CluckLink() {
            @Override
            public boolean send(String dest, String source, byte[] data) {
                failInvoke = true;
                throw new RuntimeException("Should not be invoked!");
            }
        };
        node.addLink(new VerifyingCluckLink(), "example-1");
        node.addLink(cl, "example-2");
        node.addLink(new VerifyingCluckLink(), "example-3");
        node.addLink(new VerifyingCluckLink(), "example-4");
        node.addLink(new VerifyingCluckLink(), "example-5");
        assertEquals(node.getLinkName(cl), "example-2");
        assertFalse(failInvoke);
    }

    @Test(expected = NullPointerException.class)
    public void testGetLinkNameNull() {
        node.getLinkName(null);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetLinkNameNonexistent() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No such link!");
        node.getLinkName(new VerifyingCluckLink());
    }

    @Test
    public void testHasLink() {
        for (int i = 0; i < 10; i++) {
            for (int j = i; j < 10; j++) {
                assertFalse(node.hasLink("test-" + j));
            }
            node.addLink(new VerifyingCluckLink(), "test-" + i);
            for (int j = 0; j <= i; j++) {
                assertTrue(node.hasLink("test-" + j));
            }
        }
        for (int i = 0; i < 5; i++) {
            node.removeLink("test-" + i);
            assertFalse(node.hasLink("test-" + i));
        }
        for (int i = 0; i < 5; i++) {
            assertFalse(node.hasLink("test-" + i));
        }
    }

    @Test
    public void testAddLinkHasLink() {
        assertFalse(node.hasLink("test-1"));
        node.addLink(new VerifyingCluckLink(), "test-1");
        assertTrue(node.hasLink("test-1"));
    }

    @Test
    public void testAddLinkExistent() {
        node.addLink(new VerifyingCluckLink(), "example-1");
        try {
            node.addLink(new VerifyingCluckLink(), "example-1");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("already used"));
            assertTrue(ex.getMessage().contains("example-1"));
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAddLinkNullA() {
        node.addLink(new VerifyingCluckLink(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddLinkNullB() {
        node.addLink(null, "example-1");
    }

    @Test
    public void testAddOrReplaceLinkHasLink() {
        assertFalse(node.hasLink("test-1"));
        node.addOrReplaceLink(new VerifyingCluckLink(), "test-1");
        assertTrue(node.hasLink("test-1"));
    }

    @Test
    public void testAddOrReplaceLinkExistent() {
        assertFalse(node.hasLink("example-1"));
        node.addLink(new VerifyingCluckLink(), "example-1");
        assertTrue(node.hasLink("example-1"));
        VerifyingCluckLink beta = new VerifyingCluckLink();
        node.addOrReplaceLink(beta, "example-1");
        assertTrue(node.hasLink("example-1"));

        beta.expectedMessage = new byte[] { CluckConstants.RMT_NOTIFY };
        beta.ifExpected = true;
        node.transmit("example-1", null, new byte[] { CluckConstants.RMT_NOTIFY });
        beta.check();
    }

    @Test(expected = NullPointerException.class)
    public void testAddOrReplaceLinkNullA() {
        node.addOrReplaceLink(new VerifyingCluckLink(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddOrReplaceLinkNullB() {
        node.addOrReplaceLink(null, "example-1");
    }

    @Test
    public void testRemoveLink() {
        assertFalse(node.removeLink("test-1"));

        assertFalse(node.hasLink("test-1"));
        VerifyingCluckLink main = new VerifyingCluckLink();
        main.expectedDestination = null;
        main.expectedSource = "bounce";
        main.expectedMessage = new byte[] { CluckConstants.RMT_NOTIFY };
        node.addLink(main, "test-1");
        assertTrue(node.hasLink("test-1"));

        VerifyingCluckLink bounce = new VerifyingCluckLink();
        bounce.expectedDestination = null;
        bounce.expectedSource = "test-1";
        bounce.expectedMessage = new byte[] { CluckConstants.RMT_NEGATIVE_ACK };
        node.addLink(bounce, "bounce");

        main.ifExpected = true;
        node.transmit("test-1", "bounce", new byte[] { CluckConstants.RMT_NOTIFY });
        main.check();

        assertTrue(node.hasLink("test-1"));
        assertTrue(node.removeLink("test-1"));
        assertFalse(node.hasLink("test-1"));

        bounce.ifExpected = true;
        node.transmit("test-1", "bounce", new byte[] { CluckConstants.RMT_NOTIFY });
        bounce.check();

        assertFalse(node.hasLink("test-1"));
        assertFalse(node.removeLink("test-1"));
        assertFalse(node.hasLink("test-1"));
    }

    @Test
    public void testGetRPCManager() {
        assertTrue(node.getRPCManager() == node.getRPCManager());
    }

    @Test(expected = NotSerializableException.class)
    public void testNotSerializable() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutput oos = new ObjectOutputStream(baos)) {
                oos.writeObject(node);
            }
        }
    }

    @Test
    public void testSerializeGlobal() throws IOException, ClassNotFoundException {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutput oo = new ObjectOutputStream(baos)) {
                oo.writeObject(Cluck.getNode());
            }
            bytes = baos.toByteArray();
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            try (ObjectInput oi = new ObjectInputStream(bais)) {
                assertTrue(Cluck.getNode() == oi.readObject());
            }
        }
    }
}
