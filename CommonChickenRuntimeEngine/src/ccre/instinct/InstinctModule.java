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
import ccre.concurrency.ReporterThread;
import ccre.time.Time;

/**
 * The base class for an Instinct (the simple autonomous subsystem) module.
 *
 * @author skeggsc
 */
public abstract class InstinctModule extends InstinctBaseModule {
    private static Logger logger = LoggerFactory.getLogger(InstinctModule.class);

    /**
     * If the instinct module should currently be running.
     */
    private BooleanInput shouldBeRunning;
    /**
     * The amount of time between condition checks for most things that auto is
     * waiting on. Must be positive. 20 milliseconds by default.
     */
    private int autoCycleRate = 20;

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
     * Create a new InstinctModule with a BooleanInput controlling when this
     * module should run.
     *
     * @param shouldBeRunning The input to control the running of this module.
     */
    public InstinctModule(BooleanInput shouldBeRunning) {
        if (shouldBeRunning == null) {
            throw new NullPointerException();
        }
        setShouldBeRunning(shouldBeRunning);
    }

    /**
     * Create a new InstinctModule that needs to be registered before it will be
     * useful.
     *
     * @see ccre.frc.FRC#registerAutonomous(InstinctModule)
     */
    public InstinctModule() {
        this.shouldBeRunning = null;
    }

    /**
     * Get the amount of time between condition checks for most things that auto
     * is waiting on. Must be positive. 20 milliseconds by default.
     * 
     * @return the autoSynchTimeout
     */
    public int getAutoCycleRate() {
        return autoCycleRate;
    }

    /**
     * Set the amount of time between condition checks for most things that auto
     * is waiting on. Must be positive. 20 milliseconds by default.
     * 
     * @param autoCycleRate the autoCycleRate to set
     * @throws IllegalArgumentException if the specified timeout is not positive
     */
    public void setAutoCycleRate(int autoCycleRate) throws IllegalArgumentException {
        if (autoCycleRate <= 0) {
            throw new IllegalArgumentException("AutoSynchTimeout must be positive!");
        }
        this.autoCycleRate = autoCycleRate;
    }

    private void instinctBody() {
        while (true) {
            while (!shouldBeRunning.get()) {
                try {
                    waitCycle();
                } catch (InterruptedException ex) {
                }
            }
            try {// TODO: Is this needed any longer?
                 // Get rid of any lingering interruptions.
                waitCycle();
            } catch (InterruptedException ex) {
            }
            try {
                try {
                    logger.debug("Started " + getTypeName() + ".");
                    autonomousMain();
                    logger.debug("Completed " + getTypeName() + ".");
                } catch (InterruptedException ex) {
                    logger.debug("Interrupted " + getTypeName() + ".");
                } catch (AutonomousModeOverException ex) {
                    logger.debug("Exited " + getTypeName() + " by stop.");
                    continue;
                }
            } catch (Throwable t) {
                logger.error("Exception thrown during Autonomous mode!", t);
            }
            while (shouldBeRunning.get()) {
                try {
                    // Wait until no longer supposed to be running.
                    waitCycle();
                } catch (InterruptedException ex) {
                }
            }
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
     * Sets this module to run when the specified BooleanInput is true. You also
     * need to fire the InstinctModule's event - likely with updateWhen().
     *
     * @param when When this should be running.
     * @see #updateWhen(ccre.channel.EventInput)
     */
    public void setShouldBeRunning(BooleanInput when) {
        if (this.shouldBeRunning != null) {
            throw new IllegalStateException();
        }
        if (when == null) {
            throw new NullPointerException();
        }
        shouldBeRunning = when;
        if (!main.isAlive()) {
            main.start();
        }
    }

    void waitCycle() throws InterruptedException {
        Time.sleep(autoCycleRate);
    }

    void ensureShouldBeRunning() throws AutonomousModeOverException {
        if (!shouldBeRunning.get()) {
            throw new AutonomousModeOverException();
        }
    }
}
