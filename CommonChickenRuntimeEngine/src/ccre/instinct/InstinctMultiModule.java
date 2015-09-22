/*
 * Copyright 2014-2015 Colby Skeggs
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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;
import ccre.cluck.CluckPublisher;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.tuning.TuningContext;

/**
 * An easy way to have multiple autonomous modes. Simply register a series of
 * InstinctModeModules with this.
 *
 * @author skeggsc
 */
public class InstinctMultiModule extends InstinctModule {
    private static Logger logger = LoggerFactory.getLogger(InstinctMultiModule.class);

    /**
     * The list of modes registered with this InstinctMultiModule.
     */
    private final ArrayList<InstinctModeModule> modes = new ArrayList<InstinctModeModule>();
    /**
     * The actively-selected mode, or null.
     */
    private InstinctModeModule mode;
    /**
     * The TuningContext used both for the storage segment and cluck node to
     * store and edit the current mode.
     */
    private final TuningContext context;

    /**
     * Create a new InstinctMultiModule with a BooleanInput controlling when
     * this module should run.
     *
     * @param shouldBeRunning The input to control the running of this module.
     * @param context the TuningContext to use in this MultiModule.
     */
    public InstinctMultiModule(BooleanInput shouldBeRunning, TuningContext context) {
        super(shouldBeRunning);
        this.context = context;
    }

    /**
     * Create a new InstinctModule that needs to be registered before it will be
     * useful.
     *
     * @param context the TuningContext to use in this MultiModule.
     *
     * @see ccre.frc.FRC#registerAutonomous(InstinctModule)
     */
    public InstinctMultiModule(TuningContext context) {
        super();
        this.context = context;
    }

    /**
     * Set the active mode to the given mode.
     *
     * @param mode the new active mode.
     * @throws IllegalArgumentException if the specified mode does not exist in
     * this MultiModule.
     */
    public void setActiveMode(InstinctModeModule mode) throws IllegalArgumentException {
        if (!modes.contains(mode)) {
            throw new IllegalArgumentException("The specified mode does not exist: " + mode.getModeName() + " in " + modes);
        }
        this.mode = mode;
        this.context.getSegment().setStringForKey("autonomous-mode", mode.getModeName());
    }

    /**
     * Find the active mode.
     *
     * @return the active mode.
     */
    public InstinctModeModule getActiveMode() {
        return mode;
    }

    /**
     * Publish controls over Cluck to manipulate the current mode. These are a
     * set of buttons that view or change them mode.
     *
     * By default, only "Autonomous Mode Check" will be published, which will
     * log the current autonomous mode. This lets the driver know the current
     * mode.
     *
     * If showIndividualModes is true, then each mode registered so far will get
     * its own "Autonomous Mode: &lt;mode&gt;" button that will switch to that
     * mode.
     *
     * If showCycleChooser is true, then "Autonomous Mode Next" and
     * "Autonomous Mode Previous" will be published, which will cycle through
     * the autonomous modes.
     *
     * @param showIndividualModes if the individual modes should have controls.
     * @param showCycleChooser if a cyclical selector should have controls.
     */
    public void publishDefaultControls(boolean showIndividualModes, boolean showCycleChooser) {
        CluckPublisher.publish(context.getNode(), "Autonomous Mode Check", new EventOutput() {
            public void event() {
                logger.info("Current autonomous mode: " + mode.getModeName());
            }
        });
        if (showIndividualModes) {
            for (final InstinctModeModule curmode : modes) {
                CluckPublisher.publish(context.getNode(), "Autonomous Mode: " + curmode.getModeName(), new EventOutput() {
                    public void event() {
                        setActiveMode(curmode);
                        logger.info("New autonomous mode: " + mode.getModeName());
                    }
                });
            }
        }
        if (showCycleChooser) {
            CluckPublisher.publish(context.getNode(), "Autonomous Mode Next", new EventOutput() {
                public void event() {
                    boolean wasLast = (mode == modes.get(modes.size() - 1));
                    for (InstinctModeModule m : modes) {
                        if (wasLast) {
                            setActiveMode(m);
                            logger.info("New autonomous mode: " + mode.getModeName());
                            return;
                        } else {
                            wasLast = (m == mode);
                        }
                    }
                    // Couldn't find the mode. (We would have returned earlier.)
                    logger.warn("Mode not found while iterating: " + mode.getModeName());
                    setActiveMode(modes.get(0));// Just use the first mode.
                    logger.info("New autonomous mode: " + mode.getModeName());
                }
            });
            CluckPublisher.publish(context.getNode(), "Autonomous Mode Previous", new EventOutput() {
                public void event() {
                    InstinctModeModule last = modes.get(modes.size() - 1);
                    for (InstinctModeModule m : modes) {
                        if (m == mode) {
                            setActiveMode(last);
                            logger.info("New autonomous mode: " + mode.getModeName());
                            return;
                        }
                        last = m;
                    }
                    // Couldn't find the mode. (We would have returned earlier.)
                    logger.warn("Mode not found while iterating: " + mode.getModeName());
                    setActiveMode(modes.get(0));// Just use the first mode.
                    logger.info("New autonomous mode: " + mode.getModeName());
                }
            });
        }
    }

