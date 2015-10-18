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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.util.Values;

public class CluckPublisherTest {

    private CluckNode node;

    // TODO: test subscribing with a slash

    @Before
    public void setUp() throws Exception {
        node = new CluckNode();
    }

    @After
    public void tearDown() throws Exception {
        node = null;
    }

    @Test
    public void testEventOutput() {
        for (int i = 0; i < 20; i++) {
            String name = Values.getRandomString();
            if (name.contains("/") || node.hasLink(name)) {
                i--;
                continue;
            }
            CountingEventOutput ceo = new CountingEventOutput();
            CluckPublisher.publish(node, name, ceo);
            EventOutput proxy = CluckPublisher.subscribeEO(node, name);
            for (int j = 0; j < 10; j++) {
                ceo.ifExpected = true;
                proxy.event();
                ceo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testEventOutputNullA() {
        CluckPublisher.publish(null, "hello", EventOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testEventOutputNullB() {
        CluckPublisher.publish(node, null, EventOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testEventOutputNullC() {
        CluckPublisher.publish(node, "hello", (EventOutput) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventOutputWithSlash() {
        CluckPublisher.publish(node, "hello/other", EventOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeEONullA() {
        CluckPublisher.subscribeEO(null, "hello");
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeEONullB() {
        CluckPublisher.subscribeEO(node, null);
    }

    @Test
    public void testEventInput() {
        for (int i = 0; i < 20; i++) {
            String name = Values.getRandomString();
            if (name.contains("/") || node.hasLink(name)) {
                i--;
                continue;
            }
            CountingEventOutput ceo = new CountingEventOutput();
            EventStatus es = new EventStatus();
            CluckPublisher.publish(node, name, es.asInput());
            CluckPublisher.subscribeEI(node, name).send(ceo);
            for (int j = 0; j < 10; j++) {
                ceo.ifExpected = true;
                es.event();
                ceo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testEventInputNullA() {
        CluckPublisher.publish(null, "hello", EventInput.never);
    }

    @Test(expected = NullPointerException.class)
    public void testEventInputNullB() {
        CluckPublisher.publish(node, null, EventInput.never);
    }

    @Test(expected = NullPointerException.class)
    public void testEventInputNullC() {
        CluckPublisher.publish(node, "hello", (EventInput) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventInputWithSlash() {
        CluckPublisher.publish(node, "hello/other", EventInput.never);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeEINullA() {
        CluckPublisher.subscribeEI(null, "hello");
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeEINullB() {
        CluckPublisher.subscribeEI(node, null);
    }

    @Test
    public void testBooleanOutput() {
        for (int i = 0; i < 20; i++) {
            String name = Values.getRandomString();
            if (name.contains("/") || node.hasLink(name)) {
                i--;
                continue;
            }
            CountingBooleanOutput cbo = new CountingBooleanOutput();
            CluckPublisher.publish(node, name, cbo);
            BooleanOutput proxy = CluckPublisher.subscribeBO(node, name);
            for (boolean b : Values.interestingBooleans) {
                cbo.valueExpected = b;
                cbo.ifExpected = true;
                proxy.set(b);
                cbo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanOutputNullA() {
        CluckPublisher.publish(null, "hello", BooleanOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanOutputNullB() {
        CluckPublisher.publish(node, null, BooleanOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanOutputNullC() {
        CluckPublisher.publish(node, "hello", (BooleanOutput) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBooleanOutputWithSlash() {
        CluckPublisher.publish(node, "hello/other", BooleanOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeBONullA() {
        CluckPublisher.subscribeBO(null, "hello");
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeBONullB() {
        CluckPublisher.subscribeBO(node, null);
    }

    @Test
    public void testBooleanInput() {
        for (int i = 0; i < 20; i++) {
            String name = Values.getRandomString();
            if (name.contains("/") || node.hasLink(name)) {
                i--;
                continue;
            }
            CountingBooleanOutput cbo = new CountingBooleanOutput();
            BooleanStatus bs = new BooleanStatus();
            CluckPublisher.publish(node, name, bs.asInput());
            CluckPublisher.subscribeBI(node, name, false).send(cbo);
            for (boolean b : Values.interestingBooleans) {
                cbo.valueExpected = b;
                cbo.ifExpected = b != bs.get();
                bs.set(b);
                cbo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanInputNullA() {
        CluckPublisher.publish(null, "hello", BooleanInput.alwaysFalse);
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanInputNullB() {
        CluckPublisher.publish(node, null, BooleanInput.alwaysFalse);
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanInputNullC() {
        CluckPublisher.publish(node, "hello", (BooleanInput) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBooleanInputWithSlash() {
        CluckPublisher.publish(node, "hello/other", BooleanInput.alwaysFalse);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeBINullA() {
        CluckPublisher.subscribeBI(null, "hello", false);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeBINullB() {
        CluckPublisher.subscribeBI(node, null, false);
    }

//    @Test
//    public void testSetupSearching() {
//        fail("Not yet implemented");
//    }
//
//
//    @Test
//    public void testPublishCluckNodeStringLoggingTarget() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testSubscribeLT() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishCluckNodeStringBooleanInput() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testSubscribeBI() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishCluckNodeStringFloatInput() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testSubscribeFI() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishCluckNodeStringFloatOutput() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testSubscribeFO() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishCluckNodeStringFloatStatus() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishCluckNodeStringBooleanStatus() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishCluckNodeStringEventStatus() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishCluckNodeStringOutputStream() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testSubscribeOS() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testPublishRConf() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testSubscribeRConf() {
//        fail("Not yet implemented");
//    }
}
