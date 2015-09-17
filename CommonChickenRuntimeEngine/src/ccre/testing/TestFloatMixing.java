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
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.util.Utils;

/**
 * Tests the FloatMixing class.
 *
 * @author skeggsc
 */
public class TestFloatMixing extends BaseTest {

    // TODO: use these everywhere relevant
    /**
     * A sequence of interesting floats for testing edge cases: things like
     * negative infinity, NaN, MAX_VALUE, -MAX_VALUE, 0, 1, -1, etc.
     *
     * @see TestFloatMixing#lessInterestingFloats
     */
    public static final float[] interestingFloats = new float[] { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f, Float.MAX_VALUE, Float.POSITIVE_INFINITY };
    /**
     * A sequence of slightly less interesting floats for testing edge cases:
     * this is like {@link #interestingFloats}, but with only finite values not
     * near MAX_VALUE in magnitude.
     *
     * @see TestFloatMixing#interestingFloats
     */
    public static final float[] lessInterestingFloats = new float[] { -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f };

    @Override
    public String getName() {
        return "FloatMixing test";
    }

    @Override
    protected void runTest() throws Throwable {
        testOperations();
        testNegation();
        FloatOutput.ignored.set(0);
        testGetSetEvent();
        testSimpleComparisons();
        testRangeComparisons();
        testCombine();
        testRamping();
        testAlways();
        testNormalize();
    }

