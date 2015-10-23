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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingEventOutput;
import ccre.util.Values;

public class DerivedEventInputTest {

    private EventStatus es;
    private CountingEventOutput ceo;

    @Before
    public void setUp() {
        es = new EventStatus();
        ceo = new CountingEventOutput();
    }

    @After
    public void tearDown() {
        es = null;
        ceo = null;
    }

    @Test
    public void testApplyCalledAtLimitedTimes() {
        CountingEventOutput ceo = new CountingEventOutput();
        new DerivedEventInput(es) {
            @Override
            protected boolean shouldProduce() {
                ceo.event();
                return false;
            }
        };
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            es.event();
            ceo.check();
        }
    }

    @Test
    public void testProduced() {
        BooleanStatus v = new BooleanStatus();
        EventInput ei = new DerivedEventInput(es) {
            @Override
            protected boolean shouldProduce() {
                return v.get();
            }
        };
        ei.send(ceo);
        for (boolean b : Values.interestingBooleans) {
            v.set(b);
            ceo.ifExpected = b;
            es.event();
            ceo.check();
        }
    }
}
