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
package ccre.testing;

import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.ctrl.PauseTimer;
import ccre.log.Logger;

/**
 * Tests the PauseTimer class.
 * 
 * @author skeggsc
 */
public class TestPauseTimer extends BaseTest {

    @Override
    public String getName() {
        return "PauseTimer Test";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        testNormal();
        testAlternate();
    }

    private void testNormal() throws TestingException, InterruptedException {
        PauseTimer timer = new PauseTimer(499);
        try {
            BooleanStatus on1 = new BooleanStatus(), on2 = new BooleanStatus(), start = new BooleanStatus(), stop = new BooleanStatus();
            timer.send(on1);
            timer.triggerAtChanges(on2.getSetTrueEvent(), on2.getSetFalseEvent());
            timer.triggerAtStart(start.getSetTrueEvent());
            timer.triggerAtEnd(stop.getSetTrueEvent());
            assertFalse(timer.get(), "Should start off.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertFalse(stop.get(), "Should not have stopped.");
            timer.event();
            assertTrue(timer.get(), "Should become on.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertTrue(start.get(), "Should have started.");
            start.set(false);
            assertFalse(stop.get(), "Should not have stopped.");
            Thread.sleep(399);
            assertTrue(timer.get(), "Should still be on.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertFalse(stop.get(), "Should not have stopped.");
            Thread.sleep(199);
            assertFalse(timer.get(), "Should become off.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertTrue(stop.get(), "Should have stopped.");
            stop.set(false);

            assertFalse(timer.get(), "Should start off.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertFalse(stop.get(), "Should not have stopped.");
            timer.event();
            assertTrue(timer.get(), "Should become on.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertTrue(start.get(), "Should have started.");
            start.set(false);
            assertFalse(stop.get(), "Should not have stopped.");
            Thread.sleep(399);
            assertTrue(timer.get(), "Should still be on.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertFalse(stop.get(), "Should not have stopped.");
            timer.event();
            assertTrue(timer.get(), "Should still be on.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertFalse(stop.get(), "Should not have stopped.");
            Thread.sleep(399);
            assertTrue(timer.get(), "Should still be on.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertFalse(stop.get(), "Should not have stopped.");
            Thread.sleep(199);
            assertFalse(timer.get(), "Should become off.");
            assertTrue(timer.get() == on1.get(), "Should be equal!");
            assertTrue(timer.get() == on2.get(), "Should be equal!");
            assertFalse(start.get(), "Should not have started.");
            assertTrue(stop.get(), "Should have stopped.");
            stop.set(false);
        } finally {
            timer.terminate();
        }
    }

    private void testAlternate() throws TestingException, InterruptedException {
        PauseTimer timer = new PauseTimer(299);
        try {
            Logger.info("The following failure is purposeful.");
            BooleanOutput evil = new BooleanOutput() {
                public void set(boolean value) {
                    if (!value) {
                        throw new RuntimeException("Purposeful failure.");
                    }
                }
            };
            timer.send(evil);
            final boolean[] atAll = new boolean[1];
            timer.send(new BooleanOutput() {
                public void set(boolean value) {
                    if (!value) {
                        atAll[0] = true;
                    }
                }
            });
            timer.event();
            assertTrue(timer.get(), "Should be on.");
            Thread.sleep(399);
            assertFalse(timer.get(), "Should be off.");
            assertFalse(atAll[0], "Should not have occurred.");
            // TODO: Should test logging as well.
            timer.unsend(evil);

            // Now use it semi-normally.
            BooleanStatus temp = new BooleanStatus();
            timer.send(temp);
            assertFalse(temp.get(), "Should be off.");
            timer.event();
            Thread.sleep(199);
            assertTrue(temp.get(), "Should be on.");
            timer.unsend(temp);
            assertTrue(temp.get(), "Should be on.");
            Thread.sleep(149);
            assertTrue(temp.get(), "Should be on.");
            assertFalse(timer.get(), "Should be off.");
        } finally {
            timer.terminate();
        }
    }
}
