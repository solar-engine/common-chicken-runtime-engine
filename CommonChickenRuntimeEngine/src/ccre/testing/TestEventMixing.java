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

import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;

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
        testCombine_in_in();
        testDebounce();
        testDebounceRecovery();
        testFilter(false);
        testFilter(true);
    }

    private void testFilter(boolean testInputs) throws TestingException {
        final BooleanStatus allow = new BooleanStatus();
        EventStatus trig = new EventStatus();
        final int[] count = new int[3];
        final boolean[] flag = new boolean[] { false };
        EventOutput ev1 = new EventOutput() {
            public void event() {
                if (!allow.get()) {
                    throw new RuntimeException("Not allowed!");
                }
                count[0]++;
            }
        };
        EventOutput ev2 = new EventOutput() {
            public void event() {
                if (!allow.get()) {
                    throw new RuntimeException("Not allowed!");
                }
                count[1]++;
            }

            public boolean eventWithRecovery() {
                if (!allow.get()) {
                    throw new RuntimeException("Not allowed!");
                }
                count[2]++;
                return flag[0];
            }
        };
        if (testInputs) {
            trig.and(allow).send(ev1);
            trig.and(allow).send(ev2);
        } else {
            trig.send(ev1.filter(allow));
            trig.send(ev2.filter(allow));
        }
        int mirror0 = 0, mirror1 = 0, mirror2 = 0;
        for (boolean b : new boolean[] { false, false, true, true, false, true, false, true, true, true, false, false, false, true, false, true, true, false, true, false, false, true }) {
            allow.set(b);

            trig.event();
            if (b) {
                mirror0++;
                mirror1++;
            }
            assertIntsEqual(count[0], mirror0, "Mirror 0 mismatch!");
            assertIntsEqual(count[1], mirror1, "Mirror 1 mismatch!");
            assertIntsEqual(count[2], mirror2, "Mirror 2 mismatch!");

            flag[0] = (mirror0 & 1) == 0;

            boolean ret = trig.eventWithRecovery();
            if (b) {
                assertTrue(ret == flag[0], "Flag propagation mismatch!");
                mirror0++;
                mirror2++;
            } else {
                assertFalse(ret, "Flag non-propagation mismatch!");
            }
            assertIntsEqual(count[0], mirror0, "Mirror 0 mismatch!");
            assertIntsEqual(count[1], mirror1, "Mirror 1 mismatch!");
            assertIntsEqual(count[2], mirror2, "Mirror 2 mismatch!");
        }
    }

    private void testDebounceRecovery() throws TestingException, InterruptedException {
        final int[] count = new int[3];
        final boolean[] flag = new boolean[1]; // for checking propagation
        EventOutput o1 = new EventOutput() {
            public void event() {
                count[0]++;
            }
        };
        EventOutput o2 = new EventOutput() {
            public void event() {
                count[1]++;
            }

            public boolean eventWithRecovery() {
                count[2]++;
                return flag[0];
            }
        };
        EventOutput db1 = new EventStatus(o1.debounce(10));
        EventOutput db2 = new EventStatus(o2.debounce(10));
        assertIntsEqual(count[0], 0, "bad count");
        assertIntsEqual(count[1], 0, "bad count");
        assertIntsEqual(count[2], 0, "bad count");
        Thread.sleep(30);
        assertIntsEqual(count[0], 0, "bad count");
        assertIntsEqual(count[1], 0, "bad count");
        assertIntsEqual(count[2], 0, "bad count");
        db1.event();
        db2.event();
        assertIntsEqual(count[0], 1, "bad count");
        assertIntsEqual(count[1], 1, "bad count");
        assertIntsEqual(count[2], 0, "bad count");
        flag[0] = true;
        assertFalse(db1.eventWithRecovery(), "bad recovery condition");
        assertFalse(db2.eventWithRecovery(), "bad recovery condition");
        assertIntsEqual(count[0], 1, "bad count");
        assertIntsEqual(count[1], 1, "bad count");
        assertIntsEqual(count[2], 0, "bad count");
        Thread.sleep(30);
        assertIntsEqual(count[0], 1, "bad count");
        assertIntsEqual(count[1], 1, "bad count");
        assertIntsEqual(count[2], 0, "bad count");
        assertFalse(db1.eventWithRecovery(), "bad recovery condition");
        assertTrue(db2.eventWithRecovery(), "bad recovery condition");
        assertFalse(db2.eventWithRecovery(), "bad recovery condition");
        assertIntsEqual(count[0], 2, "bad count");
        assertIntsEqual(count[1], 1, "bad count");
        assertIntsEqual(count[2], 1, "bad count");
        Thread.sleep(30);
        assertIntsEqual(count[0], 2, "bad count");
        assertIntsEqual(count[1], 1, "bad count");
        assertIntsEqual(count[2], 1, "bad count");
        flag[0] = false;
        assertFalse(db1.eventWithRecovery(), "bad recovery condition");
        assertFalse(db2.eventWithRecovery(), "bad recovery condition");
        assertIntsEqual(count[0], 3, "bad count");
        assertIntsEqual(count[1], 1, "bad count");
        assertIntsEqual(count[2], 2, "bad count");
    }

    private void testDebounce() throws TestingException, InterruptedException {
        final int[] count = new int[] { 0 };
        EventOutput o = new EventOutput() {
            public void event() {
                count[0]++;
            }
        };
        EventStatus input = new EventStatus();
        input.debounced(10).send(o);

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

    private void testCombine_in_in() throws TestingException {
        EventStatus triggerA = new EventStatus(), triggerB = new EventStatus();
        EventInput combined = triggerA.or(triggerB);
        BooleanStatus alpha = new BooleanStatus();

        EventOutput unbind = combined.sendR(alpha.getSetTrueEvent());

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

        unbind.event();

        assertFalse(alpha.get(), "Should not have happened.");
        triggerA.event();
        assertFalse(alpha.get(), "Should not have happened.");
        triggerA.event();
        assertFalse(alpha.get(), "Should not have happened.");
        triggerB.event();
        assertFalse(alpha.get(), "Should not have happened.");
        triggerB.event();
        assertFalse(alpha.get(), "Should not have happened.");

        combined.send(alpha.getSetTrueEvent());

        assertFalse(alpha.get(), "Should not have happened.");
        triggerA.event();
        assertTrue(alpha.get(), "Should have happened.");
        triggerB.event();
        assertTrue(alpha.get(), "Should have happened.");
    }

    private void testCombine_out_out() throws TestingException {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus();
        EventOutput combined = alpha.getSetTrueEvent().combine(beta.getSetTrueEvent());
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
        EventOutput unbind = EventInput.never.sendR(neverOut);
        Thread.sleep(500);
        unbind.event();
    }

    private void testIgnored() {
        EventOutput.ignored.event(); // nothing should happen... but we don't have much of a way to check that.
    }
}
