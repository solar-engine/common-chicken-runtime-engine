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

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;

public class BooleanStatusTest {

    private BooleanStatus status;

    @Before
    public void setUp() throws Exception {
        this.status = new BooleanStatus();
    }

    @After
    public void tearDown() throws Exception {
        this.status = null;
    }

    @Test
    public void testBooleanStatusBoolean() {
        assertFalse(new BooleanStatus(false).get());
        assertTrue(new BooleanStatus(true).get());
    }

    @Test
    public void testBooleanStatusBooleanOutput() {
        CountingBooleanOutput cbo = new CountingBooleanOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = false;
        status = new BooleanStatus(cbo);
        cbo.check();
        assertTrue(status.hasConsumers());
        Random random = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? random.nextBoolean() : i % 3 == 0;
            cbo.ifExpected = (value != lastValue);
            cbo.valueExpected = value;
            status.set(value);
            cbo.check();
            lastValue = value;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanStatusBooleanOutputNull() {
        new BooleanStatus((BooleanOutput) null);
    }

    @Test
    public void testBooleanStatusBooleanOutputArray() {
        CountingBooleanOutput c1 = new CountingBooleanOutput();
        CountingBooleanOutput c2 = new CountingBooleanOutput();
        c1.ifExpected = c2.ifExpected = true;
        c1.valueExpected = c2.valueExpected = false;
        status = new BooleanStatus(c1, c2);
        c1.check();
        c2.check();
        assertTrue(status.hasConsumers());
        Random random = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? random.nextBoolean() : i % 3 == 0;
            c1.ifExpected = c2.ifExpected = (value != lastValue);
            c1.valueExpected = c2.valueExpected = value;
            status.set(value);
            c1.check();
            c2.check();
            lastValue = value;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanStatusBooleanOutputArrayNull() {
        new BooleanStatus((BooleanOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testBooleanStatusBooleanOutputArrayNulls() {
        new BooleanStatus(BooleanOutput.ignored, null);
    }

    @Test
    public void testToggleWhen() {
        EventStatus toggleEvent = new EventStatus();
        status.toggleWhen(toggleEvent);
        tryToggleEvent(toggleEvent);
    }

    @Test(expected = NullPointerException.class)
    public void testToggleWhenNull() {
        status.toggleWhen(null);
    }

    @Test
    public void testGetToggleEvent() {
        tryToggleEvent(status.getToggleEvent());
    }

    private void tryToggleEvent(EventOutput toggleEvent) {
        for (int i = 0; i < 20; i++) {
            boolean wanted = i % 2 == 0;
            status.set(wanted);
            assertEquals(wanted, status.get());
            for (int j = 0; j < 5; j++) {
                wanted = !wanted;
                toggleEvent.event();
                assertEquals(wanted, status.get());
            }
        }
    }

    @Test
    public void testHasConsumers() {
        assertFalse(status.hasConsumers());
        for (int i = 0; i < 5; i++) {
            EventOutput eo = status.sendR(BooleanOutput.ignored);
            assertTrue(status.hasConsumers());
            eo.event();
            assertFalse(status.hasConsumers());
            EventOutput eo1 = status.sendR(BooleanOutput.ignored);
            assertTrue(status.hasConsumers());
            EventOutput eo2 = status.sendR(BooleanOutput.ignored);
            assertTrue(status.hasConsumers());
            eo1.event();
            assertTrue(status.hasConsumers());
            eo2.event();
            assertFalse(status.hasConsumers());
        }
    }

    @Test
    public void testSet() {
        Random rand = new Random();
        CountingBooleanOutput cbo = new CountingBooleanOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = false;
        status.send(cbo);
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0;
            cbo.ifExpected = (value != lastValue);
            cbo.valueExpected = value;
            assertEquals(lastValue, status.get());
            status.set(value);
            assertEquals(value, status.get());
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
        status.send((value) -> {
            ceo.set(status.get());
            ceo2.set(value);
        });
        ceo.check();
        ceo2.check();
        for (int i = 0; i < 5; i++) {
            boolean value = i % 2 == 0;
            ceo.ifExpected = ceo2.ifExpected = true;
            ceo.valueExpected = ceo2.valueExpected = value;
            status.set(value);
            assertEquals(value, status.get());
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
        assertTrue(status.hasConsumers());
        Random rand = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0;
            ceo.ifExpected = (value != lastValue);
            status.set(value);
            ceo.check();
            lastValue = value;
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
        assertTrue(status.hasConsumers());
        Random rand = new Random();
        boolean lastValue = false;
        for (int i = 0; i < 20; i++) {
            boolean value = i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0;
            ceo.ifExpected = (value != lastValue);
            status.set(value);
            ceo.check();
            lastValue = value;
        }
        assertTrue(status.hasConsumers());
        unbind.event();
        assertFalse(status.hasConsumers());
        for (int i = 0; i < 20; i++) {
            status.set(i % 3 == 1 ? rand.nextBoolean() : i % 3 == 0);
            ceo.check();
        }
        assertFalse(status.hasConsumers());
    }

    @Test
    public void testOnUpdateAndOnUpdateR() {
        assertFalse(status.hasConsumers());
        EventOutput unbind = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasConsumers());
        status.onUpdate(EventOutput.ignored);
        assertTrue(status.hasConsumers());
        unbind.event();
        assertTrue(status.hasConsumers());
    }

    @Test
    public void testOnUpdateRTwice() {
        assertFalse(status.hasConsumers());
        EventOutput unbind1 = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasConsumers());
        EventOutput unbind2 = status.onUpdateR(EventOutput.ignored);
        assertTrue(status.hasConsumers());
        unbind1.event();
        assertTrue(status.hasConsumers());
        unbind2.event();
        assertFalse(status.hasConsumers());
    }

    @Test(expected = NullPointerException.class)
    public void testOnUpdateRNull() {
        status.onUpdateR(null);
    }
}
