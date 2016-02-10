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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingFloatOutput;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class FloatIOTest {

    private FloatIO io;

    @Before
    public void setUp() throws Exception {
        this.io = new FloatIO() {
            private float value;

            @Override
            public float get() {
                return value;
            }

            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                fail();
                return null;
            }

            @Override
            public void set(float value) {
                this.value = value;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        this.io = null;
    }

    @Test
    public void testAsOutput() {
        assertEquals(io, io.asOutput());
    }

    @Test
    public void testAsInput() {
        assertEquals(io, io.asInput());
    }

    @Test
    public void testCell() {
        FloatIO c = new FloatCell();
        CountingFloatOutput cfo = new CountingFloatOutput();
        cfo.ifExpected = true;
        cfo.valueExpected = c.get();
        c.send(cfo);
        cfo.check();
        for (float b : Values.interestingFloats) {
            cfo.ifExpected = Float.floatToIntBits(b) != Float.floatToIntBits(cfo.valueExpected);
            cfo.valueExpected = b;
            assertTrue(c.cell(b) == c);
            cfo.check();
        }
    }
}
