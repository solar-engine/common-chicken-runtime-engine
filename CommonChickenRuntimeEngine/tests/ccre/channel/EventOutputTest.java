/*
 * Copyright 2015-2016 Cel Skeggs
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

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.log.LogLevel;
import ccre.log.VerifyingLogger;
import ccre.testing.CountingEventOutput;
import ccre.time.FakeTime;
import ccre.time.Time;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class EventOutputTest {

    private static final String ERROR_STRING = "safeEvent purposeful failure.";

    private final EventOutput evil = () -> {
        throw new NoSuchElementException(ERROR_STRING);
    };
    private CountingEventOutput ceo, ceo2;

    @Before
    public void setUp() throws Exception {
        ceo = new CountingEventOutput();
        ceo2 = new CountingEventOutput();
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() throws Exception {
        VerifyingLogger.checkAndEnd();
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
    public void testIgnoredCombine() {
        assertEquals(ceo, EventOutput.ignored.combine(ceo));
    }

    @Test(expected = NullPointerException.class)
    public void testIgnoredCombineNull() {
        EventOutput.ignored.combine((EventOutput) null);
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
    public void testStaticCombineSimplification() {
        assertEquals(EventOutput.combine(), EventOutput.ignored);
        assertEquals(EventOutput.combine(new EventOutput[] { ceo }), ceo);
    }

    @Test
    public void testStaticCombine() {
        for (int n = 0; n < 20; n++) {
            CountingEventOutput[] ceos = new CountingEventOutput[n];
            for (int i = 0; i < n; i++) {
                ceos[i] = new CountingEventOutput();
            }
            EventOutput combined = EventOutput.combine(ceos);
            for (int l = 0; l < 10; l++) {
                for (int i = 0; i < n; i++) {
                    ceos[i].ifExpected = true;
                }
                combined.event();
                for (int i = 0; i < n; i++) {
                    ceos[i].check();
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNull() {
        EventOutput.combine((EventOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullElem() {
        EventOutput.combine(new EventOutput[] { null });
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullEarlierElem() {
        EventOutput.combine(null, ceo);
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullLaterElem() {
        EventOutput.combine(ceo, null);
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
        CancelOutput cex = ceo2::event;
        assertEquals(cex, ceo.on(new EventInput() {
            @Override
            public CancelOutput send(EventOutput notify) {
                assertEquals(ceo, notify);
                gotProperly = true;
                return cex;
            }

            @Override
            public CancelOutput onUpdate(EventOutput notify) {
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
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during event propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        evil.safeEvent();
        VerifyingLogger.check();
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
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during event propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        ceo.combine(evil).safeEvent();
        VerifyingLogger.check();
        ceo.check();
    }

    @Test
    public void testCombineWithError2Succeeds() {
        ceo.ifExpected = true;
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during event propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        evil.combine(ceo).safeEvent();
        VerifyingLogger.check();
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

    @Test
    public void testStaticCombineSingleError() {
        for (int n = 1; n < 6; n++) {
            CountingEventOutput[] ceos = new CountingEventOutput[n];
            for (int i = 0; i < n; i++) {
                ceos[i] = new CountingEventOutput();
            }
            for (int bad = 0; bad < n; bad++) {
                EventOutput[] reals = new EventOutput[n];
                System.arraycopy(ceos, 0, reals, 0, n);
                reals[bad] = evil;
                EventOutput combined = EventOutput.combine(reals);
                for (int l = 0; l < 10; l++) {
                    for (int i = 0; i < n; i++) {
                        ceos[i].ifExpected = i != bad;
                    }
                    try {
                        combined.event();
                        fail();
                    } catch (NoSuchElementException ex) {
                        assertEquals(0, ex.getSuppressed().length);
                    }
                    for (int i = 0; i < n; i++) {
                        ceos[i].check();
                    }
                }
            }
        }
    }

    @Test
    public void testStaticCombineManyErrors() {
        for (int n = 1; n < 6; n++) {
            CountingEventOutput[] ceos = new CountingEventOutput[n];
            for (int i = 0; i < n; i++) {
                ceos[i] = new CountingEventOutput();
            }
            for (int bad = 0; bad < 4 * n * n; bad++) {
                boolean[] evils = new boolean[n];
                int evil_count = 0;
                EventOutput[] reals = new EventOutput[n];
                System.arraycopy(ceos, 0, reals, 0, n);
                for (int i = 0; i < n; i++) {
                    evils[i] = Values.getRandomBoolean();
                    if (evils[i]) {
                        evil_count++;
                        reals[i] = evil;
                    }
                }
                EventOutput combined = EventOutput.combine(reals);
                for (int l = 0; l < 10; l++) {
                    for (int i = 0; i < n; i++) {
                        ceos[i].ifExpected = !evils[i];
                    }
                    try {
                        combined.event();
                        assertEquals(0, evil_count);
                    } catch (NoSuchElementException ex) {
                        Throwable[] suppressed = ex.getSuppressed();
                        assertEquals(evil_count - 1, suppressed.length);
                        for (int i = 0; i < suppressed.length; i++) {
                            assertTrue(suppressed[i] instanceof NoSuchElementException);
                        }
                    }
                    for (int i = 0; i < n; i++) {
                        ceos[i].check();
                    }
                }
            }
        }
    }

    @Test
    public void testCell() {
        EventOutput out = ceo::event;
        EventIO eio = out.cell();
        eio.send(ceo2);
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            ceo2.ifExpected = true;
            eio.event();
            ceo2.check();
            ceo.check();
        }
    }
}
