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

public class FloatCellTest {

    private FloatCell cell;

    @Before
    public void setUp() throws Exception {
        this.cell = new FloatCell();
    }

    @After
    public void tearDown() throws Exception {
        this.cell = null;
    }

    @Test
    public void testFloatStatusFloat() {
        for (float f : Values.interestingFloats) {
            assertEquals(f, new FloatCell(f).get(), 0);
        }
    }

    @Test
    public void testFloatStatusFloatOutput() {
        CountingFloatOutput cfo = new CountingFloatOutput();
        cfo.ifExpected = true;
        cfo.valueExpected = 0;
        cell = new FloatCell(cfo);
        cfo.check();
        assertTrue(cell.hasListeners());
        for (float f : Values.interestingFloats) {
            cfo.ifExpected = true;
            cfo.valueExpected = f;
            cell.set(f);
            cfo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFloatStatusFloatOutputNull() {
        new FloatCell((FloatOutput) null);
    }

    @Test
    public void testFloatStatusFloatOutputArray() {
        CountingFloatOutput cfo1 = new CountingFloatOutput();
        CountingFloatOutput cfo2 = new CountingFloatOutput();
        cfo1.ifExpected = cfo2.ifExpected = true;
        cfo1.valueExpected = cfo2.valueExpected = 0;
        cell = new FloatCell(cfo1, cfo2);
        cfo1.check();
        cfo2.check();
        assertTrue(cell.hasListeners());
        for (float f : Values.interestingFloats) {
            cfo1.ifExpected = cfo2.ifExpected = true;
            cfo1.valueExpected = cfo2.valueExpected = f;
            cell.set(f);
            cfo1.check();
            cfo2.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFloatStatusFloatOutputArrayNull() {
        new FloatCell((FloatOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testFloatStatusFloatOutputArrayNulls() {
        new FloatCell(FloatOutput.ignored, null);
    }

    @Test
    public void testHasConsumers() {
        assertFalse(cell.hasListeners());
        for (int i = 0; i < 5; i++) {
            EventOutput eo = cell.send(FloatOutput.ignored);
            assertTrue(cell.hasListeners());
            eo.event();
            assertFalse(cell.hasListeners());
            EventOutput eo1 = cell.send(FloatOutput.ignored);
            assertTrue(cell.hasListeners());
            EventOutput eo2 = cell.send(FloatOutput.ignored);
            assertTrue(cell.hasListeners());
            eo1.event();
            assertTrue(cell.hasListeners());
            eo2.event();
            assertFalse(cell.hasListeners());
        }
    }

    @Test
    public void testSet() {
        CountingFloatOutput cbo = new CountingFloatOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = 0;
        cell.send(cbo);
        for (float f : Values.interestingFloats) {
            cbo.ifExpected = true;
            cbo.valueExpected = f;
            cell.set(f);
            assertEquals(f, cell.get(), 0);
            cbo.check();
        }
    }

    @Test
    public void testGet() {
        CountingFloatOutput ceo = new CountingFloatOutput();
        CountingFloatOutput ceo2 = new CountingFloatOutput();
        ceo.ifExpected = ceo2.ifExpected = true;
        ceo.valueExpected = ceo2.valueExpected = 0;
        cell.send((value) -> {
            ceo.set(cell.get());
            ceo2.set(value);
        });
        ceo.check();
        ceo2.check();
        for (float f : Values.interestingFloats) {
            ceo.ifExpected = ceo2.ifExpected = true;
            ceo.valueExpected = ceo2.valueExpected = f;
            cell.set(f);
            assertEquals(f, cell.get(), 0);
            ceo.check();
            ceo2.check();
        }
    }

    @Test
    public void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        cell.onUpdate(ceo);
        assertTrue(cell.hasListeners());
        for (float f : Values.interestingFloats) {
            ceo.ifExpected = true;
            cell.set(f);
            ceo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateNull() {
        cell.onUpdate(null);
    }

    @Test
    public void testOnUpdateR() {
        CountingEventOutput ceo = new CountingEventOutput();
        EventOutput unbind = cell.onUpdate(ceo);
        assertTrue(cell.hasListeners());
        for (float f : Values.interestingFloats) {
            ceo.ifExpected = true;
            cell.set(f);
            ceo.check();
        }
        assertTrue(cell.hasListeners());
        unbind.event();
        assertFalse(cell.hasListeners());
        for (float f : Values.interestingFloats) {
            cell.set(f);
            ceo.check();
        }
        assertFalse(cell.hasListeners());
    }

    @Test
    public void testOnUpdateAndOnUpdateR() {
        assertFalse(cell.hasListeners());
        EventOutput unbind = cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        unbind.event();
        assertTrue(cell.hasListeners());
    }

    @Test
    public void testOnUpdateRTwice() {
        assertFalse(cell.hasListeners());
        EventOutput unbind1 = cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        EventOutput unbind2 = cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        unbind1.event();
        assertTrue(cell.hasListeners());
        unbind2.event();
        assertFalse(cell.hasListeners());
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateRNull() {
        cell.onUpdate(null);
    }
}
