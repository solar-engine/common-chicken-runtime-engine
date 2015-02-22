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

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatStatus;
import ccre.ctrl.EventMixing;
import ccre.ctrl.StateMachine;
import ccre.log.LogLevel;

/**
 * Tests the StateMachine class.
 *
 * @author skeggsc
 */
public class TestStateMachine extends BaseTest {

    @Override
    public String getName() {
        return "StateMachine tests";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        testDoor();
        testBadMachine();
        testTurnstile();
        testBadUsage();
        testGandalf();
    }

    private void testDoor() throws TestingException {
        // Setup door
        StateMachine door = new StateMachine(0, "CLOSED", "OPEN", "EXPLODED");

        door.autologTransitions(LogLevel.FINEST, "Subtest check: ");

        BooleanInputPoll isOpen = door.getIsState("OPEN");
        EventStatus doOpen = new EventStatus(), doClose = new EventStatus();
        door.setStateWhen("OPEN", doOpen);
        door.setStateWhen("CLOSED", doClose);

        assertFalse(isOpen.get(), "Door should be closed!");
        doClose.event();
        assertFalse(isOpen.get(), "Door should be closed!");
        doOpen.event();
        assertTrue(isOpen.get(), "Door should be open!");
        doOpen.event();
        assertTrue(isOpen.get(), "Door should be open!");
        doClose.event();
        assertFalse(isOpen.get(), "Door should be closed!");
        doOpen.event();
        assertTrue(isOpen.get(), "Door should be open!");
        doClose.event();
        assertFalse(isOpen.get(), "Door should be closed!");
        doOpen.event();
        assertTrue(isOpen.get(), "Door should be open!");

        EventOutput txv = door.getStateTransitionEvent("CLOSED", "EXPLODED");

        BooleanStatus hasExplodedEver = new BooleanStatus();
        BooleanStatus hasNotUnexplodedEver = new BooleanStatus(true);
        FloatStatus timeDilationConstant = new FloatStatus(1.0f);

        door.setOnEnterState("EXPLODED", hasExplodedEver, true);
        door.setOnExitState("EXPLODED", hasNotUnexplodedEver, false);
        door.setOnEnterState("EXPLODED", timeDilationConstant, 0.1f);
        door.setOnExitState("EXPLODED", timeDilationConstant, 1.0f);

        assertFalse(door.isState("EXPLODED"), "Should not have exploded!");
        assertFalse(hasExplodedEver.get(), "Never exploded!");
        assertTrue(hasNotUnexplodedEver.get(), "Never unexploded!");
        assertTrue(timeDilationConstant.get() == 1.0f, "Time at normal speed!");
        txv.event();
        assertFalse(door.isState("EXPLODED"), "Should not have exploded!");
        assertFalse(hasExplodedEver.get(), "Never exploded!");
        assertTrue(hasNotUnexplodedEver.get(), "Never unexploded!");
        assertTrue(timeDilationConstant.get() == 1.0f, "Time at normal speed!");
        doClose.event();
        assertFalse(door.isState("EXPLODED"), "Should not have exploded!");
        assertFalse(hasExplodedEver.get(), "Never exploded!");
        assertTrue(hasNotUnexplodedEver.get(), "Never unexploded!");
        assertTrue(timeDilationConstant.get() == 1.0f, "Time at normal speed!");
        txv.event();
        assertTrue(door.isState("EXPLODED"), "Should have exploded!");
        assertTrue(hasExplodedEver.get(), "Has exploded!");
        assertTrue(hasNotUnexplodedEver.get(), "Never unexploded!");
        assertTrue(timeDilationConstant.get() == 0.1f, "Time in slow-mo!");
        doOpen.event();
        assertFalse(door.isState("EXPLODED"), "Should have unexploded!");
        assertTrue(hasExplodedEver.get(), "Has exploded!");
        assertFalse(hasNotUnexplodedEver.get(), "Has unexploded!");
        assertTrue(timeDilationConstant.get() == 1.0f, "Time at normal speed!");
    }

