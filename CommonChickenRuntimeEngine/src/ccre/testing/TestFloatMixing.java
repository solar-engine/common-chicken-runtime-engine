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
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventStatus;
import ccre.channel.FloatFilter;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.ctrl.FloatMixing;
import ccre.testing.TestEventMixing.CountingEventOutput;
import ccre.util.Utils;

/**
 * Tests the FloatMixing class.
 *
 * @author skeggsc
 */
public class TestFloatMixing extends BaseTest {

    public static class CountingFloatOutput implements FloatOutput {
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

    // TODO: use these everywhere relevant
    public static final float[] interestingFloats = new float[] { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f, Float.MAX_VALUE, Float.POSITIVE_INFINITY };
    public static final float[] lessInterestingFloats = new float[] { -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f };

    @Override
    public String getName() {
        return "FloatMixing test";
    }

    @Override
    protected void runTest() throws Throwable {
        testOperations();
        testNegation();
        testLimits();
        testLimitsSimple();
        FloatMixing.ignored.set(0);
        testGetSetEvent();
        testSimpleComparisons();
        testRangeComparisons();
        testCombine();
        testRamping();
        testAlways();
        testSetWhile();
        testDeadzones();
        testFindRate();
        testWhenFloatChanges();
        testNormalize();
        testOnUpdate();
    }

    private void testLimitsSimple() throws TestingException {
        FloatFilter limit1 = FloatMixing.limit(Float.NEGATIVE_INFINITY, 0);
        assertObjectEqual(limit1.filter(-10000), -10000f, "Bad limit!");
        assertObjectEqual(limit1.filter(0), 0f, "Bad limit!");
        assertObjectEqual(limit1.filter(1), 0f, "Bad limit!");
        FloatFilter limit2 = FloatMixing.limit(0, Float.POSITIVE_INFINITY);
        assertObjectEqual(limit2.filter(-1), 0f, "Bad limit!");
        assertObjectEqual(limit2.filter(0), 0f, "Bad limit!");
        assertObjectEqual(limit2.filter(10000), 10000f, "Bad limit!");
    }

    private void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        FloatOutput value = FloatMixing.onUpdate(ceo);
        for (float v : interestingFloats) {
            ceo.ifExpected = true;
            value.set(v);
            ceo.check();
            ceo.ifExpected = Float.isNaN(v);
            value.set(v);
            // ... and we don't care whether or not it actually happened - it's not specified in the docs
        }
    }

