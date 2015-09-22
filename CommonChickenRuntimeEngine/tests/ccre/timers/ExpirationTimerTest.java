package ccre.timers;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatStatus;
import ccre.testing.CountingEventOutput;
import ccre.time.FakeTime;
import ccre.time.Time;

public class ExpirationTimerTest {

    private static Time oldProvider;
    private static FakeTime fake;
    private ExpirationTimer timer;

    @BeforeClass
    public static void setUpClass() {
        assertNull(oldProvider);
        oldProvider = Time.getTimeProvider();
        fake = new FakeTime();
        Time.setTimeProvider(fake);
    }

    @AfterClass
    public static void tearDownClass() {
        assertNotNull(oldProvider);
        Time.setTimeProvider(oldProvider);
        oldProvider = null;
        fake = null;
    }

    @Before
    public void setUp() throws Exception {
        timer = new ExpirationTimer();
        timer.start();
        Thread.sleep(2);
        timer.stop();
        Thread.sleep(2);
    }

    @After
    public void tearDown() throws Exception {
        timer.terminate();
        timer = null;
    }

    @Test
    public void testSimple() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        fake.forward(2000);
        Thread.sleep(2);
        timer.start();
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
    }

    @Test
    public void testScheduleLongEventOutput() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        tryFixedScheduling(ceo);
    }

    @Test
    public void testScheduleLong() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000).send(ceo);
        tryFixedScheduling(ceo);
    }

    private void tryFixedScheduling(CountingEventOutput ceo) throws InterruptedException {
        fake.forward(1500);
        timer.start();
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        timer.feed();
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        timer.stop();
        Thread.sleep(2);
        fake.forward(1500);
        Thread.sleep(2);
        ceo.check();
    }

    @Test
    public void testScheduleFloatInputEventOutput() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        FloatStatus fs = new FloatStatus(1.0f);
        timer.schedule(fs, ceo);
        tryVariableScheduling(ceo, fs);
    }

    @Test
    public void testScheduleFloatInput() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        FloatStatus fs = new FloatStatus(1.0f);
        timer.schedule(fs).send(ceo);
        tryVariableScheduling(ceo, fs);
    }

    private void tryVariableScheduling(CountingEventOutput ceo, FloatStatus fs) throws InterruptedException {
        fake.forward(1500);
        timer.start();
        Thread.sleep(2);
        fs.set(0.5f);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        timer.feed();
        Thread.sleep(2);
        fs.set(1.5f);
        fake.forward(490);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        timer.stop();
        Thread.sleep(2);
        fake.forward(1500);
        timer.start();
        Thread.sleep(2);
        fake.forward(1490);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
    }

    @Test
    public void testStartFeedStopSequence() {
        for (int i = 0; i < 10; i++) {
            timer.start();
            for (int j = 0; j < i % 3; j++) {
                timer.feed();
            }
            timer.stop();
        }
    }

    @Test
    public void testStartStopSequence() {
        for (int i = 0; i < 10; i++) {
            timer.start();
            timer.stop();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidStop() {
        timer.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidStop2() {
        timer.start();
        timer.stop();
        timer.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidFeed() {
        timer.feed();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidFeed2() {
        timer.start();
        timer.stop();
        timer.feed();
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStart() {
        timer.start();
        timer.start();
    }

    @Test
    public void testStartOrFeedInitial() {
        timer.startOrFeed();
    }

    @Test
    public void testStartOrFeedRepeated() {
        timer.startOrFeed();
        timer.startOrFeed();
    }

    @Test
    public void testStartOrFeedSecondary() {
        timer.start();
        timer.startOrFeed();
    }

    @Test
    public void testFeedPartialRepeat() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        CountingEventOutput ceo2 = new CountingEventOutput();
        timer.schedule(1500, ceo2);
        timer.start();
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(490);
        timer.feed();
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(490);
        ceo2.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        ceo2.check();
    }

    @Test
    public void testStopPartial() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        CountingEventOutput ceo2 = new CountingEventOutput();
        timer.schedule(1500, ceo2);
        timer.start();
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(490);
        timer.stop();
        Thread.sleep(2);
        fake.forward(600);
        Thread.sleep(2);
        ceo.check();
        ceo2.check();
    }

    @Test
    public void testGetStartEvent() throws InterruptedException {
        EventOutput start = timer.getStartEvent();
        tryStartEvent(start);
    }

    @Test
    public void testStartWhen() throws InterruptedException {
        EventStatus start = new EventStatus();
        timer.startWhen(start);
        tryStartEvent(start);
    }

    private void tryStartEvent(EventOutput start) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        fake.forward(1000);
        start.event();
        Thread.sleep(2);
        fake.forward(990);
        start.event();// should be ignored
        Thread.sleep(2);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(2000);
        Thread.sleep(2);
        ceo.check();
    }

    @Test
    public void testGetStartOrFeedEvent() throws InterruptedException {
        EventOutput startOrFeed = timer.getStartOrFeedEvent();
        tryStartOrFeedEvent(startOrFeed);
    }

    @Test
    public void testStartOrFeedWhen() throws InterruptedException {
        EventStatus startOrFeed = new EventStatus();
        timer.startOrFeedWhen(startOrFeed);
        tryStartOrFeedEvent(startOrFeed);
    }

    private void tryStartOrFeedEvent(EventOutput startOrFeed) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        fake.forward(1000);
        startOrFeed.event();
        Thread.sleep(2);
        fake.forward(990);
        startOrFeed.event();// should not be ignored
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(2000);
        Thread.sleep(2);
        ceo.check();
    }

    @Test
    public void testGetFeedEventNotRunning() throws InterruptedException {
        EventOutput feed = timer.getFeedEvent();
        tryFeedEventNotRunning(feed);
    }

    @Test
    public void testFeedWhenNotRunning() throws InterruptedException {
        EventStatus feed = new EventStatus();
        timer.feedWhen(feed);
        tryFeedEventNotRunning(feed);
    }

    private void tryFeedEventNotRunning(EventOutput feed) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        timer.start();
        Thread.sleep(2);
        ceo.ifExpected = true;
        fake.forward(1000);
        Thread.sleep(2);
        ceo.check();
        timer.stop();
        Thread.sleep(2);
        feed.event();
        Thread.sleep(2);
        fake.forward(2000);
        feed.event();
        Thread.sleep(2);
        ceo.check();
    }

    @Test
    public void testGetFeedEventRunning() throws InterruptedException {
        EventOutput feed = timer.getFeedEvent();
        tryFeedEventRunning(feed);
    }

    @Test
    public void testFeedWhenRunning() throws InterruptedException {
        EventStatus feed = new EventStatus();
        timer.feedWhen(feed);
        tryFeedEventRunning(feed);
    }

    private void tryFeedEventRunning(EventOutput feed) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        fake.forward(1000);
        timer.start();
        Thread.sleep(2);
        fake.forward(990);
        feed.event();// should not be ignored
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(2000);
        Thread.sleep(2);
        ceo.check();
    }

    @Test
    public void testGetStopEventSafe() {
        timer.getStopEvent().event();
    }

    @Test
    public void testGetStopEvent() throws InterruptedException {
        EventOutput stop = timer.getStopEvent();
        tryStopEvent(stop);
    }

    @Test
    public void testStopWhen() throws InterruptedException {
        EventStatus stop = new EventStatus();
        timer.stopWhen(stop);
        tryStopEvent(stop);
    }

    private void tryStopEvent(EventOutput stop) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        CountingEventOutput ceo2 = new CountingEventOutput();
        timer.schedule(1500, ceo2);
        timer.start();
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(490);
        stop.event();
        Thread.sleep(2);
        fake.forward(600);
        Thread.sleep(2);
        ceo.check();
        ceo2.check();
    }

    @Test
    public void testGetRunningControl() throws InterruptedException {
        BooleanOutput control = timer.getRunningControl();
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        CountingEventOutput ceo2 = new CountingEventOutput();
        timer.schedule(1500, ceo2);
        fake.forward(2000);
        control.set(true);
        Thread.sleep(2);
        fake.forward(990);
        ceo.ifExpected = true;
        fake.forward(10);
        Thread.sleep(2);
        ceo.check();
        fake.forward(490);
        control.set(false);
        Thread.sleep(2);
        fake.forward(2000);
        Thread.sleep(2);
        ceo.check();
        ceo2.check();
    }

    @Test
    public void testGetRunningStatus() {
        BooleanInput running = timer.getRunningStatus();
        for (int i = 0; i < 3; i++) {
            assertFalse(running.get());
            timer.start();
            assertTrue(running.get());
            timer.feed();
            assertTrue(running.get());
            timer.feed();
            assertTrue(running.get());
            timer.stop();
            assertFalse(running.get());
        }
    }

    @Test
    public void testIsRunning() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            checkRunning(false);
            timer.start();
            checkRunning(true);
            timer.feed();
            checkRunning(true);
            timer.feed();
            checkRunning(true);
            timer.stop();
            checkRunning(false);
        }
    }

    private void checkRunning(boolean running) throws InterruptedException {
        assertEquals(running, timer.isRunning());
        fake.forward(100);
        assertEquals(running, timer.isRunning());
    }
}