    private void testBadMachine() throws TestingException {
        boolean exception = false;
        try {
            new StateMachine(0, "BAD", "BAD");
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "StateMachine should have rejected duplicate arguments!");

        exception = false;
        try {
            new StateMachine(0, "SOMETHING", "ODD", "BAD", "GOOD", "NICE", "MEAN", "ANNOYING", "BAD");
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "StateMachine should have rejected duplicate arguments!");

        exception = false;
        try {
            new StateMachine("MISSING", "SOMETHING", "ODD", "GOOD", "NICE", "MEAN", "ANNOYING", "BAD");
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "StateMachine should have rejected bad initial state!");

        exception = false;
        try {
            new StateMachine(-1, "SOMETHING", "ODD", "GOOD", "NICE", "MEAN", "ANNOYING", "BAD");
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "StateMachine should have rejected bad initial state!");

        exception = false;
        try {
            new StateMachine(7, "SOMETHING", "ODD", "GOOD", "NICE", "MEAN", "ANNOYING", "BAD");
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "StateMachine should have rejected bad initial state!");

        exception = false;
        try {
            new StateMachine(0, "SOMETHING", "ODD", "GOOD", null, "MEAN", "ANNOYING", "BAD");
        } catch (NullPointerException ex) {
            exception = true;
        }
        assertTrue(exception, "StateMachine should have rejected null state name!");
    }

