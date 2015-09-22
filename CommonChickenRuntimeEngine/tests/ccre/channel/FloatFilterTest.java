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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingFloatOutput;
import ccre.util.Utils;
import ccre.util.Values;

public class FloatFilterTest {

    private FloatFilter ff;

    @Before
    public void setUp() throws Exception {
        ff = new FloatFilter() {
            @Override
            public float filter(float input) {
                return input + 1;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        ff = null;
    }

    @Test
    public void testFilter() {
        for (float f : Values.interestingFloats) {
            assertEquals(f + 1, ff.filter(f), 0);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testWrapFloatInputNull() {
        ff.wrap((FloatInput) null);
    }

    @Test
    public void testWrapFloatInput() {
        FloatStatus fs = new FloatStatus();
        FloatInput fin = ff.wrap(fs.asInput());
        CountingFloatOutput cfo = new CountingFloatOutput();
        cfo.valueExpected = ff.filter(0);
        cfo.ifExpected = true;
        fin.send(cfo);
        cfo.check();
        for (float f : Values.interestingFloats) {
            if (f == 0) { // avoids artificial problem with negative epsilon
                          // followed by zero... they both add with one to one!
                cfo.valueExpected = 2;
                cfo.ifExpected = true;
                fs.set(1);
                cfo.check();
            }
            cfo.valueExpected = f + 1;
            cfo.ifExpected = true;
            fs.set(f);
            cfo.check();
            assertEquals(f + 1, fin.get(), 0);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testWrapFloatOutputNull() {
        ff.wrap((FloatOutput) null);
    }

    @Test
    public void testWrapFloatOutput() {
        CountingFloatOutput cfo = new CountingFloatOutput();
        FloatOutput fin = ff.wrap(cfo);
        for (float f : Values.interestingFloats) {
            cfo.valueExpected = f + 1;
            cfo.ifExpected = true;
            fin.set(f);
            cfo.check();
        }
    }

    @Test
    public void testDeadzone() {
        for (float zone : Values.interestingFloats) {
            if (!Float.isFinite(zone) || zone < 0) {
                continue;
            }
            FloatFilter filter = FloatFilter.deadzone(zone);
            for (float value : Values.interestingFloats) {
                if (Float.isNaN(value)) {
                    assertTrue(Float.isNaN(filter.filter(value)));
                    continue;
                }
                assertEquals(Utils.deadzone(value, zone), filter.filter(value), 0);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeadzoneNaN() {
        FloatFilter.deadzone(Float.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeadzoneNegative() {
        FloatFilter.deadzone(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeadzoneInfinite() {
        FloatFilter.deadzone(Float.POSITIVE_INFINITY);
    }

    @Test
    public void testLimit() {
        for (float low : Values.interestingFloats) {
            for (float high : Values.interestingFloats) {
                FloatFilter limit;
                try {
                    limit = FloatFilter.limit(low, high);
                    assertFalse("should have thrown exception", high < low);
                } catch (IllegalArgumentException ex) {
                    assertTrue(high < low);
                    continue;
                }
                for (float value : Values.interestingFloats) {
                    float gotten = limit.filter(value);
                    if (Float.isNaN(value)) {
                        assertTrue(Float.isNaN(gotten));
                        continue;
                    }
                    assertTrue((gotten >= low || Float.isNaN(low)) && (gotten <= high || Float.isNaN(high)));
                    if (gotten != value) {
                        if (gotten == low) {
                            if (low == high) {
                                assertTrue(value <= low || value >= high);
                            } else {
                                assertTrue(value <= low);
                            }
                        } else if (gotten == high) {
                            assertTrue(value >= high);
                        } else {
                            fail("bad roundoff");
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testNegate() {
        for (float f : Values.interestingFloats) {
            assertEquals(-f, FloatFilter.negate.filter(f), 0);
            assertEquals(f, FloatFilter.negate.filter(-f), 0);
        }
    }
}
