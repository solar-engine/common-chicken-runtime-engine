/*
 * Copyright 2015-2016 Colby Skeggs
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
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.log.LogLevel;
import ccre.log.VerifyingLogger;
import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class BooleanOutputTest {

    private static final String ERR_STRING = "safeSet purposeful failure.";
    private final BooleanOutput evil = (v) -> {
        throw new NoSuchElementException(ERR_STRING);
    };
    private CountingBooleanOutput cbo, cbo2;

    @Before
    public void setUp() throws Exception {
        cbo = new CountingBooleanOutput();
        cbo2 = new CountingBooleanOutput();
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() throws Exception {
        VerifyingLogger.checkAndEnd();
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
    public void testIgnoredCombine() {
        assertEquals(cbo, BooleanOutput.ignored.combine(cbo));
    }

    @Test(expected = NullPointerException.class)
    public void testIgnoredCombineNull() {
        BooleanOutput.ignored.combine((BooleanOutput) null);
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
    public void testStaticCombineSimplification() {
        assertEquals(BooleanOutput.combine(), BooleanOutput.ignored);
        assertEquals(BooleanOutput.combine(new BooleanOutput[] { cbo }), cbo);
    }

    @Test
    public void testStaticCombine() {
        for (int n = 0; n < 20; n++) {
            CountingBooleanOutput[] cbos = new CountingBooleanOutput[n];
            for (int i = 0; i < n; i++) {
                cbos[i] = new CountingBooleanOutput();
            }
            BooleanOutput combined = BooleanOutput.combine(cbos);
            for (int l = 0; l < 10; l++) {
                for (int i = 0; i < n; i++) {
                    cbos[i].ifExpected = true;
                    cbos[i].valueExpected = l % 2 == 0;
                }
                combined.set(l % 2 == 0);
                for (int i = 0; i < n; i++) {
                    cbos[i].check();
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNull() {
        BooleanOutput.combine((BooleanOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullElem() {
        BooleanOutput.combine(new BooleanOutput[] { null });
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullEarlierElem() {
        BooleanOutput.combine(null, cbo);
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullLaterElem() {
        BooleanOutput.combine(cbo, null);
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
    public void testPolarize() {
        CountingEventOutput toFalse = new CountingEventOutput(), toTrue = new CountingEventOutput();
        BooleanOutput bo = BooleanOutput.polarize(toFalse, toTrue);
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
    public void testPolarizeNullA() {
        CountingEventOutput toTrue = new CountingEventOutput();
        BooleanOutput bo = BooleanOutput.polarize(null, toTrue);
        boolean last = false;
        for (boolean b : Values.interestingBooleans) {
            toTrue.ifExpected = b && !last;
            bo.set(b);
            toTrue.check();
            last = b;
        }
    }

    @Test
    public void testPolarizeNullB() {
        CountingEventOutput toFalse = new CountingEventOutput();
        BooleanOutput bo = BooleanOutput.polarize(toFalse, null);
        boolean last = false;
        for (boolean b : Values.interestingBooleans) {
            toFalse.ifExpected = !b && last;
            bo.set(b);
            toFalse.check();
            last = b;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testPolarizeNullAB() {
        BooleanOutput.polarize(null, null);
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
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERR_STRING.equals(t.getMessage()));
        evil.safeSet(false);
        VerifyingLogger.check();
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERR_STRING.equals(t.getMessage()));
        evil.safeSet(true);
        VerifyingLogger.check();
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
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERR_STRING.equals(t.getMessage()));
        cbo.combine(evil).safeSet(true);
        VerifyingLogger.check();
        cbo.check();
    }

    @Test
    public void testCombineWithError2Succeeds() {
        cbo.ifExpected = true;
        cbo.valueExpected = true;
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERR_STRING.equals(t.getMessage()));
        evil.combine(cbo).safeSet(true);
        VerifyingLogger.check();
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

    @Test
    public void testStaticCombineSingleError() {
        for (int n = 1; n < 6; n++) {
            CountingBooleanOutput[] cbos = new CountingBooleanOutput[n];
            for (int i = 0; i < n; i++) {
                cbos[i] = new CountingBooleanOutput();
            }
            for (int bad = 0; bad < n; bad++) {
                BooleanOutput[] reals = new BooleanOutput[n];
                System.arraycopy(cbos, 0, reals, 0, n);
                reals[bad] = evil;
                BooleanOutput combined = BooleanOutput.combine(reals);
                for (int l = 0; l < 10; l++) {
                    for (int i = 0; i < n; i++) {
                        cbos[i].ifExpected = i != bad;
                        cbos[i].valueExpected = l % 2 == 0;
                    }
                    try {
                        combined.set(l % 2 == 0);
                        fail();
                    } catch (NoSuchElementException ex) {
                        assertEquals(0, ex.getSuppressed().length);
                    }
                    for (int i = 0; i < n; i++) {
                        cbos[i].check();
                    }
                }
            }
        }
    }

    @Test
    public void testStaticCombineManyErrors() {
        for (int n = 1; n < 6; n++) {
            CountingBooleanOutput[] cbos = new CountingBooleanOutput[n];
            for (int i = 0; i < n; i++) {
                cbos[i] = new CountingBooleanOutput();
            }
            for (int bad = 0; bad < 4 * n * n; bad++) {
                boolean[] evils = new boolean[n];
                int evil_count = 0;
                BooleanOutput[] reals = new BooleanOutput[n];
                System.arraycopy(cbos, 0, reals, 0, n);
                for (int i = 0; i < n; i++) {
                    evils[i] = Values.getRandomBoolean();
                    if (evils[i]) {
                        evil_count++;
                        reals[i] = evil;
                    }
                }
                BooleanOutput combined = BooleanOutput.combine(reals);
                for (int l = 0; l < 10; l++) {
                    for (int i = 0; i < n; i++) {
                        cbos[i].ifExpected = !evils[i];
                        cbos[i].valueExpected = l % 2 == 0;
                    }
                    try {
                        combined.set(l % 2 == 0);
                        assertEquals(0, evil_count);
                    } catch (NoSuchElementException ex) {
                        Throwable[] suppressed = ex.getSuppressed();
                        assertEquals(evil_count - 1, suppressed.length);
                        for (int i = 0; i < suppressed.length; i++) {
                            assertTrue(suppressed[i] instanceof NoSuchElementException);
                        }
                    }
                    for (int i = 0; i < n; i++) {
                        cbos[i].check();
                    }
                }
            }
        }
    }

    @Test
    public void testCell() {
        for (boolean ob : Values.interestingBooleans) {
            BooleanOutput out = cbo::set;
            cbo.ifExpected = true;
            cbo.valueExpected = ob;
            BooleanIO bio = out.cell(ob);
            cbo.check();
            cbo2.ifExpected = true;
            cbo2.valueExpected = ob;
            bio.send(cbo2);
            cbo2.check();
            for (int i = 0; i < 20; i++) {
                boolean b = Values.getRandomBoolean();
                cbo.ifExpected = cbo2.ifExpected = bio.get() != b;
                cbo.valueExpected = cbo2.valueExpected = b;
                bio.set(b);
                cbo2.check();
                cbo.check();
                assertEquals(b, bio.get());
            }
        }
    }
}
