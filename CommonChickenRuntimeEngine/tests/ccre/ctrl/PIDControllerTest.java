/*
 * Copyright 2015 Colby Skeggs.
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
package ccre.ctrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatStatus;
import ccre.testing.CountingEventOutput;
import ccre.time.FakeTime;
import ccre.time.Time;

public class PIDControllerTest {

    private FloatStatus input, setpoint, P, I, D;
    private PIDController pid;

    @Before
    public void setUp() throws Exception {
        input = new FloatStatus(0);
        setpoint = new FloatStatus(0);
        P = new FloatStatus(1);
        I = new FloatStatus(0.1f);
        D = new FloatStatus(0.01f);
        pid = new PIDController(input, setpoint, P, I, D);
    }

    @After
    public void tearDown() throws Exception {
        input = setpoint = null;
        pid = null;
    }

    @Test
    public void testCreateFixed() {
        EventStatus updateOn = new EventStatus();
        setpoint.set(0);
        PIDController cf = PIDController.createFixed(updateOn, input, setpoint, P.get(), I.get(), D.get());
        setpoint.set(1);
        cf.update(1000);
        assertEquals(cf.get(), P.get() + I.get() + D.get(), 0.000001);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateFixedNullA() {
        PIDController.createFixed(null, input, setpoint, 1, 1, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateFixedNullB() {
        PIDController.createFixed(EventInput.never, null, setpoint, 1, 1, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateFixedNullC() {
        PIDController.createFixed(EventInput.never, input, null, 1, 1, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateNullA() {
        new PIDController(null, setpoint, P, I, D);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateNullB() {
        new PIDController(input, null, P, I, D);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateNullC() {
        new PIDController(input, setpoint, null, I, D);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateNullD() {
        new PIDController(input, setpoint, P, null, D);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateNullE() {
        new PIDController(input, setpoint, P, I, null);
    }

    @Test
    public void testPIDControllerSignsPositive() {
        input.set(5);
        setpoint.set(10);
        // which means that an update should yield a positive result, because we
        // want to have a greater input!
        pid.update(1000);
        assertTrue(pid.get() > 0);
    }

    @Test
    public void testPIDControllerSignsNegative() {
        input.set(10);
        setpoint.set(5);
        // which means that an update should yield a negative result, because we
        // want to have a smaller input!
        pid.update(1000);
        assertTrue(pid.get() < 0);
    }

    @Test
    public void testSetOutputBoundsFloat() {
        input.set(0);
        setpoint.set(100);
        pid.setOutputBounds(0.3f);
        pid.update(1000);
        assertTrue(Math.abs(pid.get()) <= 0.3f);
        setpoint.set(-10000);
        pid.update(1000);
        assertTrue(Math.abs(pid.get()) <= 0.3f);
    }

    @Test
    public void testSetOutputBoundsFloatInput() {
        input.set(0);
        setpoint.set(100);
        FloatStatus bound = new FloatStatus(0.4f);
        pid.setOutputBounds(bound);
        pid.update(1000);
        assertTrue(Math.abs(pid.get()) == 0.4f);
        setpoint.set(-10000);
        bound.set(0.2f);
        pid.update(1000);
        assertTrue(Math.abs(pid.get()) == 0.2f);
    }

    @Test
    public void testSetIntegralBoundsFloat() {
        input.set(0);
        setpoint.set(1000);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() > 10);
        pid.integralTotal.set(0);
        setpoint.set(0);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() == 0);
        pid.setIntegralBounds(1.3f);
        setpoint.set(1000);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() == 1.3f);
        setpoint.set(-10000000);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() == -1.3f);
    }

    @Test
    public void testSetIntegralBoundsFloatInput() {
        input.set(0);
        setpoint.set(1000);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() > 10);
        pid.integralTotal.set(0);
        setpoint.set(0);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() == 0);
        FloatStatus ib = new FloatStatus(1.7f);
        pid.setIntegralBounds(ib);
        setpoint.set(1000);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() == 1.7f);
        ib.set(1.8f);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() == 1.8f);
        ib.set(1.1f);
        pid.update(1000);
        assertTrue(pid.integralTotal.get() == 1.1f);
    }

    @Test
    public void testSetMaximumTimeDeltaFloat() {
        pid = new PIDController(input, setpoint, FloatInput.zero, I, FloatInput.zero);
        pid.setMaximumTimeDelta(0.1f);
        setpoint.set(1);
        pid.update(650);
        float shortValue = pid.get();
        pid = new PIDController(input, setpoint, FloatInput.zero, I, FloatInput.zero);
        pid.setMaximumTimeDelta(0.6f);
        setpoint.set(1);
        pid.update(650);
        float longValue = pid.get();
        assertEquals(longValue, shortValue * 6, longValue / 1000);
    }

    @Test
    public void testSetMaximumTimeDeltaFloatInput() {
        FloatStatus max = new FloatStatus(0.1f);
        pid = new PIDController(input, setpoint, FloatInput.zero, I, FloatInput.zero);
        pid.setMaximumTimeDelta(max);
        setpoint.set(1);
        pid.update(650);
        float shortValue = pid.get();
        setpoint.set(0);
        max.set(0.6f);
        pid.update(1000);
        pid.integralTotal.set(0);
        setpoint.set(1);
        pid.update(650);
        float longValue = pid.get();
        assertEquals(longValue, shortValue * 6, longValue / 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNegative() {
        pid.update(-1);
    }

    @Test
    public void testEvent() throws InterruptedException {
        setpoint.set(1);
        pid = new PIDController(input, setpoint, P, I, D);
        pid.update(1000);
        pid.update(50);
        float result = pid.get();
        FakeTime ft = new FakeTime();
        Time old = Time.getTimeProvider();
        Time.setTimeProvider(ft);
        ft.forward(123429);
        pid = new PIDController(input, setpoint, P, I, D);
        pid.event();
        ft.forward(50);
        pid.event();
        assertEquals(result, pid.get(), 0.000001);
        Time.setTimeProvider(old);
    }

    @Test
    public void testOnUpdate() {
        setpoint.set(1);
        CountingEventOutput ceo = new CountingEventOutput();
        pid.onUpdate(ceo);
        ceo.ifExpected = true;
        pid.update(1000);
        ceo.check();
        ceo.ifExpected = true;
        pid.update(1000);
        ceo.check();
    }

    @Test
    public void testOnUpdateR() {
        setpoint.set(1);
        CountingEventOutput ceo = new CountingEventOutput();
        EventOutput unbind = pid.onUpdateR(ceo);
        ceo.ifExpected = true;
        pid.update(1000);
        ceo.check();
        unbind.event();
        pid.update(1000);
    }

    @Test
    public void testSums() throws Throwable {
        setpoint.set(1);
        pid.update(1000);
        float total = pid.get();

        pid = new PIDController(input, setpoint, P, FloatInput.zero, FloatInput.zero);
        pid.update(1000);
        float p = pid.get();

        pid = new PIDController(input, setpoint, FloatInput.zero, I, FloatInput.zero);
        pid.update(1000);
        float i = pid.get();

        pid = new PIDController(input, setpoint, FloatInput.zero, FloatInput.zero, D);
        pid.update(1000);
        float d = pid.get();

        assertEquals(total, p + i + d, 0.00001f);
    }

    @Test
    public void testProportional() throws Throwable {
        pid = new PIDController(input, setpoint, P, FloatInput.zero, FloatInput.zero);
        input.set(1);
        setpoint.set(3.2f);
        pid.update(100);
        assertEquals(setpoint.get() - input.get(), pid.get(), 0.0001f);
    }

    @Test
    public void testIntegral() throws Throwable {
        pid = new PIDController(input, setpoint, FloatInput.zero, I, FloatInput.zero);
        input.set(1);
        setpoint.set(3.2f);
        float base_integral = 1.773f;
        pid.integralTotal.set(base_integral);
        pid.update(100);
        assertEquals(0.1f * (setpoint.get() - input.get()), pid.integralTotal.get() - base_integral, 0.0001f);
        assertEquals(I.get() * pid.integralTotal.get(), pid.get(), 0.0001f);
    }

    @Test
    public void testDerivative() throws Throwable {
        setpoint.set(1);
        pid = new PIDController(input, setpoint, FloatInput.zero, FloatInput.zero, D);
        pid.update(100);
        assertTrue(pid.getPreviousError() == 1);
        setpoint.set(3.2f);
        pid.update(100);
        assertTrue(pid.getPreviousError() == 3.2f);
        assertEquals(D.get() * (3.2f - 1.0f) / (0.1f), pid.get(), 0.00001f);
    }

    @Test
    public void testGetPreviousError() throws Throwable {
        assertTrue(pid.getPreviousError() == 0);
        setpoint.set(1);
        pid.update(100);
        assertTrue(pid.getPreviousError() == setpoint.get() - input.get());
    }

    @Test
    public void testForNaNs() {
        input.set(Float.NaN);
        pid.update(100);
        assertTrue(Float.isNaN(pid.get()));
        input.set(Float.POSITIVE_INFINITY);
        pid.update(100);
        assertTrue(Float.isNaN(pid.get()));
        input.set(Float.NEGATIVE_INFINITY);
        pid.update(100);
        assertTrue(Float.isNaN(pid.get()));
        // does it keep functioning?
        input.set(0);
        pid.update(100);
        assertEquals(pid.get(), 0, 0);
    }

    @Test
    public void testZeroLengthInfo() {
        input.set(1000000);
        pid.update(0);
        input.set(0);
        pid.update(100);
        assertEquals(pid.get(), 0, 0);
    }

    @Test
    public void testPractical() throws Throwable {
        pid.setIntegralBounds(0.05f);
        pid.setOutputBounds(1.1f);
        for (float sp : new float[] { 10, -5, 0, 3 }) {
            setpoint.set(sp);
            for (int i = 0; i < 1000; i++) {
                input.set(input.get() + pid.get() * 0.05f);
                pid.update(20);
            }
            assertEquals(input.get(), sp, 0.01f);
        }
    }
}
