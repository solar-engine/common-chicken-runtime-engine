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

import ccre.channel.EventStatus;
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
        testNegation();
        FloatOutput.ignored.set(0);
        testGetSetEvent();
        testCombine();
        testRamping();
    }
    
    private void testRamping() throws TestingException {
        for (float limit : lessInterestingFloats) {
            if (Float.isNaN(limit)) {
                continue;
            }
            CountingFloatOutput out = new CountingFloatOutput();
            EventStatus updateWhen = new EventStatus();
            FloatOutput rampout = out.addRamping(limit, updateWhen);

            float lastValue = 0.0f;
            for (float cmp1 : interestingFloats) {
                for (float cmp2 : interestingFloats) {
                    float value = cmp1 - cmp2;// to get more and more interesting numbers here

                    out.valueExpected = lastValue = Utils.updateRamping(lastValue, value, limit);
                    rampout.set(value);
                    out.ifExpected = true;
                    updateWhen.event();
                    out.check();
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

    private void testGetSetEvent() throws TestingException {
        FloatStatus val = new FloatStatus();
        for (float f : interestingFloats) {
            val.getSetEvent(f).event();
            assertObjectEqual(val.get(), f, "bad set event");
        }
    }

    private void testNegation() throws TestingException {
        FloatStatus in = new FloatStatus();
        FloatOutput nset = in.negate();
        for (float f : interestingFloats) {
            nset.set(-f);
            assertObjectEqual(in.get(), f, "bad negation");
            nset.set(f);
            assertObjectEqual(in.get(), -f, "bad negation");
        }
    }
}
