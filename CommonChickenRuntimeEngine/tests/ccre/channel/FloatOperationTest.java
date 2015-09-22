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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingFloatOutput;
import ccre.util.Values;

public class FloatOperationTest {

    private FloatStatus sa, sb;
    private CountingFloatOutput cfo;

    @Before
    public void setUp() throws Exception {
        sa = new FloatStatus();
        sb = new FloatStatus();
        cfo = new CountingFloatOutput();
    }

    @After
    public void tearDown() throws Exception {
        sa = sb = null;
        cfo = null;
    }

    @Test
    public void testAddition() {
        for (float a : Values.interestingFloats) {
            for (float b : Values.interestingFloats) {
                assertEquals(a + b, FloatOperation.addition.of(a, b), 0);
            }
        }
    }

    @Test
    public void testSubtraction() {
        for (float a : Values.interestingFloats) {
            for (float b : Values.interestingFloats) {
                assertEquals(a - b, FloatOperation.subtraction.of(a, b), 0);
            }
        }
    }

    @Test
    public void testMultiplication() {
        for (float a : Values.interestingFloats) {
            for (float b : Values.interestingFloats) {
                assertEquals(a * b, FloatOperation.multiplication.of(a, b), 0);
            }
        }
    }

    @Test
    public void testDivision() {
        for (float a : Values.interestingFloats) {
            for (float b : Values.interestingFloats) {
                assertEquals(a / b, FloatOperation.division.of(a, b), 0);
            }
        }
    }

    @Test
    public void testOfFloatFloatInput() {
        for (float b : Values.interestingFloats) {
            FloatInput fi = FloatOperation.subtraction.of(sa.asInput(), b);
            cfo.ifExpected = true;
            cfo.valueExpected = FloatOperation.subtraction.of(sa.get(), b);
            EventOutput unbind = fi.sendR(cfo);
            cfo.check();
            for (float a : Values.interestingFloats) {
                float lastExpected = cfo.valueExpected;
                cfo.valueExpected = FloatOperation.subtraction.of(a, b);
                cfo.ifExpected = lastExpected != cfo.valueExpected;
                sa.set(a);
                cfo.check();
                assertEquals(cfo.valueExpected, fi.get(), 0);
            }
            unbind.event();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatFloatInputNull() {
        FloatOperation.addition.of(0, (FloatInput) null);
    }

    @Test
    public void testOfFloatInputFloat() {
        for (float a : Values.interestingFloats) {
            FloatInput fi = FloatOperation.subtraction.of(a, sb.asInput());
            cfo.ifExpected = true;
            cfo.valueExpected = FloatOperation.subtraction.of(a, sb.get());
            EventOutput unbind = fi.sendR(cfo);
            cfo.check();
            for (float b : Values.interestingFloats) {
                float lastExpected = cfo.valueExpected;
                cfo.valueExpected = FloatOperation.subtraction.of(a, b);
                cfo.ifExpected = (lastExpected != cfo.valueExpected);
                sb.set(b);
                cfo.check();
                assertEquals(cfo.valueExpected, fi.get(), 0);
            }
            unbind.event();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatInputFloatNull() {
        FloatOperation.addition.of((FloatInput) null, 0);
    }

    @Test
    public void testOfFloatInputFloatInput() {
        FloatInput fi = FloatOperation.subtraction.of(sa.asInput(), sb.asInput());
        cfo.ifExpected = true;
        cfo.valueExpected = FloatOperation.subtraction.of(sa.get(), sb.get());
        fi.send(cfo);
        cfo.check();
        for (float a : Values.interestingFloats) {
            float lastExpected = cfo.valueExpected;
            cfo.valueExpected = FloatOperation.subtraction.of(a, sb.get());
            cfo.ifExpected = (lastExpected != cfo.valueExpected);
            sa.set(a);
            cfo.check();
            for (float b : Values.interestingFloats) {
                lastExpected = cfo.valueExpected;
                cfo.valueExpected = FloatOperation.subtraction.of(a, b);
                cfo.ifExpected = (lastExpected != cfo.valueExpected);
                sb.set(b);
                cfo.check();
                assertEquals(cfo.valueExpected, fi.get(), 0);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatInputFloatInputNullA() {
        FloatOperation.addition.of((FloatInput) null, FloatInput.zero);
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatInputFloatInputNullB() {
        FloatOperation.addition.of(FloatInput.zero, (FloatInput) null);
    }

    @Test
    public void testOfFloatOutputFloat() {
        for (float b : Values.interestingFloats) {
            FloatOutput o = FloatOperation.subtraction.of(cfo, b);
            for (float a : Values.interestingFloats) {
                cfo.ifExpected = true;
                cfo.valueExpected = FloatOperation.subtraction.of(a, b);
                o.set(a);
                cfo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatOutputFloatNull() {
        FloatOperation.addition.of((FloatOutput) null, 0);
    }

    @Test
    public void testOfFloatOutputFloatInput() {
        FloatOutput o = FloatOperation.subtraction.of(cfo, sb);
        Float lastValue = null;
        for (float b : Values.interestingFloats) {
            cfo.ifExpected = (lastValue != null);
            if (lastValue != null) {
                cfo.valueExpected = FloatOperation.subtraction.of(lastValue, b);
            }
            sb.set(b);
            cfo.check();

            for (float a : Values.interestingFloats) {
                cfo.ifExpected = true;
                cfo.valueExpected = FloatOperation.subtraction.of(a, b);
                o.set(a);
                cfo.check();
                lastValue = a;
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatOutputFloatInputNullA() {
        FloatOperation.addition.of(FloatOutput.ignored, (FloatInput) null);
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatOutputFloatInputNullB() {
        FloatOperation.addition.of((FloatOutput) null, FloatInput.zero);
    }

    @Test
    public void testOfFloatFloatOutput() {
        for (float a : Values.interestingFloats) {
            FloatOutput o = FloatOperation.subtraction.of(a, cfo);
            for (float b : Values.interestingFloats) {
                cfo.ifExpected = true;
                cfo.valueExpected = FloatOperation.subtraction.of(a, b);
                o.set(b);
                cfo.check();
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatFloatOutputNull() {
        FloatOperation.addition.of(0, (FloatOutput) null);
    }

    @Test
    public void testOfFloatInputFloatOutput() {
        FloatOutput o = FloatOperation.subtraction.of(sa, cfo);
        Float lastValue = null;
        for (float a : Values.interestingFloats) {
            cfo.ifExpected = (lastValue != null);
            if (lastValue != null) {
                cfo.valueExpected = FloatOperation.subtraction.of(a, lastValue);
            }
            sa.set(a);
            cfo.check();

            for (float b : Values.interestingFloats) {
                cfo.ifExpected = true;
                cfo.valueExpected = FloatOperation.subtraction.of(a, b);
                o.set(b);
                cfo.check();
                lastValue = b;
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatInputFloatOutputNullA() {
        FloatOperation.addition.of((FloatInput) null, FloatOutput.ignored);
    }

    @Test(expected = NullPointerException.class)
    public void testOfFloatInputFloatOutputNullB() {
        FloatOperation.addition.of(FloatInput.zero, (FloatOutput) null);
    }
}
