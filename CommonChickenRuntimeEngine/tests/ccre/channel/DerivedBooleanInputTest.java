/*
 * Copyright 2015 Cel Skeggs
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

import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class DerivedBooleanInputTest {

    private EventCell es;
    private CountingBooleanOutput cbo;

    @Before
    public void setUp() {
        es = new EventCell();
        cbo = new CountingBooleanOutput();
    }

    @After
    public void tearDown() {
        es = null;
        cbo = null;
    }

    @Test
    public void testApplyCalledAtLimitedTimes() {
        CountingEventOutput ceo = new CountingEventOutput();
        ceo.ifExpected = true;
        new DerivedBooleanInput(es) {
            @Override
            protected boolean apply() {
                ceo.event();
                return false;
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
        for (boolean base : new boolean[] { false, true }) {
            BooleanCell v = new BooleanCell(base);
            BooleanInput bi = new DerivedBooleanInput(es) {
                @Override
                protected boolean apply() {
                    return v.get();
                }
            };
            assertEquals(base, bi.get());
            boolean last = base;
            for (boolean b : Values.interestingBooleans) {
                assertEquals(last, bi.get());
                v.set(b);
                assertEquals(last, bi.get());
                es.event();
                assertEquals(b, bi.get());
                last = b;
            }
        }
    }

    @Test
    public void testApplyChange() {
        for (boolean initial : new boolean[] { false, true }) {
            BooleanCell v = new BooleanCell(initial);
            BooleanInput bi = new DerivedBooleanInput(es) {
                @Override
                protected boolean apply() {
                    return v.get();
                }
            };
            cbo.ifExpected = true;
            cbo.valueExpected = initial;
            bi.send(cbo);
            cbo.check();
            boolean last = v.get();
            for (boolean b : Values.interestingBooleans) {
                v.set(b);
                cbo.ifExpected = (b != last);
                cbo.valueExpected = b;
                es.event();
                cbo.check();
                last = b;
            }
        }
    }
}
