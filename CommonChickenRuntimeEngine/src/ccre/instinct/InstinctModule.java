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
import ccre.concurrency.ReporterThread;
import ccre.log.Logger;

/**
 * The base class for an Instinct (the simple autonomous subsystem) module.
 *
 * @author skeggsc
 */
public abstract class InstinctModule extends InstinctBaseModule implements EventOutput {

    /**
     * If the instinct module should currently be running.
     */
    BooleanInputPoll shouldBeRunning;
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
    final Object autosynch = new Object();

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
     * Create a new InstinctModule that needs to be registered before it will be
     * useful.
     *
     * @see ccre.igneous.Igneous#registerAutonomous(InstinctModule)
     */
    public InstinctModule() {
        this.shouldBeRunning = null;
    }

    private void instinctBody() {
        while (true) {
            isRunning = false;
            while (!shouldBeRunning.get()) {
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
                Logger.severe("Exception thrown during Autonomous mode!", t);
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

    void waitCycle() throws InterruptedException {
        synchronized (autosynch) {
            autosynch.wait();
        }
    }

    void notifyCycle() {
        synchronized (autosynch) {
            autosynch.notifyAll();
        }
    }

    void ensureShouldBeRunning() throws AutonomousModeOverException {
        if (!shouldBeRunning.get()) {
            throw new AutonomousModeOverException();
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
            notifyCycle();
        } else if (isRunning) {
            notifyCycle();
            main.interrupt();
        }
    }
}
