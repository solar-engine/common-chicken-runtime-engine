/*
 * Copyright 2013-2015 Colby Skeggs
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.time.Time;

/**
 * The base class for the different kinds of user-extendable Instinct modules.
 *
 * @author skeggsc
 */
public abstract class InstinctBaseModule {
    private static Logger logger = LoggerFactory.getLogger(InstinctBaseModule.class);

    /**
     * Wait until the specified BooleanInput becomes true before returning.
     *
     * @param waitFor The condition to wait until.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntil(BooleanInput waitFor) throws AutonomousModeOverException, InterruptedException {
        // TODO: make this dynamic
        while (true) {
            ensureShouldBeRunning();
            if (waitFor.get()) {
                return;
            }
            waitCycle();
        }
    }

    /**
     * Wait until the specified BooleanInput becomes true before returning, or
     * for a timeout to elapse.
     *
     * @param waitFor The condition to wait until.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected boolean waitUntil(long timeout, BooleanInput waitFor) throws AutonomousModeOverException, InterruptedException {
        long doneAt = Time.currentTimeMillis() + timeout;
        // TODO: make this dynamic
        while (Time.currentTimeMillis() < doneAt) {
            ensureShouldBeRunning();
            if (waitFor.get()) {
                return true;
            }
            waitCycle();
        }
        return false;
    }

    /**
     * Wait until the specified BooleanInput becomes false before returning.
     *
     * @param waitFor The condition to wait until false.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntilNot(BooleanInput waitFor) throws AutonomousModeOverException, InterruptedException {
        // TODO: make this dynamic
        while (true) {
            ensureShouldBeRunning();
            if (!waitFor.get()) {
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
                synchronized (b) {
                    b[0] = true;
                    b.notifyAll();
                }
            }
        };
        EventOutput unbind = source.sendR(c);
        try {
            synchronized (b) {
                while (!b[0]) {
                    ensureShouldBeRunning();
                    b.wait();
                }
            }
        } finally {
            unbind.event();
        }
    }

    /**
     * Wait for one of the specified conditions to become true before returning,
     * or for the timeout to elapse.
     *
     * @param waitFor The conditions to check.
     * @return The index of the first condition that became true, or -1 if this
     * method timed out.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected int waitUntilOneOf(long timeout, BooleanInput... waitFor) throws AutonomousModeOverException, InterruptedException {
        long doneAt = Time.currentTimeMillis() + timeout;
        // TODO: make this dynamic
        while (Time.currentTimeMillis() < doneAt) {
            ensureShouldBeRunning();
            for (int i = 0; i < waitFor.length; i++) {
                if (waitFor[i].get()) {
                    return i;
                }
            }
            waitCycle();
        }
        return -1;
    }

    /**
     * Wait for one of the specified conditions to become true before returning.
     *
     * @param waitFor The conditions to check.
     * @return The index of the first condition that became true.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected int waitUntilOneOf(BooleanInput... waitFor) throws AutonomousModeOverException, InterruptedException {
        // TODO: make this dynamic
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
     * Wait until the specified FloatInput reaches or rises above the specified
     * minimum.
     *
     * @param waitFor The value to monitor.
     * @param minimum The threshold to wait for the value to reach.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntilAtLeast(FloatInput waitFor, float minimum) throws AutonomousModeOverException, InterruptedException {
        // TODO: make sure that nothing is accidentally kept around after this
        waitUntil(waitFor.atLeast(minimum));
    }

    /**
     * Wait until the specified FloatInput reaches or falls below the specified
     * maximum.
     *
     * @param waitFor The value to monitor.
     * @param maximum The threshold to wait for the value to reach.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntilAtMost(FloatInput waitFor, float maximum) throws AutonomousModeOverException, InterruptedException {
        // TODO: make sure that nothing is accidentally kept around after this
        waitUntil(waitFor.atMost(maximum));
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
            logger.warn("Negative wait in Instinct: " + milliseconds);
            return;
        } else if (milliseconds == 0) {
            return;// Do nothing.
        }
        ensureShouldBeRunning();
        try {
            Time.sleep(milliseconds);
        } finally {
            ensureShouldBeRunning();
        }
    }

    /**
     * Wait for the specified amount of time, fetched from a FloatInput
     * specified in seconds.
     * 
     * WARNING: If the time changes during the call to this method, the updated
     * value will not be used. Only the value from when the call was originally
     * made will count.
     *
     * @param seconds The amount of time to wait for, in seconds.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitForTime(FloatInput seconds) throws InterruptedException, AutonomousModeOverException {
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
     * Wait until the next time that this module should update.
     */
    abstract void waitCycle() throws InterruptedException;

    /**
     * Make sure that the autonomous mode should still be running.
     *
     * @throws AutonomousModeOverException if the autonomous mode shouldn't be
     * running.
     */
    abstract void ensureShouldBeRunning() throws AutonomousModeOverException;
}