    /**
     * Publish an RConfComponent that shows the current autonomous mode and
     * allows to change it.
     */
    public void publishRConfControls() {
        CluckPublisher.publishRConf(context.getNode(), "Autonomous Mode Selector", new RConfable() {
            public Entry[] queryRConf() throws InterruptedException {
                Entry[] outs = new Entry[2 + modes.size()];
                outs[0] = RConf.title("Select Autonomous Mode");
                int i = 1;
                for (InstinctModeModule m : modes) {
                    outs[i++] = (m == mode) ? RConf.string(m.getModeName()) : RConf.button(m.getModeName());
                }
                outs[i] = RConf.autoRefresh(5000);
                return outs;
            }

            public boolean signalRConf(int field, byte[] data) throws InterruptedException {
                if (field >= 1 && field <= modes.size()) {
                    setActiveMode(modes.get(field - 1));
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Load the default mode setting and any settings in any of the modes.
     *
     * @param defaultMode the mode to default to if there is no valid saved
     * mode.
     */
    public void loadSettings(InstinctModeModule defaultMode) {
        if (defaultMode == null) {
            throw new NullPointerException("A default mode must be specified!");
        }
        String modeName = this.context.getSegment().getStringForKey("autonomous-mode");
        if (modeName == null) {
            this.mode = defaultMode;
        } else {
            this.mode = null;
            for (InstinctModeModule foundMode : modes) {
                if (foundMode.getModeName().equals(modeName)) {
                    this.mode = foundMode;
                }
            }
            if (this.mode == null) {
                logger.warn("Invalid loaded mode name: " + modeName);
                this.mode = defaultMode;
            }
        }

        for (InstinctModeModule curmode : modes) {
            curmode.loadSettings(context);
        }
    }

    /**
     * Add the specified mode to this MultiModule.
     *
     * @param newMode the mode to add.
     * @return the added mode.
     * @throws IllegalArgumentException if the mode's name is already used.
     */
    public InstinctModeModule addMode(InstinctModeModule newMode) throws IllegalArgumentException {
        for (InstinctModeModule oldMode : modes) {
            if (oldMode.getModeName().equals(newMode.getModeName())) {
                throw new IllegalArgumentException("Duplicate mode name: " + newMode.getModeName());
            }
        }
        newMode.setParent(this);
        modes.add(newMode);
        return newMode;
    }

    /**
     * Add a "null mode" to this MultiModule.
     *
     * The null mode will have the given name and will do nothing but log a
     * message when it runs. This could be "No mode selected" or something.
     *
     * @param name the null mode's name.
     * @param message the null mode's message.
     * @return the newly created mode.
     */
    public InstinctModeModule addNullMode(String name, final String message) {
        if (message == null) {
            throw new NullPointerException("The null mode's message cannot be null!");
        }
        return this.addMode(new InstinctModeModule(name) {
            @Override
            protected void autonomousMain() throws AutonomousModeOverException, InterruptedException {
                logger.info(message);
            }

            @Override
            public void loadSettings(TuningContext ignoredContext) {
                // Don't load anything.
            }
        });
    }

    @Override
    protected final void autonomousMain() throws AutonomousModeOverException, InterruptedException {
        if (mode == null) {
            logger.error("No autonomous mode found! Did you remember to call InstinctMultiModule.loadSettings()?");
        } else {
            logger.info("Running autonomous mode: " + mode.getModeName());
            mode.autonomousMain();
        }
    }
}