    private void testTurnstile() throws TestingException {
        // Setup turnstile
        StateMachine turnstile = new StateMachine("LOCKED", "LOCKED", "UNLOCKED");

        EventStatus insertCoin = new EventStatus();
        EventStatus pushThrough = new EventStatus();
        BooleanStatus isLocked = new BooleanStatus(true);
        BooleanStatus gotThrough = new BooleanStatus();

        gotThrough.setTrueWhen(EventMixing.filterEvent(isLocked, false, (EventInput) pushThrough));
        turnstile.transitionStateWhen("LOCKED", "UNLOCKED", insertCoin);
        turnstile.transitionStateWhen("UNLOCKED", "LOCKED", pushThrough);
        turnstile.setFalseOnEnterState("UNLOCKED", isLocked);
        turnstile.setTrueOnExitState("UNLOCKED", isLocked);

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

        assertTrue(isLocked.get(), "Turnstile should start locked!");
        assertFalse(gotThrough.get(), "Human should not have entered!");
        pushThrough.event();
        assertTrue(isLocked.get(), "Turnstile should remain locked!");
        assertFalse(gotThrough.get(), "Human should not have entered!");
        pushThrough.event();
        assertTrue(isLocked.get(), "Turnstile should remain locked!");
        assertFalse(gotThrough.get(), "Human should not have entered!");

        insertCoin.event();
        assertFalse(isLocked.get(), "Turnstile should have unlocked!");
        assertFalse(gotThrough.get(), "Human should not have entered!");
        insertCoin.event();
        assertFalse(isLocked.get(), "Turnstile should have unlocked!");
        assertFalse(gotThrough.get(), "Human should not have entered!");

        pushThrough.event();
        assertTrue(isLocked.get(), "Turnstile should have locked!");
        assertTrue(gotThrough.get(), "Human should have entered!");
        gotThrough.set(false);

        pushThrough.event();
        assertTrue(isLocked.get(), "Turnstile should remain locked!");
        assertFalse(gotThrough.get(), "Human should not have entered!");

        insertCoin.event();
        insertCoin.event();
        insertCoin.event();
        assertFalse(isLocked.get(), "Turnstile should have unlocked!");
        assertFalse(gotThrough.get(), "Human should not have entered!");

        pushThrough.event();
        assertTrue(isLocked.get(), "Turnstile should have locked!");
        assertTrue(gotThrough.get(), "Human should have entered!");

        turnstile.setState("UNLOCKED");
        assertFalse(isLocked.get(), "Turnstile should have unlocked!");
        turnstile.setState(1);
        assertFalse(isLocked.get(), "Turnstile should have unlocked!");
        turnstile.setState("LOCKED");
        assertTrue(isLocked.get(), "Turnstile should have locked!");
        turnstile.setState("UNLOCKED");
        assertFalse(isLocked.get(), "Turnstile should have unlocked!");
        turnstile.setState(0);
        assertTrue(isLocked.get(), "Turnstile should have locked!");
        turnstile.setState("LOCKED");
        assertTrue(isLocked.get(), "Turnstile should have locked!");
        turnstile.setState("UNLOCKED");
        assertFalse(isLocked.get(), "Turnstile should have unlocked!");

        boolean exception = false;
        try {
            turnstile.setState("NONEXISTENT");
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Incorrect state name should have errored.");
        try {
            turnstile.setState(-1);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Invalid state index should have errored.");
        try {
            turnstile.setState(2);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Invalid state index should have errored.");

        assertIntsEqual(totalEntriesExits[0], 4, "Wrong total number of LOCKED entrances!");
        assertIntsEqual(totalEntriesExits[1], 5, "Wrong total number of UNLOCKED entrances!");
        assertIntsEqual(totalEntriesExits[2], 5, "Wrong total number of LOCKED exits!");
        assertIntsEqual(totalEntriesExits[3], 4, "Wrong total number of UNLOCKED exits!");
    }

    private void testBadUsage() throws TestingException {
        StateMachine machine = new StateMachine(3, "SOMETHING", "ODD", "GOOD", "NICE", "MEAN", "ANNOYING", "BAD");
        boolean exception = false;
        try {
            machine.getIsState(-1);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Invalid index should have caused getIsState to fail.");

        assertTrue(machine.getIsState(3).get(), "Expected default to be respected.");

        machine.getStateSetEvent("ODD").event();

        assertFalse(machine.getIsState(3).get(), "Expected state to be changed.");

        assertTrue(machine.getIsState("ODD").get(), "Expected state to be changed.");

        exception = false;
        try {
            machine.getIsState(7);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Invalid index should have caused getIsState to fail.");

        exception = false;
        try {
            machine.getStateSetEvent("MISSING");
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Expected invalid state to cause an error.");

        exception = false;
        try {
            machine.getStateSetEvent(-1);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Expected invalid state to cause an error.");

        exception = false;
        try {
            machine.getStateSetEvent(7);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Expected invalid state to cause an error.");

        exception = false;
        try {
            machine.getStateName(-1);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Expected invalid state to cause an error.");

        exception = false;
        try {
            machine.getStateName(7);
        } catch (IllegalArgumentException ex) {
            exception = true;
        }
        assertTrue(exception, "Expected invalid state to cause an error.");
    }

    private void testGandalf() throws TestingException {
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

        gandalf.getIsStateDyn("DEAD").send(died);
        gandalf.onEnterState("MIA", missing.getSetTrueEvent());
        gandalf.onExitState("MIA", missing.getSetFalseEvent());
        gandalf.setTrueOnEnterState("ALIVE", alive);
        gandalf.setFalseOnExitState("ALIVE", alive);

        assertObjectEqual(gandalf.getStateName(0), "ALIVE", "State 0 should be alive.");
        assertObjectEqual(gandalf.getStateName(1), "MIA", "State 1 should be missing.");
        assertObjectEqual(gandalf.getStateName(2), "DEAD", "State 2 should be dead.");

        assertFalse(died.get(), "Gandalf should not be dead.");
        assertFalse(missing.get(), "Gandalf should not be missing.");
        assertFalse(alive.get(), "Gandalf should not be \"alive\" yet.");
        gandalf.setState(0); // try to retrigger events... but already in that state.
        assertFalse(died.get(), "Gandalf should not be dead.");
        assertFalse(missing.get(), "Gandalf should not be missing.");
        assertFalse(alive.get(), "Gandalf should not be \"alive\" yet.");

        // actually retrigger events
        gandalf.setState(1);
        gandalf.setState(0);

        assertFalse(died.get(), "Gandalf should not be dead.");
        assertFalse(missing.get(), "Gandalf should not be missing.");
        assertTrue(alive.get(), "Gandalf should be alive.");
        gandalf.setState(gandalf.getState() + 1);
        assertFalse(died.get(), "Gandalf should not be dead.");
        assertTrue(missing.get(), "Gandalf should be missing.");
        assertFalse(alive.get(), "Gandalf should not be alive.");
        gandalf.setState(gandalf.getState() + 1);
        assertTrue(died.get(), "Gandalf should be dead.");
        assertFalse(missing.get(), "Gandalf should not be missing.");
        assertFalse(alive.get(), "Gandalf should not be alive.");

        assertIntsEqual(changes[0], 4, "Expected five enterances.");
        assertIntsEqual(changes[1], 4, "Expected five exits.");
        assertIntsEqual(changes[2], 4, "Expected five enterances.");
        assertIntsEqual(changes[3], 4, "Expected five exits.");
    }
}
