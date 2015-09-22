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
package ccre.ctrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatStatus;
import ccre.testing.CountingEventOutput;

public class StateMachineTest {

    private StateMachine machine;

    private static final String NOT_A_NAME = "BadState";
    private static final String[] names = new String[] { "Open", "Closed", "Exploded", "Locked" };

    @Before
    public void setUp() throws Exception {
        machine = new StateMachine(0, names);
    }

    @After
    public void tearDown() throws Exception {
        machine = null;
    }

    @Test
    public void testStateMachineConstructor() {
        for (int i = 0; i < names.length; i++) {
            assertEquals(new StateMachine(names[i], names).getState(), i);
            assertEquals(new StateMachine(i, names).getState(), i);
            assertEquals(new StateMachine(names[i], names).getStateName(), names[i]);
            assertEquals(new StateMachine(i, names).getStateName(), names[i]);
        }
    }

    // A bunch of tests that just check constructor edge cases

    @Test(expected = NullPointerException.class)
    public void testStateMachineStringNullStringArray() {
        new StateMachine("Test", (String[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testStateMachineIntNullStringArray() {
        new StateMachine(0, (String[]) null);
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineStringEmptyStringArray() {
        new StateMachine("Test", new String[0]);
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineIntEmptyStringArray() {
        new StateMachine(0, new String[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testStateMachineStringNull() {
        new StateMachine("Test", new String[] { "Test", null });
    }

    @Test(expected = NullPointerException.class)
    public void testStateMachineIntNull() {
        new StateMachine(0, new String[] { "Real", null });
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineNotIncluded() {
        new StateMachine("Test", new String[] { "Not This", "Nope", "Not This Either" });
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineNullName() {
        new StateMachine(null, new String[] { "Not This", "Nope", "Not This Either" });
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineOutOfRangePositive() {
        new StateMachine(3, new String[] { "Exists", "Also exists", "Next one won't!" });
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineOutOfRangeNegative() {
        new StateMachine(-1, new String[] { "Exists", "Also exists", "Next one won't!" });
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineDuplicateString() {
        new StateMachine("Test", new String[] { "Test", "Alpha", "Not a duplicate", "Alpha" });
    }

    @Test(expected = RuntimeException.class)
    public void testStateMachineDuplicateInt() {
        new StateMachine(0, new String[] { "Test", "Alpha", "Not a duplicate", "Alpha" });
    }

    // Now back to our regularly scheduled testing

    @Test
    public void testGetNumberOfStates() {
        assertEquals(machine.getNumberOfStates(), names.length);
    }

    @Test
    public void testGetState() {
        assertEquals(machine.getState(), 0);
        assertEquals(machine.getStateName(), names[0]);
    }

    @Test
    public void testGetStateNameInt() {
        for (int i = 0; i < names.length; i++) {
            assertEquals(machine.getStateName(i), names[i]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateNameNegative() {
        machine.getStateName(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateNameHigh() {
        machine.getStateName(names.length);
    }

    @Test
    public void testSetStateInt() {
        for (int i = 0; i < names.length * 4; i++) {
            int state = i % names.length;
            machine.setState(state);
            assertEquals(machine.getState(), state);
            assertEquals(machine.getStateName(), names[state]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStateIntNegative() {
        machine.setState(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStateIntHigh() {
        machine.setState(names.length);
    }

    @Test
    public void testSetStateString() {
        for (int i = 0; i < names.length * 4; i++) {
            int state = i % names.length;
            machine.setState(names[state]);
            assertEquals(machine.getState(), state);
            assertEquals(machine.getStateName(), names[state]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStateStringNonexistent() {
        machine.setState(NOT_A_NAME);
    }

    @Test
    public void testSetStateWhenStringEventInput() {
        EventStatus[] events = new EventStatus[names.length];
        for (int i = 0; i < names.length; i++) {
            events[i] = new EventStatus();
            machine.setStateWhen(names[i], events[i]);
        }
        tryStateSetters(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStateWhenStringEventInputNonexistent() {
        machine.setStateWhen(NOT_A_NAME, EventInput.never);
    }

    @Test
    public void testSetStateWhenIntEventInput() {
        EventStatus[] events = new EventStatus[names.length];
        for (int i = 0; i < names.length; i++) {
            events[i] = new EventStatus();
            machine.setStateWhen(i, events[i]);
        }
        tryStateSetters(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStateWhenIntEventInputNegative() {
        machine.setStateWhen(-1, EventInput.never);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStateWhenIntEventInputHigh() {
        machine.setStateWhen(names.length, EventInput.never);
    }

    @Test
    public void testGetStateSetEventString() {
        EventOutput[] events = new EventOutput[names.length];
        for (int i = 0; i < names.length; i++) {
            events[i] = machine.getStateSetEvent(names[i]);
        }
        tryStateSetters(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSetStateEventStringNonexistent() {
        machine.getStateSetEvent(NOT_A_NAME);
    }

    @Test
    public void testGetStateSetEventInt() {
        EventOutput[] events = new EventOutput[names.length];
        for (int i = 0; i < names.length; i++) {
            events[i] = machine.getStateSetEvent(i);
        }
        tryStateSetters(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateSetEventIntNegative() {
        machine.getStateSetEvent(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateSetEventIntHigh() {
        machine.getStateSetEvent(names.length);
    }

    private void tryStateSetters(EventOutput[] events) {
        for (int i = 0; i < names.length * 4; i++) {
            int state = i % names.length;
            events[state].event();
            assertEquals(machine.getState(), state);
            assertEquals(machine.getStateName(), names[state]);
        }
    }

    @Test
    public void testIsState() {
        for (int i = 0; i < names.length; i++) {
            machine.setState(i);
            assertTrue(machine.isState(i));
            assertTrue(machine.isState(names[i]));
            for (int j = 0; j < names.length; j++) {
                if (i != j) {
                    assertFalse(machine.isState(j));
                    assertFalse(machine.isState(names[j]));
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsStateNegative() {
        machine.isState(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsStateHigh() {
        machine.isState(names.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsStateNonexistent() {
        machine.isState(NOT_A_NAME);
    }

    @Test
    public void testGetIsState() {
        BooleanInput[] inputsA = new BooleanInput[names.length];
        BooleanInput[] inputsB = new BooleanInput[names.length];
        for (int i = 0; i < names.length; i++) {
            inputsA[i] = machine.getIsState(i);
            inputsB[i] = machine.getIsState(names[i]);
        }
        for (int i = 0; i < names.length; i++) {
            machine.setState(i);
            assertTrue(inputsA[i].get());
            assertTrue(inputsB[i].get());
            for (int j = 0; j < names.length; j++) {
                if (i != j) {
                    assertFalse(inputsA[j].get());
                    assertFalse(inputsB[j].get());
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIsStateNegative() {
        machine.getIsState(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIsStateHigh() {
        machine.getIsState(names.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIsStateNonexistent() {
        machine.getIsState(NOT_A_NAME);
    }

    @Test
    public void testGetStateTransitionEventStringString() {
        EventOutput[] events = new EventOutput[names.length * names.length];
        for (int i = 0; i < names.length; i++) {
            for (int j = 0; j < names.length; j++) {
                events[i + j * 4] = machine.getStateTransitionEvent(names[i], names[j]);
            }
        }
        tryStateTransitions(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateTransitionEventStringStringNonexistentA() {
        machine.getStateTransitionEvent(NOT_A_NAME, names[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateTransitionEventStringStringNonexistentB() {
        machine.getStateTransitionEvent(names[0], NOT_A_NAME);
    }

    @Test
    public void testGetStateTransitionEventIntInt() {
        EventOutput[] events = new EventOutput[names.length * names.length];
        for (int i = 0; i < names.length; i++) {
            for (int j = 0; j < names.length; j++) {
                events[i + j * 4] = machine.getStateTransitionEvent(i, j);
            }
        }
        tryStateTransitions(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateTransitionEventIntIntNegativeA() {
        machine.getStateTransitionEvent(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateTransitionEventIntIntNegativeB() {
        machine.getStateTransitionEvent(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateTransitionEventIntIntHighA() {
        machine.getStateTransitionEvent(names.length, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStateTransitionEventIntIntHighB() {
        machine.getStateTransitionEvent(0, names.length);
    }

    private void tryStateTransitions(EventOutput[] events) {
        for (int state = 0; state < names.length; state++) {
            for (int i = 0; i < names.length; i++) {
                for (int j = 0; j < names.length; j++) {
                    machine.setState(state);
                    events[i + j * 4].event();
                    if (i == state) {
                        assertEquals(machine.getState(), j);
                    } else {
                        assertEquals(machine.getState(), state);
                    }
                }
            }
        }
    }

    @Test
    public void testTransitionStateWhenStringStringEventInput() {
        EventStatus[] events = new EventStatus[names.length * names.length];
        for (int i = 0; i < names.length; i++) {
            for (int j = 0; j < names.length; j++) {
                events[i + j * 4] = new EventStatus();
                machine.transitionStateWhen(names[i], names[j], events[i + j * 4]);
            }
        }
        tryStateTransitions(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransitionStateWhenStringStringEventInputNonexistentA() {
        machine.transitionStateWhen(NOT_A_NAME, names[0], EventInput.never);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransitionStateWhenStringStringEventInputNonexistentB() {
        machine.transitionStateWhen(names[0], NOT_A_NAME, EventInput.never);
    }

    @Test
    public void testTransitionStateWhenIntIntEventInput() {
        EventStatus[] events = new EventStatus[names.length * names.length];
        for (int i = 0; i < names.length; i++) {
            for (int j = 0; j < names.length; j++) {
                events[i + j * 4] = new EventStatus();
                machine.transitionStateWhen(i, j, events[i + j * 4]);
            }
        }
        tryStateTransitions(events);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransitionStateWhenIntIntEventInputNegativeA() {
        machine.transitionStateWhen(-1, 0, EventInput.never);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransitionStateWhenIntIntEventInputNegativeB() {
        machine.transitionStateWhen(0, -1, EventInput.never);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransitionStateWhenIntIntEventInputHighA() {
        machine.transitionStateWhen(names.length, 0, EventInput.never);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransitionStateWhenIntIntEventInputHighB() {
        machine.transitionStateWhen(0, names.length, EventInput.never);
    }

    private int expectedOld = -1, expectedNew = -1;

    @Test
    public void testGetStateExitEnterEvent() {
        EventInput exit = machine.getStateExitEvent();
        EventInput enter = machine.getStateEnterEvent();
        tryStateEnterExit(exit, enter);
    }

    @Test
    public void testOnStateExitEnter() {
        EventStatus exit = new EventStatus(), enter = new EventStatus();
        machine.onStateExit(exit);
        machine.onStateEnter(enter);
        tryStateEnterExit(exit, enter);
    }

    private void tryStateEnterExit(EventInput exit, EventInput enter) {
        CountingEventOutput cexit = new CountingEventOutput();
        CountingEventOutput center = new CountingEventOutput();
        exit.send(cexit);
        exit.send(() -> center.ifExpected = true);// expect enter AFTER exit
        exit.send(() -> assertEquals(expectedOld, machine.getState()));// expect
                                                                       // state
                                                                       // is
                                                                       // still
                                                                       // the
                                                                       // old
                                                                       // state
        enter.send(center);
        enter.send(() -> assertEquals(expectedNew, machine.getState()));// expect
                                                                        // state
                                                                        // is
                                                                        // nstate
        for (int i = 0; i < names.length * 4; i++) {
            int state = (i + 1) % names.length;// +1 is to make sure that we
                                               // don't start with zero

            expectedOld = machine.getState();
            expectedNew = state;

            cexit.ifExpected = true;
            machine.setState(state);
            cexit.check();
            center.check();
            // make sure that it doesn't happen if it's the same state
            machine.setState(state);
        }
    }

    @Test
    public void testOnExitEnterStateString() {
        for (int target = 0; target < names.length; target++) {
            for (int source = 0; source < names.length; source++) {
                if (source == target) {
                    continue;
                }

                // this is hacky, but it works!
                ((EventStatus) machine.getStateEnterEvent()).__UNSAFE_clearListeners();
                ((EventStatus) machine.getStateExitEvent()).__UNSAFE_clearListeners();
                // the point is that otherwise, the CountingEventOutputs will
                // start being annoyed next cycle around

                final int ftarget = target;
                CountingEventOutput center1 = new CountingEventOutput();
                CountingEventOutput center2 = new CountingEventOutput();

                machine.onEnterState(names[target]).send(center1);
                machine.onEnterState(names[target]).send(() -> assertEquals(ftarget, machine.getState()));
                machine.onEnterState(names[target], center2);
                machine.onEnterState(names[target], () -> assertEquals(ftarget, machine.getState()));

                final int fsource = source;

                CountingEventOutput cexit1 = new CountingEventOutput();
                CountingEventOutput cexit2 = new CountingEventOutput();

                machine.onExitState(names[source]).send(cexit1);
                machine.onExitState(names[source]).send(() -> center1.ifExpected = true);
                machine.onExitState(names[source]).send(() -> assertEquals(fsource, machine.getState()));
                machine.onExitState(names[source], cexit2);
                machine.onExitState(names[source], () -> center2.ifExpected = true);
                machine.onExitState(names[source], () -> assertEquals(fsource, machine.getState()));

                machine.setState(source);
                cexit1.ifExpected = cexit2.ifExpected = true;
                machine.setState(target);
                cexit1.check();
                cexit2.check();
                center1.check();
                center2.check();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEnterStateStringNonexistent() {
        machine.onEnterState(NOT_A_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnExitStateStringNonexistent() {
        machine.onExitState(NOT_A_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEnterStateStringEventOutputNonexistent() {
        machine.onEnterState(NOT_A_NAME, EventOutput.ignored);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnExitStateStringEventOutputNonexistent() {
        machine.onExitState(NOT_A_NAME, EventOutput.ignored);
    }

    @Test
    public void testOnExitEnterStateInt() {
        for (int target = 0; target < names.length; target++) {
            for (int source = 0; source < names.length; source++) {
                if (source == target) {
                    continue;
                }

                // this is hacky, but it works!
                ((EventStatus) machine.getStateEnterEvent()).__UNSAFE_clearListeners();
                ((EventStatus) machine.getStateExitEvent()).__UNSAFE_clearListeners();
                // the point is that otherwise, the CountingEventOutputs will
                // start being annoyed next cycle around

                final int ftarget = target;
                CountingEventOutput center1 = new CountingEventOutput();
                CountingEventOutput center2 = new CountingEventOutput();

                machine.onEnterState(target).send(center1);
                machine.onEnterState(target).send(() -> assertEquals(ftarget, machine.getState()));
                machine.onEnterState(target, center2);
                machine.onEnterState(target, () -> assertEquals(ftarget, machine.getState()));

                final int fsource = source;

                CountingEventOutput cexit1 = new CountingEventOutput();
                CountingEventOutput cexit2 = new CountingEventOutput();

                machine.onExitState(source).send(cexit1);
                machine.onExitState(source).send(() -> center1.ifExpected = true);
                machine.onExitState(source).send(() -> assertEquals(fsource, machine.getState()));
                machine.onExitState(source, cexit2);
                machine.onExitState(source, () -> center2.ifExpected = true);
                machine.onExitState(source, () -> assertEquals(fsource, machine.getState()));

                machine.setState(source);
                cexit1.ifExpected = cexit2.ifExpected = true;
                machine.setState(target);
                cexit1.check();
                cexit2.check();
                center1.check();
                center2.check();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEnterStateIntNegative() {
        machine.onEnterState(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEnterStateIntHigh() {
        machine.onEnterState(names.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnExitStateIntNegative() {
        machine.onExitState(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnExitStateIntHigh() {
        machine.onExitState(names.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEnterStateIntEventOutputNegative() {
        machine.onEnterState(-1, EventOutput.ignored);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnEnterStateIntEventOutputHigh() {
        machine.onEnterState(names.length, EventOutput.ignored);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnExitStateIntEventOutputNegative() {
        machine.onExitState(-1, EventOutput.ignored);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnExitStateIntEventOutputHigh() {
        machine.onExitState(names.length, EventOutput.ignored);
    }

    // And now some higher-level tests

    @Test
    public void testDoor() {
        // Setup door
        StateMachine door = new StateMachine(0, "CLOSED", "OPEN", "EXPLODED");

        BooleanInput isOpen = door.getIsState("OPEN");
        EventStatus doOpen = new EventStatus(), doClose = new EventStatus();
        door.setStateWhen("OPEN", doOpen);
        door.setStateWhen("CLOSED", doClose);

        assertFalse(isOpen.get());
        doClose.event();
        assertFalse(isOpen.get());
        doOpen.event();
        assertTrue(isOpen.get());
        doOpen.event();
        assertTrue(isOpen.get());
        doClose.event();
        assertFalse(isOpen.get());
        doOpen.event();
        assertTrue(isOpen.get());
        doClose.event();
        assertFalse(isOpen.get());
        doOpen.event();
        assertTrue(isOpen.get());

        EventOutput txv = door.getStateTransitionEvent("CLOSED", "EXPLODED");

        BooleanStatus hasExplodedEver = new BooleanStatus();
        BooleanStatus hasNotUnexplodedEver = new BooleanStatus(true);
        FloatStatus timeDilationConstant = new FloatStatus(1.0f);

        door.onEnterState("EXPLODED", hasExplodedEver.getSetTrueEvent());
        door.onExitState("EXPLODED", hasNotUnexplodedEver.getSetFalseEvent());
        door.onEnterState("EXPLODED", timeDilationConstant.getSetEvent(0.1f));
        door.onExitState("EXPLODED", timeDilationConstant.getSetEvent(1.0f));

        assertFalse(door.isState("EXPLODED"));
        assertFalse(hasExplodedEver.get());
        assertTrue(hasNotUnexplodedEver.get());
        assertTrue(timeDilationConstant.get() == 1.0f);
        txv.event();
        assertFalse(door.isState("EXPLODED"));
        assertFalse(hasExplodedEver.get());
        assertTrue(hasNotUnexplodedEver.get());
        assertTrue(timeDilationConstant.get() == 1.0f);
        doClose.event();
        assertFalse(door.isState("EXPLODED"));
        assertFalse(hasExplodedEver.get());
        assertTrue(hasNotUnexplodedEver.get());
        assertTrue(timeDilationConstant.get() == 1.0f);
        txv.event();
        assertTrue(door.isState("EXPLODED"));
        assertTrue(hasExplodedEver.get());
        assertTrue(hasNotUnexplodedEver.get());
        assertTrue(timeDilationConstant.get() == 0.1f);
        doOpen.event();
        assertFalse(door.isState("EXPLODED"));
        assertTrue(hasExplodedEver.get());
        assertFalse(hasNotUnexplodedEver.get());
        assertTrue(timeDilationConstant.get() == 1.0f);
    }

    @Test
    public void testTurnstile() {
        // Setup turnstile
        StateMachine turnstile = new StateMachine("LOCKED", "LOCKED", "UNLOCKED");

        EventStatus insertCoin = new EventStatus();
        EventStatus pushThrough = new EventStatus();
        BooleanStatus isLocked = new BooleanStatus(true);
        BooleanStatus gotThrough = new BooleanStatus();

        gotThrough.setTrueWhen(pushThrough.andNot(isLocked));
        turnstile.transitionStateWhen("LOCKED", "UNLOCKED", insertCoin);
        turnstile.transitionStateWhen("UNLOCKED", "LOCKED", pushThrough);
        turnstile.onEnterState("UNLOCKED", isLocked.getSetFalseEvent());
        turnstile.onExitState("UNLOCKED", isLocked.getSetTrueEvent());

        final int[] totalEntriesExits = new int[4];
        turnstile.onEnterState("LOCKED").send(new EventOutput() {
            public void event() {
                totalEntriesExits[0]++;
            }
        });
        turnstile.onEnterState("UNLOCKED").send(new EventOutput() {
            public void event() {
                totalEntriesExits[1]++;
            }
        });
        turnstile.onExitState("LOCKED").send(new EventOutput() {
            public void event() {
                totalEntriesExits[2]++;
            }
        });
        turnstile.onExitState("UNLOCKED").send(new EventOutput() {
            public void event() {
                totalEntriesExits[3]++;
            }
        });

        // Test turnstile

        assertTrue(isLocked.get());
        assertFalse(gotThrough.get());
        pushThrough.event();
        assertTrue(isLocked.get());
        assertFalse(gotThrough.get());
        pushThrough.event();
        assertTrue(isLocked.get());
        assertFalse(gotThrough.get());

        insertCoin.event();
        assertFalse(isLocked.get());
        assertFalse(gotThrough.get());
        insertCoin.event();
        assertFalse(isLocked.get());
        assertFalse(gotThrough.get());

        pushThrough.event();
        assertTrue(isLocked.get());
        assertTrue(gotThrough.get());
        gotThrough.set(false);

        pushThrough.event();
        assertTrue(isLocked.get());
        assertFalse(gotThrough.get());

        insertCoin.event();
        insertCoin.event();
        insertCoin.event();
        assertFalse(isLocked.get());
        assertFalse(gotThrough.get());

        pushThrough.event();
        assertTrue(isLocked.get());
        assertTrue(gotThrough.get());

        turnstile.setState("UNLOCKED");
        assertFalse(isLocked.get());
        turnstile.setState(1);
        assertFalse(isLocked.get());
        turnstile.setState("LOCKED");
        assertTrue(isLocked.get());
        turnstile.setState("UNLOCKED");
        assertFalse(isLocked.get());
        turnstile.setState(0);
        assertTrue(isLocked.get());
        turnstile.setState("LOCKED");
        assertTrue(isLocked.get());
        turnstile.setState("UNLOCKED");
        assertFalse(isLocked.get());

        assertEquals("Wrong total number of LOCKED entrances!", totalEntriesExits[0], 4);
        assertEquals("Wrong total number of UNLOCKED entrances!", totalEntriesExits[1], 5);
        assertEquals("Wrong total number of LOCKED exits!", totalEntriesExits[2], 5);
        assertEquals("Wrong total number of UNLOCKED exits!", totalEntriesExits[3], 4);
    }

    @Test
    public void testGandalf() {
        StateMachine gandalf = new StateMachine(0, "ALIVE", "MIA", "DEAD");

        BooleanStatus died = new BooleanStatus();
        BooleanStatus missing = new BooleanStatus();
        BooleanStatus alive = new BooleanStatus();

        final int[] changes = new int[4];

        gandalf.onStateEnter(new EventOutput() {
            public void event() {
                changes[0]++;
            }
        });
        gandalf.onStateExit(new EventOutput() {
            public void event() {
                changes[1]++;
            }
        });
        gandalf.getStateEnterEvent().send(new EventOutput() {
            public void event() {
                changes[2]++;
            }
        });
        gandalf.getStateExitEvent().send(new EventOutput() {
            public void event() {
                changes[3]++;
            }
        });

        gandalf.getIsState("DEAD").send(died);
        gandalf.onEnterState("MIA", missing.getSetTrueEvent());
        gandalf.onExitState("MIA", missing.getSetFalseEvent());
        gandalf.onEnterState("ALIVE", alive.getSetTrueEvent());
        gandalf.onExitState("ALIVE", alive.getSetFalseEvent());

        assertEquals(gandalf.getStateName(0), "ALIVE");
        assertEquals(gandalf.getStateName(1), "MIA");
        assertEquals(gandalf.getStateName(2), "DEAD");

        assertFalse(died.get());
        assertFalse(missing.get());
        assertFalse(alive.get());
        gandalf.setState(0);// try to retrigger events... but already in that
                            // state.
        assertFalse(died.get());
        assertFalse(missing.get());
        assertFalse(alive.get());

        // actually retrigger events
        gandalf.setState(1);
        gandalf.setState(0);

        assertFalse(died.get());
        assertFalse(missing.get());
        assertTrue(alive.get());
        gandalf.setState(gandalf.getState() + 1);
        assertFalse(died.get());
        assertTrue(missing.get());
        assertFalse(alive.get());
        gandalf.setState(gandalf.getState() + 1);
        assertTrue(died.get());
        assertFalse(missing.get());
        assertFalse(alive.get());

        assertEquals("Expected five enterances", changes[0], 4);
        assertEquals("Expected five exits", changes[1], 4);
        assertEquals("Expected five enterances", changes[2], 4);
        assertEquals("Expected five exits", changes[3], 4);
    }
}
