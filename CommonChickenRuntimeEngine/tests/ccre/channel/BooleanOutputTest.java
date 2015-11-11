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
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class BooleanOutputTest {

    private final BooleanOutput evil = (v) -> {
        throw new NoSuchElementException("safeSet purposeful failure.");
    };
    private CountingBooleanOutput cbo, cbo2;

    @Before
    public void setUp() throws Exception {
        cbo = new CountingBooleanOutput();
        cbo2 = new CountingBooleanOutput();
    }

    @After
    public void tearDown() throws Exception {
        cbo = null;
        cbo2 = null;
    }

    @Test
    public void testIgnored() {
        for (boolean b : Values.interestingBooleans) {
            BooleanOutput.ignored.set(b);
        }
    }

    @Test
    public void testInvert() {
        BooleanOutput bo = cbo.invert();
        for (boolean b : Values.interestingBooleans) {
            cbo.ifExpected = true;
            cbo.valueExpected = !b;
            bo.set(b);
            cbo.check();
        }
    }

    @Test
    public void testInvertInvert() {
        assertEquals(cbo, cbo.invert().invert());
    }

    @Test
    public void testCombine() {
        BooleanOutput bo = cbo.combine(cbo2);
        for (boolean b : Values.interestingBooleans) {
            cbo.ifExpected = cbo2.ifExpected = true;
            cbo.valueExpected = cbo2.valueExpected = b;
            bo.set(b);
            cbo.check();
            cbo2.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCombineNull() {
        cbo.combine(null);
    }

    @Test
    public void testLimitUpdatesTo() {
        EventCell es = new EventCell();
        BooleanOutput ob = cbo.limitUpdatesTo(es);

        es.event();
        cbo.check();// nothing should have gone through yet, since no value has
                    // been sent yet!

        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            boolean v = i % 2 == 0;
            ob.set(v);
            if (i % 73 == 0 || rand.nextInt(10) == 0) {
                cbo.ifExpected = true;
                cbo.valueExpected = v;
                es.event();
                cbo.check();
                if (rand.nextBoolean()) {
                    cbo.ifExpected = true;
                    es.event();
                    cbo.check();
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testLimitUpdatesToNull() {
        cbo.limitUpdatesTo(null);
    }

    @Test
    public void testEventSetBoolean() {
        trySet(cbo.eventSet(false), false);
        trySet(cbo.eventSet(true), true);
    }

    private void trySet(EventOutput doSet, boolean expect) {
        for (int i = 0; i < 10; i++) {
            cbo.ifExpected = true;
            cbo.valueExpected = expect;
            doSet.event();
            cbo.check();
        }
    }

    @Test
    public void testEventSetBooleanInput() {
        BooleanCell bs = new BooleanCell();
        EventOutput evt = cbo.eventSet(bs);
        for (int i = 0; i < 10; i++) {
            cbo.ifExpected = true;
            bs.set(cbo.valueExpected = (i % 2) == 0);
            evt.event();
            cbo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testEventSetBooleanInputNull() {
        cbo.eventSet(null);
    }

    @Test
    public void testSetWhenBooleanEventInputFalse() {
        EventCell set = new EventCell();
        cbo.setWhen(false, set);
        trySet(set, false);
    }

    @Test(expected = NullPointerException.class)
    public void testSetWhenBooleanEventInputFalseNull() {
        cbo.setWhen(false, null);
    }

    @Test
    public void testSetWhenBooleanEventInputTrue() {
        EventCell set = new EventCell();
        cbo.setWhen(true, set);
        trySet(set, true);
    }

    @Test(expected = NullPointerException.class)
    public void testSetWhenBooleanEventInputTrueNull() {
        cbo.setWhen(true, null);
    }

    @Test
    public void testSetWhenBooleanInputEventInput() {
        BooleanCell bs = new BooleanCell();
        EventCell evt = new EventCell();
        cbo.setWhen(bs, evt);
        for (int i = 0; i < 10; i++) {
            cbo.ifExpected = true;
            bs.set(cbo.valueExpected = (i % 2) == 0);
            evt.event();
            cbo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSetWhenBooleanInputEventInputTrueNullA() {
        cbo.setWhen(null, EventInput.never);
    }

    @Test(expected = NullPointerException.class)
    public void testSetWhenBooleanInputEventInputTrueNullB() {
        cbo.setWhen(BooleanInput.alwaysFalse, null);
    }

    @Test
    public void testSetTrueWhen() {
        EventCell set = new EventCell();
        cbo.setTrueWhen(set);
        trySet(set, true);
    }

    @Test(expected = NullPointerException.class)
    public void testSetTrueWhenNull() {
        cbo.setTrueWhen(null);
    }

    @Test
    public void testSetFalseWhen() {
        EventCell set = new EventCell();
        cbo.setFalseWhen(set);
        trySet(set, false);
    }

    @Test(expected = NullPointerException.class)
    public void testSetFalseWhenNull() {
        cbo.setFalseWhen(null);
    }

    @Test
    public void testOnChange() {
        CountingEventOutput toFalse = new CountingEventOutput(), toTrue = new CountingEventOutput();
        BooleanOutput bo = BooleanOutput.onChange(toFalse, toTrue);
        boolean last = false;
        for (boolean b : Values.interestingBooleans) {
            (b ? toTrue : toFalse).ifExpected = (last != b);
            bo.set(b);
            toTrue.check();
            toFalse.check();
            last = b;
        }
    }

    @Test
    public void testOnChangeNullA() {
        CountingEventOutput toTrue = new CountingEventOutput();
        BooleanOutput bo = BooleanOutput.onChange(null, toTrue);
        boolean last = false;
        for (boolean b : Values.interestingBooleans) {
            toTrue.ifExpected = b && !last;
            bo.set(b);
            toTrue.check();
            last = b;
        }
    }

    @Test
    public void testOnChangeNullB() {
        CountingEventOutput toFalse = new CountingEventOutput();
        BooleanOutput bo = BooleanOutput.onChange(toFalse, null);
        boolean last = false;
        for (boolean b : Values.interestingBooleans) {
            toFalse.ifExpected = !b && last;
            bo.set(b);
            toFalse.check();
            last = b;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnChangeNullAB() {
        BooleanOutput.onChange(null, null);
    }

    @Test
    public void testFilter() {
        for (boolean not : new boolean[] { false, true }) {
            BooleanCell allowDeny = new BooleanCell(true), out = new BooleanCell();
            BooleanOutput bo = not ? out.filterNot(allowDeny) : out.filter(allowDeny);
            boolean expect = false, next = false;
            for (int i = 0; i < 110; i++) {
                boolean nvalue = ((i % 19) < 10) ^ not;
                if (allowDeny.get() == not && nvalue != not) {
                    // switching to active: expect an update as necessary
                    bo.set(next);
                    assertEquals(expect, out.get());

                    allowDeny.set(nvalue);
                    expect = next;

                    assertEquals(expect, out.get());

                    next = !next;
                } else {
                    allowDeny.set(nvalue);
                }

                bo.set(next);
                if (allowDeny.get() ^ not) {
                    expect = next;
                }
                assertEquals(expect, out.get());
                next = !next;
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFilterNull() {
        cbo.filter(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFilterNotNull() {
        cbo.filterNot(null);
    }

    @Test
    public void testSafeSet() {
        evil.safeSet(false);
        evil.safeSet(true);
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionPropagationFalse() {
        evil.set(false);
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionPropagationTrue() {
        evil.set(true);
    }

    @Test(expected = NoSuchElementException.class)
    public void testCombineWithError1CausesError() {
        cbo.ifExpected = true;
        cbo.valueExpected = true;
        cbo.combine(evil).set(true);
    }

    @Test(expected = NoSuchElementException.class)
    public void testCombineWithError2CausesError() {
        cbo.ifExpected = true;
        cbo.valueExpected = true;
        evil.combine(cbo).set(true);
    }

    @Test
    public void testCombineWithError1Succeeds() {
        cbo.ifExpected = true;
        cbo.valueExpected = true;
        cbo.combine(evil).safeSet(true);
        cbo.check();
    }

    @Test
    public void testCombineWithError2Succeeds() {
        cbo.ifExpected = true;
        cbo.valueExpected = true;
        evil.combine(cbo).safeSet(true);
        cbo.check();
    }

    @Test
    public void testCombineWithError3CausesError() {
        boolean errored = false;
        try {
            evil.combine(evil).set(true);
        } catch (NoSuchElementException ex) {
            errored = true;
            assertEquals(ex.getSuppressed().length, 1);
            assertTrue(ex.getSuppressed()[0] instanceof NoSuchElementException);
        }
        assertTrue(errored);
    }
}
