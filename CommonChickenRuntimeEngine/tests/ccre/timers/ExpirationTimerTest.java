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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventCell;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.scheduler.VirtualTime;
import ccre.testing.CountingEventOutput;

@SuppressWarnings("javadoc")
public class ExpirationTimerTest {

    private ExpirationTimer timer;

    @Before
    public void setUp() throws Exception {
        VirtualTime.startFakeTime();
        timer = new ExpirationTimer();
        timer.start();
        timer.stop();
    }

    @After
    public void tearDown() throws Exception {
        timer.terminate();
        timer = null;
        VirtualTime.endFakeTime();
    }

    @Test
    public void testSimple() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        VirtualTime.forward(2000);
        timer.start();
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
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
        VirtualTime.forward(1500);
        timer.start();
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        timer.feed();
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        timer.stop();
        VirtualTime.forward(1500);
        ceo.check();
    }

    @Test
    public void testScheduleFloatInputEventOutput() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        FloatCell fs = new FloatCell(1.0f);
        timer.schedule(fs, ceo);
        tryVariableScheduling(ceo, fs);
    }

    @Test
    public void testScheduleFloatInput() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        FloatCell fs = new FloatCell(1.0f);
        timer.schedule(fs).send(ceo);
        tryVariableScheduling(ceo, fs);
    }

    private void tryVariableScheduling(CountingEventOutput ceo, FloatCell fs) throws InterruptedException {
        VirtualTime.forward(1500);
        timer.start();
        fs.set(0.5f);
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        timer.feed();
        fs.set(1.5f);
        VirtualTime.forward(490);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        timer.stop();
        VirtualTime.forward(1500);
        timer.start();
        VirtualTime.forward(1490);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
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

        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(490);
        timer.feed();
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(490);
        ceo2.ifExpected = true;
        VirtualTime.forward(10);
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
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(490);
        timer.stop();
        VirtualTime.forward(600);
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
        EventCell start = new EventCell();
        timer.startWhen(start);
        tryStartEvent(start);
    }

    private void tryStartEvent(EventOutput start) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        VirtualTime.forward(1000);
        start.event();
        VirtualTime.forward(990);
        start.event();// should be ignored
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(2000);
        ceo.check();
    }

    @Test
    public void testGetStartOrFeedEvent() throws InterruptedException {
        EventOutput startOrFeed = timer.getStartOrFeedEvent();
        tryStartOrFeedEvent(startOrFeed);
    }

    @Test
    public void testStartOrFeedWhen() throws InterruptedException {
        EventCell startOrFeed = new EventCell();
        timer.startOrFeedWhen(startOrFeed);
        tryStartOrFeedEvent(startOrFeed);
    }

    private void tryStartOrFeedEvent(EventOutput startOrFeed) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        VirtualTime.forward(1000);
        startOrFeed.event();
        VirtualTime.forward(990);
        startOrFeed.event();// should not be ignored
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(2000);
        ceo.check();
    }

    @Test
    public void testGetFeedEventNotRunning() throws InterruptedException {
        EventOutput feed = timer.getFeedEvent();
        tryFeedEventNotRunning(feed);
    }

    @Test
    public void testFeedWhenNotRunning() throws InterruptedException {
        EventCell feed = new EventCell();
        timer.feedWhen(feed);
        tryFeedEventNotRunning(feed);
    }

    private void tryFeedEventNotRunning(EventOutput feed) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        timer.start();
        ceo.ifExpected = true;
        VirtualTime.forward(1000);
        ceo.check(); // flaky; 1 failure
        timer.stop();
        feed.event();
        VirtualTime.forward(2000);
        feed.event();
        ceo.check();
    }

    @Test
    public void testGetFeedEventRunning() throws InterruptedException {
        EventOutput feed = timer.getFeedEvent();
        tryFeedEventRunning(feed);
    }

    @Test
    public void testFeedWhenRunning() throws InterruptedException {
        EventCell feed = new EventCell();
        timer.feedWhen(feed);
        tryFeedEventRunning(feed);
    }

    private void tryFeedEventRunning(EventOutput feed) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        VirtualTime.forward(1000);
        timer.start();
        VirtualTime.forward(990);
        feed.event();// should not be ignored
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(2000);
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
        EventCell stop = new EventCell();
        timer.stopWhen(stop);
        tryStopEvent(stop);
    }

    private void tryStopEvent(EventOutput stop) throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        CountingEventOutput ceo2 = new CountingEventOutput();
        timer.schedule(1500, ceo2);
        timer.start();
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check(); // NOTE: flaky here: 1 nonoccurrence.
        VirtualTime.forward(490);
        stop.event();
        VirtualTime.forward(600);
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
        VirtualTime.forward(2000);
        control.set(true);
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(490);
        control.set(false);
        VirtualTime.forward(2000);
        ceo.check();
        ceo2.check();
    }

    @Test(expected = NullPointerException.class)
    public void testRunWhenNull() {
        timer.runWhen(null);
    }

    @Test
    public void testRunWhen() throws InterruptedException {
        BooleanCell run = new BooleanCell();
        timer.runWhen(run);
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        CountingEventOutput ceo2 = new CountingEventOutput();
        timer.schedule(1500, ceo2);
        VirtualTime.forward(2000);
        run.set(true);
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(490);
        run.set(false);
        VirtualTime.forward(2000);
        ceo.check();
        ceo2.check();
    }

    @Test
    public void testGetRunning_Write() throws InterruptedException {
        BooleanOutput control = timer.getRunning();
        CountingEventOutput ceo = new CountingEventOutput();
        timer.schedule(1000, ceo);
        CountingEventOutput ceo2 = new CountingEventOutput();
        timer.schedule(1500, ceo2);
        VirtualTime.forward(2000);
        control.set(true);
        VirtualTime.forward(990);
        ceo.ifExpected = true;
        VirtualTime.forward(10);
        ceo.check();
        VirtualTime.forward(490);
        control.set(false);
        VirtualTime.forward(2000);
        ceo.check();
        ceo2.check();
    }

    @Test
    public void testGetRunning_Read() {
        BooleanInput running = timer.getRunning();
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
        VirtualTime.forward(100);
        assertEquals(running, timer.isRunning());
    }
}
