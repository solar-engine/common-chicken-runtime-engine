/*
 * Copyright 2016 Cel Skeggs
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
package ccre.timers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.channel.EventCell;
import ccre.channel.EventOutput;
import ccre.log.LogLevel;
import ccre.log.VerifyingLogger;
import ccre.scheduler.TestingSchedulerSecrets;
import ccre.testing.CountingFloatOutput;
import ccre.time.FakeTime;
import ccre.time.Time;

@SuppressWarnings("javadoc")
public class StopwatchTimerTest {

    private static final String ERROR_STRING = "Stopwatch purposeful failure.";

    private static Time oldProvider;
    private static FakeTime fake;
    private StopwatchTimer stopwatch;
    private CountingFloatOutput cfo;

    @BeforeClass
    public static void setUpClass() {
        assertNull(oldProvider);
        oldProvider = Time.getTimeProvider();
        fake = new FakeTime();
        Time.setTimeProvider(fake);
    }

    @Before
    public void setUp() {
        cfo = new CountingFloatOutput();
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() {
        VerifyingLogger.checkAndEnd();
        cfo = null;
        stopwatch = null;
        TestingSchedulerSecrets.resetScheduler();
    }

    @AfterClass
    public static void tearDownClass() {
        assertNotNull(oldProvider);
        Time.setTimeProvider(oldProvider);
        oldProvider = null;
        fake = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePeriod() {
        new StopwatchTimer(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroPeriod() {
        new StopwatchTimer(0);
    }

    @Test
    public void testBasic() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        assertEquals(stopwatch.get(), 0, 0);
        for (int i = 1; i < 50; i++) {
            Thread.sleep(1);
            fake.forward(10);
            assertEquals(i / 100.0, stopwatch.get(), 0.000001);
        }
    }

    @Test
    public void testReset() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        for (int n = 0; n < 5; n++) {
            stopwatch.reset();
            assertEquals(stopwatch.get(), 0, 0);
            for (int i = 1; i < 10; i++) {
                Thread.sleep(1);
                fake.forward(10);
                assertEquals(i / 100.0, stopwatch.get(), 0.000001);
            }
        }
    }

    @Test
    public void testEventReset() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        EventOutput eo = stopwatch.eventReset();
        for (int n = 0; n < 5; n++) {
            eo.event();
            assertEquals(stopwatch.get(), 0, 0);
            for (int i = 1; i < 10; i++) {
                Thread.sleep(1);
                fake.forward(10);
                assertEquals(i / 100.0, stopwatch.get(), 0.000001);
            }
        }
    }

    @Test
    public void testResetWhen() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        EventCell ec = new EventCell();
        stopwatch.resetWhen(ec);
        for (int n = 0; n < 5; n++) {
            ec.event();
            assertEquals(stopwatch.get(), 0, 0);
            for (int i = 1; i < 10; i++) {
                Thread.sleep(1);
                fake.forward(10);
                assertEquals(i / 100.0, stopwatch.get(), 0.000001);
            }
        }
    }

    @Test
    public void testSent() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        cfo.ifExpected = true;
        cfo.valueExpected = 0;
        stopwatch.send(cfo);
        cfo.check();
        for (int i = 1; i < 50; i++) {
            Thread.sleep(1);
            fake.forward(9);
            cfo.ifExpected = true;
            cfo.valueExpected = i / 100f;
            Thread.sleep(1);
            fake.forward(1);
            cfo.check();
        }
    }

    @Test
    public void testExact() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        assertEquals(stopwatch.get(), 0, 0);
        for (int i = 1; i < 50; i++) {
            Thread.sleep(1);
            fake.forward(9);
            assertEquals((i - 1) / 100f, stopwatch.get(), 0);
            Thread.sleep(1);
            fake.forward(1);
            assertEquals(i / 100f, stopwatch.get(), 0);
        }
    }

    @Test
    public void testSlurred() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        assertEquals(stopwatch.get(), 0, 0);
        for (int i = 1; i < 40; i++) {
            Thread.sleep(1);
            fake.forward(9);
            assertEquals((i - 1) * 15 / 1000f, stopwatch.get(), 0);
            Thread.sleep(1);
            fake.forward(6); // five ms too long
            // so we expect the marks to be at multiples of 15 ms instead
            assertEquals(i * 15 / 1000f, stopwatch.get(), 0);
        }
    }

    @Test
    public void testVariedPeriods() throws InterruptedException {
        for (int i = 2; i < 400; i *= 2) {
            stopwatch = new StopwatchTimer(i);
            assertEquals(stopwatch.get(), 0, 0);
            for (int j = 1; j < 10; j++) {
                fake.forward(i - 1);
                assertEquals((j - 1) * i / 1000f, stopwatch.get(), 0);
                Thread.sleep(1);
                fake.forward(1);
                assertEquals(j * i / 1000f, stopwatch.get(), 0);
            }
            stopwatch = null;
        }
    }

    private int counter;

    @Test
    public void testErrors() throws InterruptedException {
        stopwatch = new StopwatchTimer();
        counter = 0;
        stopwatch.onUpdate(() -> {
            synchronized (StopwatchTimerTest.this) {
                counter++;
                if (counter < 6) {
                    throw new RuntimeException(ERROR_STRING);
                }
            }
        });
        for (int i = 0; i < 20; i++) {
            if (i < 5) {
                VerifyingLogger.configure(LogLevel.SEVERE, "Top-level failure in scheduled event", (t) -> t.getClass() == RuntimeException.class && ERROR_STRING.equals(t.getMessage()));
            }
            Thread.sleep(1);
            fake.forward(10);
            VerifyingLogger.check();
        }
    }
}
