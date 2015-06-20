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
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.ctrl.FloatMixing;
import ccre.util.Utils;

/**
 * Tests the FloatMixing class.
 *
 * @author skeggsc
 */
public class TestFloatMixing extends BaseTest {

    public class CountingFloatOutput implements FloatOutput {
        public float valueExpected;
        public boolean ifExpected;

        public synchronized void set(float value) {
            if (!ifExpected) {
                throw new RuntimeException("Unexpected set!");
            }
            ifExpected = false;
            if (value != valueExpected && !(Float.isNaN(value) && Float.isNaN(valueExpected))) {
                throw new RuntimeException("Incorrect set!");
            }
        }

        public void check() {
            if (ifExpected) {
                throw new RuntimeException("Did not get expected set!");
            }
        }
    }

    public static final float[] interestingFloats = new float[] { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f, Float.MAX_VALUE, Float.POSITIVE_INFINITY };
    public static final float[] lessInterestingFloats = new float[] { -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f };

    @Override
    public String getName() {
        return "FloatMixing test";
    }

    @Override
    protected void runTest() throws Throwable {
        testOperations();
        testFilters();
        FloatMixing.ignored.set(0);
        testGetSetEvent();
        testSimpleComparisons();
        testRangeComparisons();
        testCombine();
        testRamping();
    }

    private void testRamping() throws TestingException {
        for (float limit : lessInterestingFloats) {
            CountingFloatOutput out = new CountingFloatOutput();
            EventStatus updateWhen = new EventStatus();
            FloatOutput rampout = FloatMixing.addRamping(limit, updateWhen, out);

            FloatStatus rampstat = new FloatStatus();
            FloatInput gotten = FloatMixing.addRamping(limit, updateWhen, rampstat.asInput());

            float lastValue = 0.0f;
            for (float cmp1 : interestingFloats) {
                for (float cmp2 : interestingFloats) {
                    float value = cmp1 - cmp2; // to get more and more interesting numbers here

                    out.valueExpected = lastValue = Utils.updateRamping(lastValue, value, limit);
                    rampout.set(value);
                    rampstat.set(value);
                    out.ifExpected = true;
                    updateWhen.event();
                    out.check();
                    assertObjectEqual(gotten.get(), lastValue, "mismatched input ramping");
                }
            }
        }
    }

