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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingFloatOutput;
import ccre.testing.CountingEventOutput;
import ccre.util.Values;

public class DerivedFloatInputTest {

    private EventCell es;
    private CountingFloatOutput cfo;

    @Before
    public void setUp() {
        es = new EventCell();
        cfo = new CountingFloatOutput();
    }

    @After
    public void tearDown() {
        es = null;
        cfo = null;
    }

    @Test
    public void testApplyCalledAtLimitedTimes() {
        CountingEventOutput ceo = new CountingEventOutput();
        ceo.ifExpected = true;
        new DerivedFloatInput(es) {
            @Override
            protected float apply() {
                ceo.event();
                return 0;
            }
        };
        ceo.check();
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            es.event();
            ceo.check();
        }
    }

    @Test
    public void testGet() {
        for (float base : Values.interestingFloats) {
            FloatCell v = new FloatCell(base);
            FloatInput fi = new DerivedFloatInput(es) {
                @Override
                protected float apply() {
                    return v.get();
                }
            };
            assertEquals(base, fi.get(), 0);
            float last = base;
            for (float f : Values.interestingFloats) {
                assertEquals(last, fi.get(), 0);
                v.set(f);
                assertEquals(last, fi.get(), 0);
                es.event();
                assertEquals(f, fi.get(), 0);
                last = f;
            }
        }
    }

    @Test
    public void testApplyChange() {
        FloatCell v = new FloatCell();
        FloatInput bi = new DerivedFloatInput(es) {
            @Override
            protected float apply() {
                return v.get();
            }
        };
        bi.send(cfo);
        float last = v.get();
        for (float f : Values.interestingFloats) {
            v.set(f);
            cfo.ifExpected = Float.floatToIntBits(f) != Float.floatToIntBits(last);
            cfo.valueExpected = f;
            es.event();
            cfo.check();
            last = f;
        }
    }
}