    private void testNormalize() throws TestingException {
        FloatStatus value = new FloatStatus();
        FloatStatus low = new FloatStatus(), high = new FloatStatus();
        FloatInput norm1 = value.normalize(low, high);
        for (float lowV : interestingFloats) {
            low.set(lowV);
            for (float highV : interestingFloats) {
                high.set(highV);
                FloatInput norm2;
                if (!Float.isFinite(lowV) || !Float.isFinite(highV) || !Float.isFinite(highV - lowV)) {
                    try {
                        value.normalize(lowV, highV);
                        assertFail("expected failure due to non-finite parameter or range");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    continue;// undefined behavior - don't care at all
                } else if (lowV == highV) {
                    try {
                        value.normalize(lowV, highV);
                        assertFail("expected failure due to zero range");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    norm2 = FloatInput.always(Float.NaN);
                } else {
                    norm2 = value.normalize(lowV, highV);
                }
                for (float v : interestingFloats) {
                    value.set(v);
                    // check sameness
                    assertObjectEqual(norm1.get(), norm2.get(), "inconsistent");
                    // check correctness
                    float scaled = norm1.get();
                    if (Float.isNaN(lowV) || Float.isNaN(highV) || Float.isNaN(v) || (lowV == highV)) {
                        assertTrue(Float.isNaN(scaled), "bad NaN handling");
                    } else {
                        assertFalse(Float.isNaN(scaled), "bad NaN handling: " + lowV + "," + highV + "," + v + " (range " + (highV - lowV) + ")");
                        float unscaled = scaled * (highV - lowV) + lowV;
                        assertTrue(unscaled == v || Math.abs(unscaled - v) <= Math.max(Math.max(Math.abs(unscaled), Math.abs(v)), Math.max(Math.abs(lowV), Math.abs(highV))) * 0.0001f, "non-reversible calculation: " + unscaled + " vs " + v);
                    }
                }
            }
        }
    }

    private void testAlways() {
        for (float value : interestingFloats) {
            CountingFloatOutput out = new CountingFloatOutput();
            out.ifExpected = true;
            out.valueExpected = value;
            FloatInput vall = FloatInput.always(value);
            EventOutput unbind = vall.sendR(out);
            out.check();
            unbind.event();
        }
    }

    private void testRamping() throws TestingException {
        for (float limit : lessInterestingFloats) {
            CountingFloatOutput out = new CountingFloatOutput();
            EventStatus updateWhen = new EventStatus();
            FloatOutput rampout = out.addRamping(limit, updateWhen);

            FloatStatus rampstat = new FloatStatus();
            FloatInput gotten = rampstat.withRamping(limit, updateWhen);

            float lastValue = 0.0f;
            for (float cmp1 : interestingFloats) {
                for (float cmp2 : interestingFloats) {
                    float value = cmp1 - cmp2;// to get more and more interesting numbers here

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
                        value.inRange(min, max);
                        assertFail("Expected a thrown exception for NaN bound!");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    try {
                        value.outsideRange(min, max);
                        value.outsideRange(min, max);
                        assertFail("Expected a thrown exception for NaN bound!");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    continue;
                }
                BooleanInput in = value.inRange(min, max);
                BooleanInput out = value.outsideRange(min, max);
                for (float v : interestingFloats) {
                    value.set(v);
                    if (Float.isNaN(v)) {
                        assertFalse(in.get() || out.get(), "bad NaN handling");
                        continue;
                    }
                    assertTrue(in.get() == (min <= v && v <= max), "bad in range");
                    assertTrue(in.get() != out.get(), "inconsistent with out: " + min + "," + v + "," + max + ": " + in.get() + " " + out.get());
                }
            }
        }
    }

    private void testCombine() {
        CountingFloatOutput a = new CountingFloatOutput(), b = new CountingFloatOutput();
        FloatOutput c2 = a.combine(b);

        for (float f : interestingFloats) {
            a.valueExpected = b.valueExpected = f;

            a.ifExpected = b.ifExpected = true;
            c2.set(f);
            a.check();
            b.check();
        }
    }

    private void testSimpleComparisons() throws TestingException {
        FloatStatus val = new FloatStatus(), comparison = new FloatStatus();
        BooleanInput al0 = val.atLeast(1f);
        BooleanInput al1 = val.atLeast(comparison);
        BooleanInput am0 = val.atMost(1f);
        BooleanInput am1 = val.atMost(comparison);
        for (float f : interestingFloats) {
            val.set(f);
            assertTrue(al0.get() == (f >= 1f), "bad least comparison");
            assertTrue(am0.get() == (f <= 1f), "bad most comparison");
            for (float c : interestingFloats) {
                comparison.set(c);
                // last because we need to resend val - this is ugly...
                // TODO: still necessary?
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
            val.getSetEvent(f).event();
            assertObjectEqual(val.get(), f, "bad set event");
        }
    }

    private void testNegation() throws TestingException {
        FloatStatus in = new FloatStatus();
        FloatInput out = in.negated();
        FloatOutput nset = in.negate();
        for (float f : interestingFloats) {
            in.set(f);
            assertObjectEqual(out.get(), -f, "bad negation");
            in.set(-f);
            assertObjectEqual(out.get(), f, "bad negation");
            nset.set(-f);
            assertObjectEqual(out.get(), -f, "bad negation");
            nset.set(f);
            assertObjectEqual(out.get(), f, "bad negation");
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
        FloatInput add = a.plus(b);
        checkOperation(add, 0, 0, 0, 0);
        checkOperation(add, 1, 0, 1, 1);
        checkOperation(add, 1, 1, 2, 2);
        checkOperation(add, 17.3f, -18.6f, 17.3f + -18.6f, 17.3f + -18.6f);
        checkOperation(add, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        FloatInput sub = a.minus(b);
        checkOperation(sub, 0, 0, 0, 0);
        checkOperation(sub, 1, 0, 1, -1);
        checkOperation(sub, 1, 1, 0, 0);
        checkOperation(sub, 17.3f, -18.6f, 35.9f, -35.9f);
        checkOperation(sub, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);

        FloatInput mul = a.multipliedBy(b);
        checkOperation(mul, 0, 0, 0, 0);
        checkOperation(mul, 1, 0, 0, 0);
        checkOperation(mul, 1, 1, 1, 1);
        checkOperation(mul, 17.3f, -18.6f, 17.3f * -18.6f, 17.3f * -18.6f);
        checkOperation(mul, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        FloatInput div = a.dividedBy(b);
        checkOperation(div, 0, 0, Float.NaN, Float.NaN);
        checkOperation(div, 1, 0, Float.POSITIVE_INFINITY, 0);
        checkOperation(div, 1, 1, 1, 1);
        checkOperation(div, 17.3f, -18.6f, 17.3f / -18.6f, -18.6f / 17.3f);
        checkOperation(div, Float.POSITIVE_INFINITY, 177, Float.POSITIVE_INFINITY, 0);
    }
}
