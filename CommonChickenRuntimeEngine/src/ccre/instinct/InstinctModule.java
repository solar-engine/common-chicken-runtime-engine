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
import ccre.concurrency.ReporterThread;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.Mixing;
import ccre.log.LogLevel;
import ccre.log.Logger;

/**
 * The base class for an Instinct (the simple autonomous subsystem) module.
 *
 * @author skeggsc
 */
public abstract class InstinctModule implements EventOutput {

    /**
     * If the instinct module should currently be running.
     */
    private BooleanInputPoll shouldBeRunning;
    /**
     * If this module is currently running.
     */
    private volatile boolean isRunning = false;
    /**
     * If this module has finished execution and is waiting for the signal to
     * stop running before continuing.
     */
    private volatile boolean isEndWaiting = false;
    /**
     * The object used to coordinate when the instinct module should resume
     * execution.
     */
    private final Object autosynch = new Object();

    /**
     * The main thread for code running in this Instinct Module.
     */
    private final ReporterThread main = new ReporterThread("Instinct") {
        @Override
        protected void threadBody() {
            instinctBody();
        }
    };

    /**
     * Create a new InstinctModule with a BooleanInputPoll controlling when this
     * module should run.
     *
     * @param shouldBeRunning The input to control the running of this module.
     */
    public InstinctModule(BooleanInputPoll shouldBeRunning) {
        if (shouldBeRunning == null) {
            throw new NullPointerException();
        }
        this.shouldBeRunning = shouldBeRunning;
    }

    /**
     * Create a new InstinctModule that needs to be registered with an
     * InstinctRegistrar before it can run.
     */
    public InstinctModule() {
        this.shouldBeRunning = null;
    }

    private void instinctBody() {
        while (true) {
            isRunning = false;
            while (!shouldBeRunning.get()) { // TODO: Is it an issue to have this in here instead of further out?
                try {
                    waitCycle();
                } catch (InterruptedException ex) {
                }
            }
            try {
                // Get rid of any lingering interruptions.
                waitCycle();
            } catch (InterruptedException ex) {
            }
            try {
                try {
                    isRunning = true;
                    Logger.info("Started " + getTypeName() + ".");
                    autonomousMain();
                    Logger.info("Completed " + getTypeName() + ".");
                } catch (InterruptedException ex) {
                    Logger.info("Interrupted " + getTypeName() + ".");
                } catch (AutonomousModeOverException ex) {
                    Logger.info("Exited " + getTypeName() + " by stop.");
                    continue;
                }
            } catch (Throwable t) {
                Logger.log(LogLevel.SEVERE, "Exception thrown during Autonomous mode!", t);
            }
            isRunning = false;
            isEndWaiting = true;
            while (shouldBeRunning.get()) {
                try {
                    // Wait until no longer supposed to be running.
                    waitCycle();
                } catch (InterruptedException ex) {
                }
            }
            isEndWaiting = false;
        }
    }

    /**
     * The name of this control loop. Defaults to "autonomous mode".
     *
     * @return The name to use in printouts.
     */
    protected String getTypeName() {
        return "autonomous mode";
    }

    /**
     * Sets this module to run when the specified BooleanInputPoll is true. You
     * also need to fire the InstinctModule's event - likely with updateWhen().
     *
     * @param when When this should be running.
     * @see #updateWhen(ccre.channel.EventInput)
     */
    public void setShouldBeRunning(BooleanInputPoll when) {
        if (this.shouldBeRunning != null) {
            throw new IllegalStateException();
        }
        shouldBeRunning = when;
    }

    /**
     * Sets this module to be updated (continue execution) when the specified
     * event is produced.
     *
     * @param src The event to wait for to continue execution.
     */
    public void updateWhen(EventInput src) {
        src.send(this);
    }

    /**
     * Wait until the next time that this module is updated.
     */
    private void waitCycle() throws InterruptedException {
        synchronized (autosynch) {
            autosynch.wait();
        }
    }

    public final void event() {
        if (shouldBeRunning == null) {
            throw new RuntimeException("You need to have specified when the Insight module should be running!");
        }
        if (shouldBeRunning.get() || isEndWaiting) {
            if (!main.isAlive()) {
                main.start();
            }
            synchronized (autosynch) {
                autosynch.notifyAll();
            }
        } else if (isRunning) {
            synchronized (autosynch) {
                autosynch.notifyAll();
            }
            main.interrupt();
        }
    }

    /**
     * Wait until the specified BooleanInputPoll becomes true before returning.
     *
     * @param waitFor The condition to wait until.
     * @throws AutonomousModeOverException If the autonomous mode has ended.
     * @throws InterruptedException Possibly also if autonomous mode has ended.
     */
    protected void waitUntil(BooleanInputPoll waitFor) throws AutonomousModeOverException, InterruptedException {
        while (true) {
            if (!shouldBeRunning.get()) {
                throw new AutonomousModeOverException();
            }
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
        final Object localAutosynch = autosynch;
        EventOutput c = new EventOutput() {
            public void event() {
                b[0] = true;
                synchronized (localAutosynch) {
                    localAutosynch.notifyAll();
                }
            }
        };
        source.send(c);
        try {
            while (!b[0]) {
                if (!shouldBeRunning.get()) {
                    throw new AutonomousModeOverException();
                }
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
            if (!shouldBeRunning.get()) {
                throw new AutonomousModeOverException();
            }
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
     * @param milliseconds The amount of time to wait for.
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
            if (!shouldBeRunning.get()) {
                throw new AutonomousModeOverException();
            }
            throw ex;
        }
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
}
