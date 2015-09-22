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
package ccre.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.testing.CountingEventOutput;
import ccre.time.FakeTime;
import ccre.time.Time;
import ccre.util.Values;

public class EventInputTest { // TODO: should I be checking propagation of
                              // withRecovery messages?

    private EventInput ei;
    private CountingEventOutput ceo, ceo2;
    private static Time oldProvider;
    private static FakeTime fake;

    // These two are for 'debounced' testing.
    @BeforeClass
    public static void setUpClass() {
        assertNull(oldProvider);
        oldProvider = Time.getTimeProvider();
        fake = new FakeTime();
        Time.setTimeProvider(fake);
    }

    @AfterClass
    public static void tearDownClass() {
        assertNotNull(oldProvider);
        Time.setTimeProvider(oldProvider);
        oldProvider = null;
        fake = null;
    }

    @Before
    public void setUp() throws Exception {
        ceo = new CountingEventOutput();
        ceo2 = new CountingEventOutput();
    }

    @After
    public void tearDown() throws Exception {
        ei = null;
        ceo = null;
        ceo2 = null;
    }

    @Test
    public void testNever() {
        assertEquals(EventOutput.ignored, EventInput.never.onUpdateR(() -> fail("should not be fired!")));
    }

    private boolean gotProperly = false;

    @Test
    public void testSend() {
        gotProperly = false;
        ei = new EventInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                assertEquals(ceo, notify);
                gotProperly = true;
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                fail("expected onUpdate, not onUpdateR!");
                return null;
            }
        };
        ei.send(ceo);
        assertTrue(gotProperly);
    }

    @Test(expected = NullPointerException.class)
    public void testSendNull() {
        ei = (notify) -> notify;
        ei.send(null);
    }

    @Test
    public void testSendR() {
        gotProperly = false;
        ei = new EventInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                fail("expected onUpdateR, not onUpdate!");
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                assertEquals(ceo, notify);
                gotProperly = true;
                return ceo2;
            }
        };
        assertEquals(ceo2, ei.sendR(ceo));
        assertTrue(gotProperly);
    }

    @Test(expected = NullPointerException.class)
    public void testSendRNull() {
        ei = (notify) -> notify;
        ei.sendR(null);
    }

    @Test
    public void testOr() {
        EventStatus a = new EventStatus(), b = new EventStatus();
        a.or(b).send(ceo);
        for (boolean c : Values.interestingBooleans) {
            ceo.ifExpected = true;
            (c ? a : b).event();
            ceo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOrNull() {
        ei = (notify) -> notify;
        ei.or(null);
    }

    @Test
    public void testAnd() {
        EventStatus a = new EventStatus();
        BooleanStatus on = new BooleanStatus();
        a.and(on).send(ceo);
        for (boolean c : Values.interestingBooleans) {
            on.set(c);
            for (int i = 0; i < 3; i++) {
                ceo.ifExpected = c;
                a.event();
                ceo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAndNull() {
        ei = (notify) -> notify;
        ei.and(null);
    }

    @Test
    public void testAndNot() {
        EventStatus a = new EventStatus();
        BooleanStatus off = new BooleanStatus();
        a.andNot(off).send(ceo);
        for (boolean c : Values.interestingBooleans) {
            off.set(c);
            for (int i = 0; i < 3; i++) {
                ceo.ifExpected = !c;
                a.event();
                ceo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAndNotNull() {
        ei = (notify) -> notify;
        ei.andNot(null);
    }

    @Test
    public void testDebounced() throws InterruptedException {
        long millis = 72;
        EventStatus db = new EventStatus();
        db.debounced(millis).send(ceo);
        for (int i = 0; i < 100; i++) {
            ceo.ifExpected = (i % 6 == 0);
            db.event();
            ceo.check();
            if (i % 3 == 2) {
                fake.forward(millis / 2);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDebouncedNegative() {
        ei = (notify) -> notify;
        ei.debounced(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDebouncedZero() {
        ei = (notify) -> notify;
        ei.debounced(0);
    }

    @Test
    public void testOnUpdate() {
        gotProperly = false;
        EventInput ei = (notify) -> {
            assertEquals(ceo, notify);
            gotProperly = true;
            return null;
        };
        ei.onUpdate(ceo);
        assertTrue(gotProperly);
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateNull() {
        ei = (notify) -> notify;
        ei.onUpdate(null);
    }
}
