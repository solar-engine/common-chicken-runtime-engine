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
package ccre.testing;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.ctrl.BooleanMixing;

/**
 * Tests the BooleanMixing class.
 *
 * @author skeggsc
 */
public class TestBooleanMixing extends BaseTest {

    /**
     * An "interesting" sequence of booleans to test with, such that edge cases
     * with things like repetition of the same value can be caught.
     */
    public static final boolean[] interestingBooleans = new boolean[] { false, true, true, false, false, true, false, true, false, true, true, true, false, true, false, false, false };

    @Override
    public String getName() {
        return "BooleanMixing test";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        testIgnored();
        testFilters();
        testCombine();
        testAlgebra();
        testAlgebraPoly();
        testChangeMonitors();
        testSetWhen();
    }

    private void testIgnored() throws TestingException, InterruptedException {
        // Not much of any way to test these...
        BooleanMixing.ignored.set(false);
        BooleanMixing.ignored.set(true);

        assertFalse(BooleanMixing.alwaysFalse.get(), "False should be.");
        assertTrue(BooleanMixing.alwaysTrue.get(), "True should be.");

        final int[] found = new int[1];
        BooleanOutput fsend = new BooleanOutput() {
            public void set(boolean value) {
                if (value) {
                    throw new RuntimeException("Should be false.");
                }
                found[0]++;
            }
        };
        BooleanMixing.alwaysFalse.send(fsend);
        assertIntsEqual(found[0], 1, "Should have been sent initial value of false.");
        BooleanMixing.alwaysFalse.unsend(fsend);
        assertIntsEqual(found[0], 1, "Values should still be unchanged.");

        BooleanOutput tsend = new BooleanOutput() {
            public void set(boolean value) {
                if (!value) {
                    throw new RuntimeException("Should be true.");
                }
                found[0]++;
            }
        };
        BooleanMixing.alwaysTrue.send(tsend);
        assertIntsEqual(found[0], 2, "Should have been sent initial value of true.");
        Thread.sleep(50);
        assertIntsEqual(found[0], 2, "Values should still be unchanged.");
        BooleanMixing.alwaysTrue.unsend(tsend);
        assertIntsEqual(found[0], 2, "Values should still be unchanged.");

        assertFalse(BooleanMixing.alwaysFalse.get(), "False should be.");
        assertTrue(BooleanMixing.alwaysTrue.get(), "True should be.");
    }

    private void testFilters() throws TestingException {
        BooleanStatus test = new BooleanStatus();
        BooleanStatus test2 = new BooleanStatus();
        BooleanInput wrapped = BooleanMixing.invert((BooleanInput) test);
        wrapped.send(test2);

        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertFalse(test.get(), "Should be false.");
        test.set(true);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertTrue(test.get(), "Should be true.");
        test.set(false);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertFalse(test.get(), "Should be false.");
        test.set(true);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertTrue(test.get(), "Should be true.");
        test.set(false);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertFalse(test.get(), "Should be false.");

        BooleanOutput wrapout = BooleanMixing.invert((BooleanOutput) test);
        wrapout.set(true);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertFalse(test.get(), "Should be false.");
        wrapout.set(false);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertTrue(test.get(), "Should be true.");
        wrapout.set(true);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertFalse(test.get(), "Should be false.");
        wrapout.set(false);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertTrue(test.get(), "Should be true.");
        wrapout.set(true);
        assertTrue(test.get() != wrapped.get(), "Values should be opposites.");
        assertTrue(test.get() != test2.get(), "Values should be opposites.");
        assertFalse(test.get(), "Should be false.");
    }

    private void testCombine() throws TestingException {
        BooleanStatus a = new BooleanStatus(), b = new BooleanStatus(), c = new BooleanStatus();
        BooleanOutput d = BooleanMixing.combine(a, b), e = BooleanMixing.combine(a, b, c);

        assertFalse(a.get() || b.get() || c.get(), "Should all be off.");
        e.set(false);
        assertFalse(a.get() || b.get() || c.get(), "Should all still be off.");
        e.set(true);
        assertTrue(a.get() && b.get() && c.get(), "Should all be on.");
        e.set(true);
        assertTrue(a.get() && b.get() && c.get(), "Should all still be on.");
        e.set(false);
        assertFalse(a.get() || b.get() || c.get(), "Should all be off.");

        assertFalse(a.get() || b.get(), "Should both be off.");
        assertFalse(c.get(), "c should be off.");
        d.set(false);
        assertFalse(a.get() || b.get(), "Should both still be off.");
        assertFalse(c.get(), "c should be off.");
        d.set(true);
        assertTrue(a.get() && b.get(), "Should both be on.");
        assertFalse(c.get(), "c should be off.");
        d.set(true);
        c.set(true);
        assertTrue(a.get() && b.get(), "Should both still be on.");
        assertTrue(c.get(), "c should be on.");
        d.set(false);
        assertFalse(a.get() || b.get(), "Should both be off.");
        assertTrue(c.get(), "c should be on.");
    }

