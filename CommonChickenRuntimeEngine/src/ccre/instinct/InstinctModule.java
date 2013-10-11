/*
 * Copyright 2013 Colby Skeggs
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

import ccre.chan.BooleanInputPoll;
import ccre.chan.FloatInputPoll;
import ccre.concurrency.ReporterThread;
import ccre.ctrl.Mixing;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;

/**
 * The base class for an Instinct (the simple autonomous subsystem) module.
 *
 * @author skeggsc
 */
public abstract class InstinctModule implements EventConsumer {

    private BooleanInputPoll shouldBeRunning;
    private boolean isRunning = false, isEndWaiting = false;

    public InstinctModule(BooleanInputPoll shouldBeRunning) {
        this.shouldBeRunning = shouldBeRunning;
    }

    public InstinctModule() {
        this.shouldBeRunning = null;
    }
    
    public void register(InstinctRegistrar reg) {
        this.shouldBeRunning = reg.getWhenShouldAutonomousBeRunning();
        reg.updatePeriodicallyAlways(this);
    }

    public void updateWhen(EventSource src) {
        src.addListener(this);
    }
    private final Object autosynch = new Object();
    private final ReporterThread main = new ReporterThread("Instinct") {
        @Override
        protected void threadBody() {
            while (true) {
                isRunning = false;
                while (!shouldBeRunning.readValue()) {
                    synchronized (autosynch) {
                        try {
                            autosynch.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                // Get rid of any lingering interruptions.
                synchronized (autosynch) {
                    try {
                        autosynch.wait();
                    } catch (InterruptedException ex) {
                    }
                }
                try {
                    isRunning = true;
                    Logger.info("Started autonomous mode.");
                    autonomousMain();
                    Logger.info("Autonomous mode completed.");
                } catch (InterruptedException ex) {
                    Logger.info("Autonomous mode interrupted.");
                } catch (AutonomousModeOverException ex) {
                    Logger.info("Autonomous mode exited by stop.");
                    continue;
                } catch (Throwable t) {
                    Logger.log(LogLevel.SEVERE, "Exception thrown during Autonomous mode!", t);
                }
                isRunning = false;
                isEndWaiting = true;
                while (shouldBeRunning.readValue()) {
                    try {
                        // Wait until no longer supposed to be running.
                        synchronized (autosynch) {
                            autosynch.wait();
                        }
                    } catch (InterruptedException ex) {
                    }
                }
                isEndWaiting = false;
            }
        }
    };

    public final void eventFired() {
        if (shouldBeRunning == null) {
            throw new RuntimeException("You need to have specified when the Insight module should be running!");
        }
        if (shouldBeRunning.readValue() || isEndWaiting) {
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

    protected void waitUntil(BooleanInputPoll waitFor) throws AutonomousModeOverException, InterruptedException {
        while (true) {
            if (!shouldBeRunning.readValue()) {
                throw new AutonomousModeOverException();
            }
            if (waitFor.readValue()) {
                return;
            }
            synchronized (autosynch) {
                autosynch.wait();
            }
        }
    }

    protected void waitUntilAtLeast(FloatInputPoll waitFor, float minimum) throws AutonomousModeOverException, InterruptedException {
        waitUntil(Mixing.floatIsAtLeast(waitFor, minimum));
    }

    protected void waitUntilAtMost(FloatInputPoll waitFor, float maximum) throws AutonomousModeOverException, InterruptedException {
        waitUntil(Mixing.floatIsAtMost(waitFor, maximum));
    }

    protected void waitForTime(long milliseconds) throws InterruptedException, AutonomousModeOverException {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            if (!shouldBeRunning.readValue()) {
                throw new AutonomousModeOverException();
            }
            throw ex;
        }
    }

    protected abstract void autonomousMain() throws AutonomousModeOverException, InterruptedException;
}