    private void testRangeComparisons() throws TestingException {
        for (float min : interestingFloats) {
            for (float max : interestingFloats) {
                FloatStatus value = new FloatStatus();
                if (Float.isNaN(min) || Float.isNaN(max)) {
                    try {
                        FloatMixing.floatIsInRange(value, min, max);
                        assertFail("Expected a thrown exception for NaN bound!");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    try {
                        FloatMixing.floatIsInRange((FloatInputPoll) value, min, max);
                        assertFail("Expected a thrown exception for NaN bound!");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    try {
                        FloatMixing.floatIsOutsideRange(value, min, max);
                        assertFail("Expected a thrown exception for NaN bound!");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    try {
                        FloatMixing.floatIsOutsideRange((FloatInputPoll) value, min, max);
                        assertFail("Expected a thrown exception for NaN bound!");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    continue;
                }
                BooleanInput in = FloatMixing.floatIsInRange(value, min, max);
                BooleanInputPoll inp = FloatMixing.floatIsInRange((FloatInputPoll) value, min, max);
                BooleanInput out = FloatMixing.floatIsOutsideRange(value, min, max);
                BooleanInputPoll outp = FloatMixing.floatIsOutsideRange((FloatInputPoll) value, min, max);
                for (float v : interestingFloats) {
                    value.set(v);
                    if (Float.isNaN(v)) {
                        assertFalse(in.get() || inp.get() || out.get() || outp.get(), "bad NaN handling");
                        continue;
                    }
                    assertTrue(in.get() == (min <= v && v <= max), "bad in range");
                    assertTrue(in.get() == inp.get(), "inconsistent with in");
                    assertTrue(in.get() != out.get(), "inconsistent with out: " + min + "," + v + "," + max + ": " + in.get() + " " + out.get());
                    assertTrue(inp.get() != outp.get(), "inconsistent with out");
                }
            }
        }
    }

    private void testCombine() {
        CountingFloatOutput a = new CountingFloatOutput(), b = new CountingFloatOutput(), c = new CountingFloatOutput();
        FloatOutput c2 = FloatMixing.combine(a, b), c3 = FloatMixing.combine(a, b, c);

        for (float f : interestingFloats) {
            a.valueExpected = b.valueExpected = c.valueExpected = f;

            a.ifExpected = b.ifExpected = true;
            c2.set(f);
            a.check();
            b.check();

            a.ifExpected = b.ifExpected = c.ifExpected = true;
            c3.set(f);
            a.check();
            b.check();
            c.check();
        }
    }

    private void testSimpleComparisons() throws TestingException {
        FloatStatus val = new FloatStatus(), comparison = new FloatStatus();
        BooleanInput al0 = FloatMixing.floatIsAtLeast(val, 1f);
        BooleanInput al1 = FloatMixing.floatIsAtLeast(val, comparison);
        BooleanInputPoll al2 = FloatMixing.floatIsAtLeast((FloatInputPoll) val, 1f);
        BooleanInputPoll al3 = FloatMixing.floatIsAtLeast((FloatInputPoll) val, comparison);
        BooleanInput am0 = FloatMixing.floatIsAtMost(val, 1f);
        BooleanInput am1 = FloatMixing.floatIsAtMost(val, comparison);
        BooleanInputPoll am2 = FloatMixing.floatIsAtMost((FloatInputPoll) val, 1f);
        BooleanInputPoll am3 = FloatMixing.floatIsAtMost((FloatInputPoll) val, comparison);
        for (float f : interestingFloats) {
            val.set(f);
            assertTrue(al0.get() == (f >= 1f), "bad least comparison");
            assertTrue(am0.get() == (f <= 1f), "bad most comparison");
            assertTrue(al2.get() == (f >= 1f), "bad least comparison");
            assertTrue(am2.get() == (f <= 1f), "bad most comparison");
            for (float c : interestingFloats) {
                comparison.set(c);
                assertTrue(al3.get() == (f >= c), "bad least comparison");
                assertTrue(am3.get() == (f <= c), "bad most comparison");

                // last because we need to resend val - this is ugly...
                val.set(f == 0.0f ? 1.0f : 0.0f);
                val.set(f);
                assertTrue(al1.get() == (f >= c), "bad least comparison: " + f + " versus " + c + ": " + al1.get() + " vs " + (f >= c));
                assertTrue(am1.get() == (f <= c), "bad most comparison");
            }
        }
    }

    private void testGetSetEvent() throws TestingException {
        FloatStatus val = new FloatStatus();
        for (float f : interestingFloats) {
            FloatMixing.getSetEvent(val, f).event();
            assertObjectEqual(val.get(), f, "bad set event");
        }
    }

    private void testFilters() throws TestingException {
        FloatStatus in = new FloatStatus();
        FloatInput out = FloatMixing.negate(in.asInput());
        FloatInputPoll out2 = FloatMixing.negate((FloatInputPoll) in);
        FloatOutput nset = FloatMixing.negate(in.asOutput());
        for (float f : interestingFloats) {
            in.set(f);
            assertObjectEqual(out.get(), -f, "bad negation");
            assertObjectEqual(out2.get(), -f, "bad negation");
            in.set(-f);
            assertObjectEqual(out.get(), f, "bad negation");
            assertObjectEqual(out2.get(), f, "bad negation");
            nset.set(-f);
            assertObjectEqual(out.get(), -f, "bad negation");
            nset.set(f);
            assertObjectEqual(out.get(), f, "bad negation");
        }
        for (float low : interestingFloats) {
            for (float high : interestingFloats) {
                FloatInputPoll test;
                try {
                    test = FloatMixing.limit(low, high).wrap((FloatInputPoll) in);
                    assertFalse(high < low, "no IAE when expected!");
                } catch (IllegalArgumentException ex) {
                    assertTrue(high < low, "IAE when unexpected!");
                    continue;
                }
                for (float value : interestingFloats) {
                    in.set(value);
                    float gotten = test.get();
                    if (Float.isNaN(value)) {
                        assertTrue(Float.isNaN(gotten), "bad NaN handling");
                        continue;
                    }
                    assertTrue((gotten >= low || Float.isNaN(low)) && (gotten <= high || Float.isNaN(high)), "limit failed: " + gotten + " from " + low + "," + value + "," + high);
                    if (gotten != value) {
                        if (gotten == low) {
                            if (low == high) {
                                assertTrue(value <= low || value >= high, "wrong roundoff");
                            } else {
                                assertTrue(value <= low, "wrong roundoff");
                            }
                        } else if (gotten == high) {
                            assertTrue(value >= high, "wrong roundoff");
                        } else {
                            assertFail("bad roundoff");
                        }
                    }
                }
            }
        }
    }

    private final FloatStatus a = new FloatStatus(), b = new FloatStatus();

    private void checkOperation(FloatInput inp, float a, float b, float out, float rev) throws TestingException {
        this.a.set(a);
        this.b.set(b);
        assertObjectEqual(inp.get(), out, "operation result mismatch");
        this.a.set(b);
        this.b.set(a);
        assertObjectEqual(inp.get(), rev, "reversed operation result mismatch");
    }

    private void testOperations() throws TestingException {
        FloatInput add = FloatMixing.addition.of(a.asInput(), b.asInput());
        checkOperation(add, 0, 0, 0, 0);
        checkOperation(add, 1, 0, 1, 1);
        checkOperation(add, 1, 1, 2, 2);
        checkOperation(add, 17.3f, -18.6f, 17.3f + -18.6f, 17.3f + -18.6f);
        checkOperation(add, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        FloatInput sub = FloatMixing.subtraction.of(a.asInput(), b.asInput());
        checkOperation(sub, 0, 0, 0, 0);
        checkOperation(sub, 1, 0, 1, -1);
        checkOperation(sub, 1, 1, 0, 0);
        checkOperation(sub, 17.3f, -18.6f, 35.9f, -35.9f);
        checkOperation(sub, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);

        FloatInput mul = FloatMixing.multiplication.of(a.asInput(), b.asInput());
        checkOperation(mul, 0, 0, 0, 0);
        checkOperation(mul, 1, 0, 0, 0);
        checkOperation(mul, 1, 1, 1, 1);
        checkOperation(mul, 17.3f, -18.6f, 17.3f * -18.6f, 17.3f * -18.6f);
        checkOperation(mul, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        FloatInput div = FloatMixing.division.of(a.asInput(), b.asInput());
        checkOperation(div, 0, 0, Float.NaN, Float.NaN);
        checkOperation(div, 1, 0, Float.POSITIVE_INFINITY, 0);
        checkOperation(div, 1, 1, 1, 1);
        checkOperation(div, 17.3f, -18.6f, 17.3f / -18.6f, -18.6f / 17.3f);
        checkOperation(div, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, 0);
    }
}
