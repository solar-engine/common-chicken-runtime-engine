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

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;

public class BooleanCellTest {

    private BooleanCell cell;

    @Before
    public void setUp() throws Exception {
        this.cell = new BooleanCell();
    }

    @After
    public void tearDown() throws Exception {
        this.cell = null;
    }

    @Test
    public void testBooleanStatusBoolean() {
        assertFalse(new BooleanCell(false).get());
        assertTrue(new BooleanCell(true).get());
    }

    @Test
    public void testBooleanStatusBooleanOutput() {
        CountingBooleanOutput cbo = new CountingBooleanOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = false;
        cell = new BooleanCell(cbo);
        cbo.check();
        assertTrue(cell.hasListeners());
        Random random = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? random.nextBoolean() : i % 3 == 0;
            cbo.ifExpected = (value != lastValue);
            cbo.valueExpected = value;
            cell.set(value);
            cbo.check();
            lastValue = value;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanStatusBooleanOutputNull() {
        new BooleanCell((BooleanOutput) null);
    }

    @Test
    public void testBooleanStatusBooleanOutputArray() {
        CountingBooleanOutput c1 = new CountingBooleanOutput();
        CountingBooleanOutput c2 = new CountingBooleanOutput();
        c1.ifExpected = c2.ifExpected = true;
        c1.valueExpected = c2.valueExpected = false;
        cell = new BooleanCell(c1, c2);
        c1.check();
        c2.check();
        assertTrue(cell.hasListeners());
        Random random = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? random.nextBoolean() : i % 3 == 0;
            c1.ifExpected = c2.ifExpected = (value != lastValue);
            c1.valueExpected = c2.valueExpected = value;
            cell.set(value);
            c1.check();
            c2.check();
            lastValue = value;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanStatusBooleanOutputArrayNull() {
        new BooleanCell((BooleanOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanStatusBooleanOutputArrayNulls() {
        new BooleanCell(BooleanOutput.ignored, null);
    }


    @Test
    public void testHasConsumers() {
        assertFalse(cell.hasListeners());
        for (int i = 0; i < 5; i++) {
            EventOutput eo = cell.sendR(BooleanOutput.ignored);
            assertTrue(cell.hasListeners());
            eo.event();
            assertFalse(cell.hasListeners());
            EventOutput eo1 = cell.sendR(BooleanOutput.ignored);
            assertTrue(cell.hasListeners());
            EventOutput eo2 = cell.sendR(BooleanOutput.ignored);
            assertTrue(cell.hasListeners());
            eo1.event();
            assertTrue(cell.hasListeners());
            eo2.event();
            assertFalse(cell.hasListeners());
        }
    }

    @Test
    public void testSet() {
        Random rand = new Random();
        CountingBooleanOutput cbo = new CountingBooleanOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = false;
        cell.send(cbo);
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0;
            cbo.ifExpected = (value != lastValue);
            cbo.valueExpected = value;
            assertEquals(lastValue, cell.get());
            cell.set(value);
            assertEquals(value, cell.get());
            cbo.check();
            lastValue = value;
        }
    }

    @Test
    public void testGet() {
        CountingBooleanOutput ceo = new CountingBooleanOutput();
        CountingBooleanOutput ceo2 = new CountingBooleanOutput();
        ceo.ifExpected = ceo2.ifExpected = true;
        ceo.valueExpected = ceo2.valueExpected = false;
        cell.send((value) -> {
            ceo.set(cell.get());
            ceo2.set(value);
        });
        ceo.check();
        ceo2.check();
        for (int i = 0; i < 5; i++) {
            boolean value = i % 2 == 0;
            ceo.ifExpected = ceo2.ifExpected = true;
            ceo.valueExpected = ceo2.valueExpected = value;
            cell.set(value);
            assertEquals(value, cell.get());
            ceo.check();
            ceo2.check();
        }
    }

    @Test
    public void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        cell.onUpdate(ceo);
        assertTrue(cell.hasListeners());
        Random rand = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0;
            ceo.ifExpected = (value != lastValue);
            cell.set(value);
            ceo.check();
            lastValue = value;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateNull() {
        cell.onUpdate(null);
    }

    @Test
    public void testOnUpdateR() {
        CountingEventOutput ceo = new CountingEventOutput();
        EventOutput unbind = cell.onUpdateR(ceo);
        assertTrue(cell.hasListeners());
        Random rand = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0;
            ceo.ifExpected = (value != lastValue);
            cell.set(value);
            ceo.check();
            lastValue = value;
        }
        assertTrue(cell.hasListeners());
        unbind.event();
        assertFalse(cell.hasListeners());
        for (int i = 0; i < 20; i++) {
            cell.set(i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0);
            ceo.check();
        }
        assertFalse(cell.hasListeners());
    }

    @Test
    public void testOnUpdateAndOnUpdateR() {
        assertFalse(cell.hasListeners());
        EventOutput unbind = cell.onUpdateR(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        cell.onUpdate(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        unbind.event();
        assertTrue(cell.hasListeners());
    }

    @Test
    public void testOnUpdateRTwice() {
        assertFalse(cell.hasListeners());
        EventOutput unbind1 = cell.onUpdateR(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        EventOutput unbind2 = cell.onUpdateR(EventOutput.ignored);
        assertTrue(cell.hasListeners());
        unbind1.event();
        assertTrue(cell.hasListeners());
        unbind2.event();
        assertFalse(cell.hasListeners());
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateRNull() {
        cell.onUpdateR(null);
    }
}
