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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.channel.FloatCell;
import ccre.log.LogLevel;
import ccre.log.VerifyingLogger;
import ccre.scheduler.VirtualTime;
import ccre.testing.CountingEventOutput;

@SuppressWarnings("javadoc")
public class PauseTimerTest {

    private PauseTimer pt;

    @Before
    public void setUp() throws Exception {
        VirtualTime.startFakeTime();
        pt = new PauseTimer(1000);
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() throws Exception {
        VerifyingLogger.checkAndEnd();
        pt.terminate();
        pt = null;
        VirtualTime.endFakeTime();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPauseTimerZero() {
        new PauseTimer(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPauseTimerNegative() {
        new PauseTimer(-1);
    }

    @Test
    public void testPauseTimerSmallPositive() {
        new PauseTimer(1);
    }

    @Test(expected = NullPointerException.class)
    public void testPauseTimerNull() {
        new PauseTimer(null);
    }

    @Test
    public void testNoDefaultTrigger() throws InterruptedException {
        CountingEventOutput con = new CountingEventOutput();
        CountingEventOutput coff = new CountingEventOutput();
        pt.triggerAtChanges(con, coff);
        VirtualTime.forward(700);
        VirtualTime.forward(700);
        VirtualTime.forward(700);
        // if it ever fires here, then ceo will get annoyed
    }

    @Test
    public void testEventAllAtOnce() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        pt.onRelease().send(ceo);
        VirtualTime.forward(2000);
        startEvt();
        ceo.ifExpected = true;
        VirtualTime.forward(1000);
        ceo.check();
    }

    @Test
    public void testTriggerVariance() throws InterruptedException {
        CountingEventOutput coff = new CountingEventOutput();
        FloatCell time = new FloatCell(1.0f);
        pt = new PauseTimer(time);
        pt.triggerAtEnd(coff);
        for (int i = 10; i < 3000; i *= 2) {
            time.set(i / 1000f);
            pt.event();
            VirtualTime.forward(i - 5);
            coff.ifExpected = true;
            VirtualTime.forward(5);
            coff.check();
        }
    }

    @Test
    public void testTriggerAtEnd() throws InterruptedException {
        CountingEventOutput coff = new CountingEventOutput();
        pt.triggerAtEnd(coff);
        startEvt();
        VirtualTime.forward(990);
        coff.ifExpected = true;
        VirtualTime.forward(10);
        coff.check();
    }

    @Test
    public void testTriggerAtStart() throws InterruptedException {
        CountingEventOutput con = new CountingEventOutput();
        pt.triggerAtStart(con);
        con.ifExpected = true;
        startEvt();
        con.check();
        VirtualTime.forward(1000);
    }

    @Test
    public void testTriggerAtChanges() throws InterruptedException {
        CountingEventOutput con = new CountingEventOutput();
        CountingEventOutput coff = new CountingEventOutput();
        pt.triggerAtChanges(con, coff);
        VirtualTime.forward(2000);
        con.ifExpected = true;
        startEvt();
        con.check();
        VirtualTime.forward(990);
        coff.ifExpected = true;
        VirtualTime.forward(10);
        coff.check();
    }

    @Test
    public void testEventSequenceMultiPress() throws InterruptedException {
        CountingEventOutput con = new CountingEventOutput();
        CountingEventOutput coff = new CountingEventOutput();
        pt.triggerAtChanges(con, coff);
        VirtualTime.forward(2000);
        con.ifExpected = true;
        startEvt();
        con.check();
        VirtualTime.forward(500);
        startEvt();
        VirtualTime.forward(990);
        coff.ifExpected = true;
        VirtualTime.forward(10);
        coff.check();
    }

    @Test
    public void testGet() throws InterruptedException {
        assertFalse(pt.get());
        startEvt();
        assertTrue(pt.get());
        for (int i = 0; i < 11; i++) {
            VirtualTime.forward(90);
            assertTrue(pt.get());
        }
        VirtualTime.forward(10);
        assertFalse(pt.get()); // flaky
    }

    private void startEvt() throws InterruptedException {
        pt.event();
    }

    @Test
    public void testGetMultiEvent() throws InterruptedException {
        assertFalse(pt.get());
        startEvt();
        assertTrue(pt.get());
        for (int i = 0; i < 11; i++) {
            VirtualTime.forward(90);
            assertTrue(pt.get());
        }
        startEvt();
        assertTrue(pt.get());
        for (int i = 0; i < 11; i++) {
            VirtualTime.forward(90);
            assertTrue(pt.get());
        }
        VirtualTime.forward(10);
        assertFalse(pt.get());
    }

    @Test
    public void testStartError() {
        CountingEventOutput ceo = new CountingEventOutput();
        RuntimeException rtex = new RuntimeException("Purposeful failure.");
        pt.triggerAtStart(() -> {
            ceo.event();
            // TODO: test logging
            throw rtex;
        });
        ceo.ifExpected = true;
        VerifyingLogger.configure(LogLevel.SEVERE, "Failure while starting PauseTimer", rtex);
        pt.event();
        VerifyingLogger.check();
        ceo.check();
    }

    @Test
    public void testEndError() throws InterruptedException {
        CountingEventOutput ceo = new CountingEventOutput();
        RuntimeException rtex = new RuntimeException("Purposeful failure.");
        pt.triggerAtEnd(() -> {
            ceo.event();
            // TODO: test logging
            throw rtex;
        });
        for (int i = 0; i < 10; i++) {
            // if something breaks internally, this loop will stop succeeding.
            pt.event();
            VirtualTime.forward(990);
            ceo.ifExpected = true;
            VerifyingLogger.configure(LogLevel.SEVERE, "Top-level failure in scheduled event", rtex);
            VirtualTime.forward(10);
            ceo.check(); // flaky; 5 failures.
            VerifyingLogger.check();
        }
    }
}
