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

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.log.LogLevel;
import ccre.log.LoggingTarget;
import ccre.log.VerifyingLoggingTarget;
import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.testing.CountingFloatOutput;
import ccre.util.Utils;
import ccre.util.Values;

@SuppressWarnings("javadoc")
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
            EventCell es = new EventCell();
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
            BooleanCell bs = new BooleanCell();
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

    @Test
    public void testFloatOutput() {
        for (int i = 0; i < 20; i++) {
            String name = Values.getRandomString();
            if (name.contains("/") || node.hasLink(name)) {
                i--;
                continue;
            }
            CountingFloatOutput cfo = new CountingFloatOutput();
            CluckPublisher.publish(node, name, cfo);
            FloatOutput proxy = CluckPublisher.subscribeFO(node, name);
            for (float f : Values.interestingFloats) {
                cfo.valueExpected = f;
                cfo.ifExpected = true;
                proxy.set(f);
                cfo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFloatOutputNullA() {
        CluckPublisher.publish(null, "hello", FloatOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testFloatOutputNullB() {
        CluckPublisher.publish(node, null, FloatOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testFloatOutputNullC() {
        CluckPublisher.publish(node, "hello", (FloatOutput) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFloatOutputWithSlash() {
        CluckPublisher.publish(node, "hello/other", FloatOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeFONullA() {
        CluckPublisher.subscribeFO(null, "hello");
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeFONullB() {
        CluckPublisher.subscribeFO(node, null);
    }

    @Test
    public void testFloatInput() {
        for (int i = 0; i < 20; i++) {
            String name = Values.getRandomString();
            if (name.contains("/") || node.hasLink(name)) {
                i--;
                continue;
            }
            CountingFloatOutput cfo = new CountingFloatOutput();
            FloatCell fs = new FloatCell();
            CluckPublisher.publish(node, name, fs.asInput());
            CluckPublisher.subscribeFI(node, name, false).send(cfo);
            for (float f : Values.interestingFloats) {
                cfo.valueExpected = f;
                cfo.ifExpected = true;
                fs.set(f);
                cfo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFloatInputNullA() {
        CluckPublisher.publish(null, "hello", FloatInput.zero);
    }

    @Test(expected = NullPointerException.class)
    public void testFloatInputNullB() {
        CluckPublisher.publish(node, null, FloatInput.zero);
    }

    @Test(expected = NullPointerException.class)
    public void testFloatInputNullC() {
        CluckPublisher.publish(node, "hello", (FloatInput) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFloatInputWithSlash() {
        CluckPublisher.publish(node, "hello/other", FloatInput.zero);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeFINullA() {
        CluckPublisher.subscribeFI(null, "hello", false);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeFINullB() {
        CluckPublisher.subscribeFI(node, null, false);
    }

    @Test
    public void testLoggingTarget() {
        for (int i = 0; i < 10; i++) {
            String name = Values.getRandomString();
            if (name.contains("/") || node.hasLink(name)) {
                i--;
                continue;
            }
            VerifyingLoggingTarget vlt = new VerifyingLoggingTarget();
            CluckPublisher.publish(node, name, vlt);
            LoggingTarget lt = CluckPublisher.subscribeLT(node, name);
            for (LogLevel level : LogLevel.allLevels) {
                for (String message : Values.getRandomStrings(5)) {
                    if (Values.getRandomBoolean()) {
                        String extra = Values.getRandomBoolean() ? Values.getRandomString() : null;
                        if (extra != null && extra.isEmpty()) {
                            extra = null;
                        }
                        vlt.ifExpected = true;
                        vlt.isThrowableExpected = false;
                        vlt.levelExpected = level;
                        vlt.messageExpected = message;
                        vlt.stringExpected = extra;
                        lt.log(level, message, extra);
                        vlt.check();
                    } else {
                        Throwable thr = Values.getRandomBoolean() ? new Throwable("A STRING") : null;
                        vlt.ifExpected = true;
                        vlt.isThrowableExpected = false;
                        vlt.levelExpected = level;
                        vlt.messageExpected = message;
                        vlt.stringExpected = Utils.toStringThrowable(thr);
                        lt.log(level, message, thr);
                        vlt.check();
                    }
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLoggingTargetNullA() {
        CluckPublisher.publish(null, "hello", LoggingTarget.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testLoggingTargetNullB() {
        CluckPublisher.publish(node, null, LoggingTarget.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testLoggingTargetNullC() {
        CluckPublisher.publish(node, "hello", (LoggingTarget) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoggingTargetWithSlash() {
        CluckPublisher.publish(node, "hello/other", LoggingTarget.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeLTNullA() {
        CluckPublisher.subscribeLT(null, "hello");
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeLTNullB() {
        CluckPublisher.subscribeLT(node, null);
    }

//    @Test
//    public void testSetupSearching() {
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