    private void testNormalize() throws TestingException {
        FloatStatus value = new FloatStatus();
        FloatStatus low = new FloatStatus(), high = new FloatStatus();
        FloatInput norm1 = FloatMixing.normalizeFloat(value, low, high);
        FloatInputPoll norm2 = FloatMixing.normalizeFloat((FloatInputPoll) value, low, high);
        for (float lowV : interestingFloats) {
            low.set(lowV);
            for (float highV : interestingFloats) {
                high.set(highV);
                FloatInput norm3;
                FloatInputPoll norm4;
                if (!Float.isFinite(lowV) || !Float.isFinite(highV) || !Float.isFinite(highV - lowV)) {
                    try {
                        FloatMixing.normalizeFloat(value, lowV, highV);
                        assertFail("expected failure due to non-finite parameter or range");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    try {
                        FloatMixing.normalizeFloat((FloatInputPoll) value, lowV, highV);
                        assertFail("expected failure due to non-finite parameter or range");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    continue; // undefined behavior - don't care at all
                } else if (lowV == highV) {
                    try {
                        FloatMixing.normalizeFloat(value, lowV, highV);
                        assertFail("expected failure due to zero range");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    try {
                        FloatMixing.normalizeFloat((FloatInputPoll) value, lowV, highV);
                        assertFail("expected failure due to zero range");
                    } catch (IllegalArgumentException ex) {
                        // correct!
                    }
                    norm4 = norm3 = FloatMixing.always(Float.NaN);
                } else {
                    norm3 = FloatMixing.normalizeFloat(value, lowV, highV);
                    norm4 = FloatMixing.normalizeFloat((FloatInputPoll) value, lowV, highV);
                }
                for (float v : interestingFloats) {
                    value.set(v);
                    // check sameness
                    assertObjectEqual(norm1.get(), norm2.get(), "inconsistent");
                    assertObjectEqual(norm1.get(), norm3.get(), "inconsistent");
                    assertObjectEqual(norm1.get(), norm4.get(), "inconsistent");
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

    private void testWhenFloatChanges() throws TestingException {
        for (float delta : interestingFloats) {
            FloatStatus value = new FloatStatus();
            EventStatus updateOn = new EventStatus();
            TestEventMixing.CountingEventOutput co = new TestEventMixing.CountingEventOutput();
            if (!Float.isFinite(delta)) {
                try {
                    FloatMixing.whenFloatChanges(value, delta, updateOn);
                    assertFail("Expected IAE from non-finite delta");
                } catch (IllegalArgumentException ex) {
                    // correct!
                }
                continue;
            }
            FloatMixing.whenFloatChanges(value, delta, updateOn).send(co);
            float lastValue = value.get();
            updateOn.event();
            for (float v : interestingFloats) {
                value.set(v);
                value.set(v);
                if (co.ifExpected = Math.abs(lastValue - v) > delta) {
                    lastValue = v;
                }
                updateOn.event();
                co.check();
            }
        }
    }

    private void testFindRate() throws TestingException {
        // TODO: test initial NaN behavior
        FloatStatus finp = new FloatStatus();
        EventStatus updateOn = new EventStatus();
        FloatInputPoll fin = FloatMixing.findRate(finp);
        FloatInputPoll fin2 = FloatMixing.findRate(finp, updateOn);
        float lastValue = finp.get();
        for (float value : interestingFloats) {
            if (Float.isInfinite(value)) {
                continue;
            }
            finp.set(value);
            float delta = fin.get();
            if (Float.isNaN(value)) {
                assertTrue(Float.isNaN(delta), "bad NaN handling");
                assertTrue(Float.isNaN(fin2.get()), "bad NaN handling");
                updateOn.event();
                assertTrue(Float.isNaN(fin2.get()), "bad NaN handling");
                continue;
            }
            assertFalse(Float.isNaN(delta), "expected non-NaN at this point");
            assertTrue(Math.abs(lastValue + delta - value) <= Math.max(Math.abs(value), Math.abs(lastValue)) * 0.0001f, "bad findRate from " + lastValue + " + " + delta + " => " + (lastValue + delta) + " => " + Math.abs(lastValue + delta - value) + " vs " + Math.abs(value) * 0.0001f);
            lastValue = value;
            assertObjectEqual(fin.get(), 0.0f, "bad second findRate"); // because of DOCUMENTED bad usage of 1-parameter findRate
            assertObjectEqual(fin2.get(), delta, "bad findRate2 delta");
            assertObjectEqual(fin2.get(), delta, "bad findRate2 delta"); // check for consistency
            updateOn.event();
            assertObjectEqual(fin2.get(), 0.0f, "bad findRate2 initial");
        }
    }

    private void testDeadzones() throws TestingException {
        for (float zone : interestingFloats) {
            FloatFilter filter = FloatMixing.deadzone(zone);
            FloatStatus val = new FloatStatus();
            FloatInput in1 = filter.wrap(val.asInput());
            FloatInput in2 = FloatMixing.deadzone(val.asInput(), zone);
            FloatInputPoll in3 = FloatMixing.deadzone((FloatInputPoll) val, zone);
            CountingFloatOutput out = new CountingFloatOutput();
            out.ifExpected = true;
            out.valueExpected = 0;
            val.send(FloatMixing.deadzone(out, zone));
            out.check(); // because send will automatically set with the current value
            for (float value : interestingFloats) {
                out.ifExpected = true;
                out.valueExpected = Utils.deadzone(value, zone);
                val.set(value);
                out.check();
                assertObjectEqual(Utils.deadzone(value, zone), in1.get(), "bad deadzoning");
                assertObjectEqual(Utils.deadzone(value, zone), in2.get(), "bad deadzoning");
                assertObjectEqual(Utils.deadzone(value, zone), in3.get(), "bad deadzoning");
            }
        }
    }

    private void testSetWhile() {
        CountingFloatOutput a = new CountingFloatOutput(), b = new CountingFloatOutput();
        BooleanStatus should = new BooleanStatus();
        for (float value : interestingFloats) {
            EventStatus check = new EventStatus();
            FloatMixing.setWhile(check, should, a, value);
            FloatMixing.setWhileNot(check, should, b, value);

            a.valueExpected = b.valueExpected = value;
            for (boolean bool : TestBooleanMixing.interestingBooleans) {
                should.set(bool);
                a.ifExpected = bool;
                b.ifExpected = !bool;
                check.event();
                a.check();
                b.check();
            }
        }
    }

    private void testAlways() {
        for (float value : interestingFloats) {
            CountingFloatOutput out = new CountingFloatOutput();
            out.ifExpected = true;
            out.valueExpected = value;
            FloatInput vall = FloatMixing.always(value);
            vall.send(out);
            out.check();
            vall.unsend(out);
        }
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

    private void testLimits() throws TestingException {
        FloatStatus in = new FloatStatus();
        testNegation();
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

    private void testNegation() throws TestingException {
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
