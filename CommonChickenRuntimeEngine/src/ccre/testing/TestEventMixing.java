/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.ctrl.EventMixing;

/**
 * Tests the EventMixing class.
 *
 * @author skeggsc
 */
public class TestEventMixing extends BaseTest {

    @Override
    public String getName() {
        return "EventMixing Test";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        testIgnored();
        testNever();
        testCombine_out_out();
        testCombine_poly_out();
        testCombine_in_in();
        testCombine_poly_in();
        testDebounce();
    }

    private void testDebounce() throws TestingException, InterruptedException {
        final int[] count = new int[] { 0 };
        EventOutput o = new EventOutput() {
            public void event() {
                count[0]++;
            }
        };
        EventStatus input = new EventStatus();
        EventMixing.debounce((EventInput) input, 10).send(o);
        
        assertTrue(count[0] == 0, "Bad counter");
        input.event();
        assertTrue(count[0] == 1, "Bad counter");
        input.event();
        assertTrue(count[0] == 1, "Bad counter");
        input.event();
        assertTrue(count[0] == 1, "Bad counter");
        Thread.sleep(30);
        assertTrue(count[0] == 1, "Bad counter");
        input.event();
        assertTrue(count[0] == 2, "Bad counter");
        input.event();
        assertTrue(count[0] == 2, "Bad counter");
        input.event();
        assertTrue(count[0] == 2, "Bad counter");
        Thread.sleep(30);
        assertTrue(count[0] == 2, "Bad counter");
        input.event();
        assertTrue(count[0] == 3, "Bad counter");
        input.event();
        assertTrue(count[0] == 3, "Bad counter");
        input.event();
        assertTrue(count[0] == 3, "Bad counter");
    }

    private void testCombine_poly_in() throws TestingException {
        EventStatus triggerA = new EventStatus(), triggerB = new EventStatus(), triggerC = new EventStatus();
        EventInput combined = EventMixing.combine((EventInput) triggerA, (EventInput) triggerB, (EventInput) triggerC);
        BooleanStatus alpha = new BooleanStatus();
        alpha.setTrueWhen(combined);
        assertFalse(alpha.get(), "Should not have happened.");
        triggerA.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerA.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerB.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerB.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerC.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerC.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerA.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerB.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerA.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        combined.unsend(alpha.getSetTrueEvent());
        assertFalse(alpha.get(), "Should not have happened.");
        triggerA.event();
        triggerB.event();
        triggerC.event();
        assertFalse(alpha.get(), "Should not have happened.");

        EventMixing.combine(new EventInput[0]).send(new EventOutput() {
            public void event() {
                throw new RuntimeException("Should not occur!");
            }
        });
    }

    private void testCombine_in_in() throws TestingException {
        EventStatus triggerA = new EventStatus(), triggerB = new EventStatus();
        EventInput combined = EventMixing.combine((EventInput) triggerA, (EventInput) triggerB);
        BooleanStatus alpha = new BooleanStatus();
        alpha.setTrueWhen(combined);
        assertFalse(alpha.get(), "Should not have happened.");
        triggerA.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerA.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerB.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
        triggerB.event();
        assertTrue(alpha.get(), "Should have happened.");
        alpha.set(false);
    }

    private void testCombine_poly_out() throws TestingException {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus(), gamma = new BooleanStatus();
        EventOutput combined = EventMixing.combine(alpha.getSetTrueEvent(), beta.getSetTrueEvent(), gamma.getSetTrueEvent());
        assertFalse(alpha.get(), "Should be false.");
        assertFalse(beta.get(), "Should be false.");
        assertFalse(gamma.get(), "Should be false.");
        combined.event();
        assertTrue(alpha.get(), "Should be true.");
        assertTrue(beta.get(), "Should be true.");
        assertTrue(gamma.get(), "Should be true.");
        alpha.set(false);
        beta.set(false);
        gamma.set(false);
        assertFalse(alpha.get(), "Should be false.");
        assertFalse(beta.get(), "Should be false.");
        assertFalse(gamma.get(), "Should be false.");
        combined.event();
        assertTrue(alpha.get(), "Should be true.");
        assertTrue(beta.get(), "Should be true.");
        assertTrue(gamma.get(), "Should be true.");
        alpha.set(false);
        beta.set(false);
        gamma.set(false);
        assertFalse(alpha.get(), "Should be false.");
        assertFalse(beta.get(), "Should be false.");
        assertFalse(gamma.get(), "Should be false.");
        EventMixing.combine(new EventOutput[0]).event();
    }

    private void testCombine_out_out() throws TestingException {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus();
        EventOutput combined = EventMixing.combine(alpha.getSetTrueEvent(), beta.getSetTrueEvent());
        assertFalse(alpha.get(), "Should be false.");
        assertFalse(beta.get(), "Should be false.");
        combined.event();
        assertTrue(alpha.get(), "Should be true.");
        assertTrue(beta.get(), "Should be true.");
        alpha.set(false);
        beta.set(false);
        assertFalse(alpha.get(), "Should be false.");
        assertFalse(beta.get(), "Should be false.");
        combined.event();
        assertTrue(alpha.get(), "Should be true.");
        assertTrue(beta.get(), "Should be true.");
    }

    private void testNever() throws InterruptedException {
        EventOutput neverOut = new EventOutput() {
            public void event() {
                throw new RuntimeException("Should not have happened!");
            }
        };
        EventMixing.never.send(neverOut);
        Thread.sleep(500);
        EventMixing.never.unsend(neverOut);
    }

    private void testIgnored() {
        EventMixing.ignored.event(); // nothing should happen... but we don't have much of a way to check that.
    }
}
