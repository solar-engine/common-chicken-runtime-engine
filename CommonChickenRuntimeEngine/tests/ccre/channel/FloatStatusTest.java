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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingEventOutput;
import ccre.testing.CountingFloatOutput;
import ccre.util.Values;

public class FloatStatusTest {

    private FloatStatus status;

    @Before
    public void setUp() throws Exception {
        this.status = new FloatStatus();
    }

    @After
    public void tearDown() throws Exception {
        this.status = null;
    }

    @Test
    public void testFloatStatusFloat() {
        for (float f : Values.interestingFloats) {
            assertEquals(f, new FloatStatus(f).get(), 0);
        }
    }

    @Test
    public void testFloatStatusFloatOutput() {
        CountingFloatOutput cfo = new CountingFloatOutput();
        cfo.ifExpected = true;
        cfo.valueExpected = 0;
        status = new FloatStatus(cfo);
        cfo.check();
        assertTrue(status.hasListeners());
        for (float f : Values.interestingFloats) {
            cfo.ifExpected = true;
            cfo.valueExpected = f;
            status.set(f);
            cfo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFloatStatusFloatOutputNull() {
        new FloatStatus((FloatOutput) null);
    }

    @Test
    public void testFloatStatusFloatOutputArray() {
        CountingFloatOutput cfo1 = new CountingFloatOutput();
        CountingFloatOutput cfo2 = new CountingFloatOutput();
        cfo1.ifExpected = cfo2.ifExpected = true;
        cfo1.valueExpected = cfo2.valueExpected = 0;
        status = new FloatStatus(cfo1, cfo2);
        cfo1.check();
        cfo2.check();
        assertTrue(status.hasListeners());
        for (float f : Values.interestingFloats) {
            cfo1.ifExpected = cfo2.ifExpected = true;
            cfo1.valueExpected = cfo2.valueExpected = f;
            status.set(f);
            cfo1.check();
            cfo2.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFloatStatusFloatOutputArrayNull() {
        new FloatStatus((FloatOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testFloatStatusFloatOutputArrayNulls() {
        new FloatStatus(FloatOutput.ignored, null);
    }

    @Test
    public void testHasConsumers() {
        assertFalse(status.hasListeners());
        for (int i = 0; i < 5; i++) {
            EventOutput eo = status.sendR(FloatOutput.ignored);
            assertTrue(status.hasListeners());
            eo.event();
            assertFalse(status.hasListeners());
            EventOutput eo1 = status.sendR(FloatOutput.ignored);
            assertTrue(status.hasListeners());
            EventOutput eo2 = status.sendR(FloatOutput.ignored);
            assertTrue(status.hasListeners());
            eo1.event();
            assertTrue(status.hasListeners());
            eo2.event();
            assertFalse(status.hasListeners());
        }
    }

    @Test
    public void testSet() {
        CountingFloatOutput cbo = new CountingFloatOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = 0;
        status.send(cbo);
        for (float f : Values.interestingFloats) {
            cbo.ifExpected = true;
            cbo.valueExpected = f;
            status.set(f);
            assertEquals(f, status.get(), 0);
            cbo.check();
        }
    }

    @Test
    public void testGet() {
        CountingFloatOutput ceo = new CountingFloatOutput();
        CountingFloatOutput ceo2 = new CountingFloatOutput();
        ceo.ifExpected = ceo2.ifExpected = true;
        ceo.valueExpected = ceo2.valueExpected = 0;
        status.send((value) -> {
            ceo.set(status.get());
            ceo2.set(value);
        });
        ceo.check();
        ceo2.check();
        for (float f : Values.interestingFloats) {
            ceo.ifExpected = ceo2.ifExpected = true;
            ceo.valueExpected = ceo2.valueExpected = f;
            status.set(f);
            assertEquals(f, status.get(), 0);
            ceo.check();
            ceo2.check();
        }
    }

    @Test
    public void testAsOutput() {
        assertEquals(status, status.asOutput());
    }

    @Test
    public void testAsInput() {
        assertEquals(status, status.asInput());
    }

    @Test
    public void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        status.onUpdate(ceo);
        assertTrue(status.hasListeners());
        for (float f : Values.interestingFloats) {
            ceo.ifExpected = true;
            status.set(f);
            ceo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateNull() {
        status.onUpdate(null);
    }

    @Test
    public void testOnUpdateR() {
        CountingEventOutput ceo = new CountingEventOutput();
        EventOutput unbind = status.onUpdateR(ceo);
        assertTrue(status.hasListeners());
        for (float f : Values.interestingFloats) {
            ceo.ifExpected = true;
            status.set(f);
            ceo.check();
        }
        assertTrue(status.hasListeners());
        unbind.event();
        assertFalse(status.hasListeners());
        for (float f : Values.interestingFloats) {
            status.set(f);
            ceo.check();
        }
        assertFalse(status.hasListeners());
    }

    @Test
    public void testOnUpdateAndOnUpdateR() {
        assertFalse(status.hasListeners());
        EventOutput unbind = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasListeners());
        status.onUpdate(EventOutput.ignored);
        assertTrue(status.hasListeners());
        unbind.event();
        assertTrue(status.hasListeners());
    }

    @Test
    public void testOnUpdateRTwice() {
        assertFalse(status.hasListeners());
        EventOutput unbind1 = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasListeners());
        EventOutput unbind2 = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasListeners());
        unbind1.event();
        assertTrue(status.hasListeners());
        unbind2.event();
        assertFalse(status.hasListeners());
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateRNull() {
        status.onUpdateR(null);
    }
}