    private void testAlgebra() throws TestingException {
        BooleanStatus a = new BooleanStatus(), b = new BooleanStatus();
        BooleanInput xor = BooleanMixing.xorBooleans(a, b);
        BooleanInput or = BooleanMixing.orBooleans(a, b);
        BooleanInput and = BooleanMixing.andBooleans(a, b);
        BooleanStatus orStat = new BooleanStatus(), andStat = new BooleanStatus(), xorStat = new BooleanStatus();
        BooleanMixing.orBooleans(a, b).send(orStat);
        BooleanMixing.andBooleans(a, b).send(andStat);
        BooleanMixing.xorBooleans(a, b).send(xorStat);

        a.set(false);
        b.set(false);
        assertFalse(xor.get(), "Bad xor");
        assertFalse(or.get(), "Bad or");
        assertFalse(and.get(), "Bad and");
        assertFalse(xorStat.get(), "Bad xor");
        assertFalse(orStat.get(), "Bad or");
        assertFalse(andStat.get(), "Bad and");

        a.set(true);
        b.set(false);
        assertTrue(xor.get(), "Bad xor");
        assertTrue(or.get(), "Bad or");
        assertFalse(and.get(), "Bad and");
        assertTrue(xorStat.get(), "Bad xor");
        assertTrue(orStat.get(), "Bad or");
        assertFalse(andStat.get(), "Bad and");

        a.set(false);
        b.set(true);
        assertTrue(xor.get(), "Bad xor");
        assertTrue(or.get(), "Bad or");
        assertFalse(and.get(), "Bad and");
        assertTrue(xorStat.get(), "Bad xor");
        assertTrue(orStat.get(), "Bad or");
        assertFalse(andStat.get(), "Bad and");

        a.set(true);
        b.set(true);
        assertFalse(xor.get(), "Bad xor");
        assertTrue(or.get(), "Bad or");
        assertTrue(and.get(), "Bad and");
        assertFalse(xorStat.get(), "Bad xor");
        assertTrue(orStat.get(), "Bad or");
        assertTrue(andStat.get(), "Bad and");

        a.set(false);
        b.set(false);
        assertFalse(xor.get(), "Bad xor");
        assertFalse(or.get(), "Bad or");
        assertFalse(and.get(), "Bad and");
        assertFalse(xorStat.get(), "Bad xor");
        assertFalse(orStat.get(), "Bad or");
        assertFalse(andStat.get(), "Bad and");
    }

    private void testAlgebraPoly() throws TestingException {
        BooleanStatus a = new BooleanStatus(), b = new BooleanStatus(), c = new BooleanStatus();
        BooleanInput or = BooleanMixing.orBooleans(a, b, c);
        BooleanInput and = BooleanMixing.andBooleans(a, b, c);
        BooleanStatus orStat = new BooleanStatus(), andStat = new BooleanStatus();
        BooleanMixing.orBooleans(a, b, c).send(orStat);
        BooleanMixing.andBooleans(a, b, c).send(andStat);

        for (int i = 0; i < 16; i++) {
            a.set((i & 1) != 0);
            b.set((i & 2) != 0);
            c.set((i & 4) != 0);
            boolean orReal = ((i & 7) != 0);
            boolean andReal = ((i & 7) == 7);
            assertTrue(or.get() == orReal, "Bad or");
            assertTrue(and.get() == andReal, "Bad and");
            assertTrue(orStat.get() == orReal, "Bad or");
            assertTrue(andStat.get() == andReal, "Bad and");
        }
    }

    private void testChangeMonitors() throws TestingException {
        final int[] counts = new int[4];
        BooleanOutput out = BooleanMixing.triggerWhenBooleanChanges(new EventOutput() {
            public void event() {
                counts[0]++;
            }
        }, new EventOutput() {
            public void event() {
                counts[1]++;
            }
        });
        BooleanStatus out2 = new BooleanStatus();
        BooleanMixing.onRelease(out2).send(new EventOutput() {
            public void event() {
                counts[2]++;
            }
        });
        BooleanMixing.onPress(out2).send(new EventOutput() {
            public void event() {
                counts[3]++;
            }
        });

        int a = 0, b = 0;
        boolean last = false;

        for (boolean v : interestingBooleans) {
            if (v != last) {
                if (v) {
                    b++;
                } else {
                    a++;
                }
            }
            out.set(v);
            out2.set(v);
            assertIntsEqual(counts[0], a, "Bad count.");
            assertIntsEqual(counts[1], b, "Bad count.");
            assertIntsEqual(counts[2], a, "Bad count.");
            assertIntsEqual(counts[3], b, "Bad count.");
            last = v;
        }
    }

    private void testSetWhen() throws TestingException {
        final boolean[] expecting = new boolean[2];
        BooleanOutput out = new BooleanOutput() {
            public void set(boolean value) {
                if (value != expecting[0]) {
                    throw new RuntimeException("Unexpected value!");
                }
                if (!expecting[1]) {
                    throw new RuntimeException("Value at unexpected time!");
                }
                expecting[1] = false;
            }
        };
        EventStatus setTrue = new EventStatus(), setFalse = new EventStatus();
        BooleanMixing.setWhen(setTrue, out, true);
        BooleanMixing.setWhen(setFalse, out, false);

        for (boolean b : interestingBooleans) {
            expecting[0] = b;
            expecting[1] = true;
            (b ? setTrue : setFalse).event();
            assertFalse(expecting[1], "Value not received.");
        }
    }
}
