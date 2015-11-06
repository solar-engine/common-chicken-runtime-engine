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

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.testing.CountingEventOutput;
import ccre.time.FakeTime;
import ccre.time.Time;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class EventOutputTest {

    private final EventOutput evil = () -> {
        throw new NoSuchElementException("safeEvent purposeful failure.");
    };
    private CountingEventOutput ceo, ceo2;

    @Before
    public void setUp() throws Exception {
        ceo = new CountingEventOutput();
        ceo2 = new CountingEventOutput();
    }

    @After
    public void tearDown() throws Exception {
        ceo = ceo2 = null;
    }

    private static Time oldProvider;
    private static FakeTime fake;

    // These two are for 'debounce' testing.
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

    @Test
    public void testIgnored() {
        // nothing should happen... but we don't have much of a way to check
        // that.
        EventOutput.ignored.event();
    }

    @Test
    public void testEvent() {
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            ceo.event();
            ceo.check();
        }
    }

    @Test
    public void testCombine() {
        EventOutput eo = ceo.combine(ceo2);
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = ceo2.ifExpected = true;
            eo.event();
            ceo.check();
            ceo2.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCombineNull() {
        ceo.combine(null);
    }

    @Test
    public void testFilter() {
        BooleanCell allow = new BooleanCell();
        EventOutput eo = ceo.filter(allow);
        for (boolean b : Values.interestingBooleans) {
            allow.set(b);
            for (int i = 0; i < 3; i++) {
                ceo.ifExpected = b;
                eo.event();
                ceo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFilterNull() {
        ceo.filter(null);
    }

    @Test
    public void testFilterNot() {
        BooleanCell deny = new BooleanCell();
        EventOutput eo = ceo.filterNot(deny);
        for (boolean b : Values.interestingBooleans) {
            deny.set(b);
            for (int i = 0; i < 3; i++) {
                ceo.ifExpected = !b;
                eo.event();
                ceo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFilterNotNull() {
        ceo.filterNot(null);
    }

    @Test
    public void testDebounce() throws InterruptedException {
        long millis = 72;
        EventOutput db = ceo.debounce(millis);
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
    public void testDebounceNegative() {
        ceo.debounce(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDebounceZero() {
        ceo.debounce(0);
    }

    private boolean gotProperly;

    @Test
    public void testOn() {
        gotProperly = false;
        assertEquals(ceo2, ceo.on(new EventInput() {
            @Override
            public EventOutput send(EventOutput notify) {
                assertEquals(ceo, notify);
                gotProperly = true;
                return ceo2;
            }

            @Override
            public EventOutput onUpdate(EventOutput notify) {
                fail("supposed to go to send() directly!");
                return null;
            }
        }));
        assertTrue(gotProperly);
    }

    @Test(expected = NullPointerException.class)
    public void testOnNull() {
        ceo.on(null);
    }

    @Test
    public void testSafeSet() {
        evil.safeEvent();
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionPropagation() {
        evil.event();
    }

    @Test(expected = NoSuchElementException.class)
    public void testCombineWithError1CausesError() {
        ceo.ifExpected = true;
        ceo.combine(evil).event();
    }

    @Test(expected = NoSuchElementException.class)
    public void testCombineWithError2CausesError() {
        ceo.ifExpected = true;
        evil.combine(ceo).event();
    }

    @Test
    public void testCombineWithError1Succeeds() {
        ceo.ifExpected = true;
        ceo.combine(evil).safeEvent();
        ceo.check();
    }

    @Test
    public void testCombineWithError2Succeeds() {
        ceo.ifExpected = true;
        evil.combine(ceo).safeEvent();
        ceo.check();
    }

    @Test
    public void testCombineWithError3CausesError() {
        boolean errored = false;
        try {
            evil.combine(evil).event();
        } catch (NoSuchElementException ex) {
            errored = true;
            assertEquals(ex.getSuppressed().length, 1);
            assertTrue(ex.getSuppressed()[0] instanceof NoSuchElementException);
        }
        assertTrue(errored);
    }
}
