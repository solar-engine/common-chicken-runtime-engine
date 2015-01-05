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
package ccre.instinct;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.FloatMixing;
import ccre.log.Logger;

/**
 * The base class for the different kinds of user-extendable Instinct modules.
 *
 * @author skeggsc
 */
public abstract class InstinctBaseModule {

    /**
     * Wait until the specified BooleanInputPoll becomes true before returning.
     *
     * @param waitFor The condition to wait until.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntil(BooleanInputPoll waitFor) throws AutonomousModeOverException, InterruptedException {
        while (true) {
            ensureShouldBeRunning();
            if (waitFor.get()) {
                return;
            }
            waitCycle();
        }
    }

    /**
     * Wait until the specified EventInput is produced before returning.
     *
     * @param source The event to wait for.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitForEvent(EventInput source) throws AutonomousModeOverException, InterruptedException {
        final boolean[] b = new boolean[1];
        EventOutput c = new EventOutput() {
            public void event() {
                b[0] = true;
                notifyCycle();
            }
        };
        source.send(c);
        try {
            while (!b[0]) {
                ensureShouldBeRunning();
                waitCycle();
            }
        } finally {
            source.unsend(c);
        }
    }

    /**
     * Wait for one of the specified conditions to become true before returning.
     *
     * @param waitFor The conditions to check.
     * @return The index of the first condition that became true.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected int waitUntilOneOf(BooleanInputPoll... waitFor) throws AutonomousModeOverException, InterruptedException {
        while (true) {
            ensureShouldBeRunning();
            for (int i = 0; i < waitFor.length; i++) {
                if (waitFor[i].get()) {
                    return i;
                }
            }
            waitCycle();
        }
    }

    /**
     * Wait until the specified FloatInputPoll reaches or rises above the
     * specified minimum.
     *
     * @param waitFor The value to monitor.
     * @param minimum The threshold to wait for the value to reach.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntilAtLeast(FloatInputPoll waitFor, float minimum) throws AutonomousModeOverException, InterruptedException {
        waitUntil(FloatMixing.floatIsAtLeast(waitFor, minimum));
    }

    /**
     * Wait until the specified FloatInputPoll reaches or falls below the
     * specified maximum.
     *
     * @param waitFor The value to monitor.
     * @param maximum The threshold to wait for the value to reach.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntilAtMost(FloatInputPoll waitFor, float maximum) throws AutonomousModeOverException, InterruptedException {
        waitUntil(FloatMixing.floatIsAtMost(waitFor, maximum));
    }

    /**
     * Wait for the specified amount of time.
     *
     * @param milliseconds The amount of time to wait for, in milliseconds.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitForTime(long milliseconds) throws InterruptedException, AutonomousModeOverException {
        if (milliseconds < 0) {
            Logger.warning("Negative wait in Instinct: " + milliseconds);
            return;
        } else if (milliseconds == 0) {
            return; // Do nothing.
        }
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            ensureShouldBeRunning();
            throw ex;
        }
    }

    /**
     * Wait for the specified amount of time, fetched from a FloatInputPoll
     * specified in seconds.
     *
     * @param seconds The amount of time to wait for, in seconds.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitForTime(FloatInputPoll seconds) throws InterruptedException, AutonomousModeOverException {
        waitForTime((long) (1000 * seconds.get() + 0.5f));
    }

    /**
     * The location for the main code of this InstinctModule.
     *
     * @throws AutonomousModeOverException Propagate this up here when thrown by
     * any waiting method.
     * @throws InterruptedException Propagate this up here when thrown by any
     * waiting method.
     */
    protected abstract void autonomousMain() throws AutonomousModeOverException, InterruptedException;

    /**
     * Wait until the next time that this module is updated.
     */
    abstract void waitCycle() throws InterruptedException;

    /**
     * Cause a fake cycle - resume any threads waiting in waitCycle().
     */
    abstract void notifyCycle();
    
    /**
     * Make sure that the autonomous mode should still be running.
     * 
     * @throws AutonomousModeOverException if the autonomous mode shouldn't be
     * running.
     */
    abstract void ensureShouldBeRunning() throws AutonomousModeOverException;
}