/*
 * Copyright 2016 Alexander Mackworth
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

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.time.FakeTime;
import ccre.time.Time;

public class ElapsedTimerTest {
    private static Time oldProvider;
    private static FakeTime fake;

    @BeforeClass
    public static void setUpClass() throws Exception {
        assertNull(oldProvider);
        oldProvider = Time.getTimeProvider();
        fake = new FakeTime();
        Time.setTimeProvider(fake);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        assertNotNull(oldProvider);
        Time.setTimeProvider(oldProvider);
        oldProvider = null;
        fake = null;
    }

    @Test
    public void testElapsedTimer() throws InterruptedException {
        ElapsedTimer timer = new ElapsedTimer(1);
        Random rand = new Random();
        
        timer.start();
        float currentTime = timer.getElapsedTimeMillis().get();
        for (int i = 0; i < 100; i++) {
            int delay = rand.nextInt(10000);
            fake.forward(delay);
            Thread.sleep(2);
            currentTime += delay;
            assertEquals(currentTime, timer.getElapsedTimeMillis().get(), 0);
        }
    }
    
    @Test
    public void testPeriodLargerThanDelay() throws InterruptedException {
        Random rand = new Random();

        int firstDelay = rand.nextInt(100);
        int secondDelay = rand.nextInt(100);

        ElapsedTimer timer = new ElapsedTimer(firstDelay > secondDelay ? firstDelay : secondDelay);

        timer.start();
        fake.forward(firstDelay < secondDelay ? firstDelay : secondDelay);
        assertEquals(0, timer.getElapsedTimeMillis().get(), 0);
    }
}
