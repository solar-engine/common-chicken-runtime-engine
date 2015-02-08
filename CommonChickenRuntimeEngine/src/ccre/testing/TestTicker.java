/*
 * Copyright 2014-2015 Colby Skeggs and Vincent Miller
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
package ccre.testing;

import ccre.channel.EventOutput;
import ccre.ctrl.EventMixing;
import ccre.ctrl.Ticker;
import ccre.log.Logger;

/**
 * Tests the Ticker class.
 * 
 * @author skeggsc
 */
public class TestTicker extends BaseTest {

    @Override
    public String getName() {
        return "Ticker Test";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        runCorrect();
        runBroken();
    }

    private void runCorrect() throws TestingException, InterruptedException {
        final int[] cur = new int[1];
        EventOutput a = new EventOutput() {
            public void event() {
                try {
                    Thread.sleep(15);
                    synchronized (cur) {
                        cur[0]++;
                    }
                } catch (InterruptedException ex) {
                    // Ignore it.
                }
            }
        };
        {
            cur[0] = 0;
            Ticker t = new Ticker(19, true);
            try {
                t.send(a);
                Thread.sleep(499);
                synchronized (cur) {
                    assertTrue(23 <= cur[0] && cur[0] <= 26, "Bad Ticker count: " + cur[0]);
                }
            } finally {
                t.terminate();
            }
        }
        Thread.sleep(30);
        {
            synchronized (cur) {
                cur[0] = 0;
            }
            Ticker t = new Ticker(19, false);
            try {
                t.send(a);
                t.unsend(a);
                synchronized (cur) {
                    assertTrue(cur[0] == 0, "Bad Ticker count: " + cur[0]);
                }
                Thread.sleep(499);
                synchronized (cur) {
                    assertTrue(cur[0] == 0, "Bad Ticker count: " + cur[0]);
                }
                t.send(a);
                Thread.sleep(499);
                synchronized (cur) {
                    assertTrue(13 <= cur[0] && cur[0] <= 15, "Bad Ticker count: " + cur[0]);
                }
            } finally {
                t.terminate();
            }
        }
        Thread.sleep(30);
        {
            cur[0] = 0;
            Ticker t = new Ticker(19);
            try {
                t.send(a);
                Thread.sleep(499);
                synchronized (cur) {
                    assertTrue(13 <= cur[0] && cur[0] <= 14, "Bad Ticker count!");
                }
            } finally {
                t.terminate();
            }
            try {
                t.send(EventMixing.ignored);
                assertFail("Expected a failure!");
            } catch (IllegalStateException exc) {
                // Correct!
            }
        }
    }

    private void runBroken() throws TestingException, InterruptedException {
        Ticker t = new Ticker(19);
        try {
            final int[] ctr = new int[1];
            Logger.info("The following ticker main loop errors are purposeful.");
            t.send(new EventOutput() {
                public void event() {
                    synchronized (ctr) {
                        ctr[0]++;
                    }
                    throw new RuntimeException("Ticker purposeful failure.");
                }
            });
            Thread.sleep(201);
            synchronized (ctr) {
                assertTrue(ctr[0] == 6, "Should not have counted that number: " + ctr[0]);
                ctr[0] = 0;
            }
            Thread.sleep(120);
            synchronized (ctr) {
                assertTrue(ctr[0] == 0, "Should not have counted at all!");
            }
        } finally {
            t.terminate();
        }

        t = new Ticker(19);
        try {
            final int[] ctr = new int[1];
            Logger.info("The following ticker main loop errors are purposeful.");
            t.send(new EventOutput() {
                public void event() {
                    synchronized (ctr) {
                        ctr[0]++;
                        if (ctr[0] < 6) { // stops right before it would get permanently detached.
                            throw new RuntimeException("Ticker purposeful failure.");
                        }
                    }
                }
            });
            Thread.sleep(251);
            synchronized (ctr) {
                assertTrue(ctr[0] >= 7, "Should not have counted that number: " + ctr[0]);
                ctr[0] = 0;
            }
            Thread.sleep(120);
            synchronized (ctr) {
                assertTrue(ctr[0] > 0, "Should have counted some!");
            }
        } finally {
            t.terminate();
        }

        t = new Ticker(19);
        try {
            final int[] ctr = new int[1];
            Logger.info("The following ticker main loop errors are purposeful.");
            t.send(new EventOutput() {
                public void event() {
                    synchronized (ctr) {
                        if (ctr[0]++ == 3) {
                            throw new RuntimeException("Ticker purposeful failure.");
                        }
                    }
                }
            });
            Thread.sleep(249);
            synchronized (ctr) {
                assertTrue(ctr[0] >= 8, "Should have continued to count: " + ctr[0]);
            }
        } finally {
            t.terminate();
        }
    }
}
