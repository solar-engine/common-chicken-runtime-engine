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

import ccre.holders.TuningContext;

/**
 * An Instinct module that can be added to an InstinctMultiModule.
 *
 * This is relevant if you want multiple autonomous modes to easily switch
 * between.
 *
 * @author skeggsc
 */
public abstract class InstinctModeModule extends InstinctBaseModule {

    /**
     * The name of the mode, as specified during creation.
     */
    public final String modeName;
    private InstinctMultiModule parent;

    /**
     * Create a new InstinctModeModule with a given mode name.
     *
     * @param modeName the name of this mode, used during selection and saving.
     */
    public InstinctModeModule(String modeName) {
        if (modeName == null) {
            throw new NullPointerException("modeName is null");
        }
        this.modeName = modeName;
    }

    /**
     * Load any needed settings from a TuningContext.
     *
     * @param context the tuning context to load from.
     */
    public abstract void loadSettings(TuningContext context);

    void setParent(InstinctMultiModule parent) {
        if (parent == null) {
            throw new NullPointerException();
        }
        if (this.parent != null) {
            throw new IllegalStateException("The InstinctModeModule has already been registered!");
        }
        this.parent = parent;
    }

    @Override
    void waitCycle() throws InterruptedException {
        parent.waitCycle();
    }

    @Override
    void ensureShouldBeRunning() throws AutonomousModeOverException {
        parent.ensureShouldBeRunning();
        if (this != parent.getActiveMode()) {
            throw new AutonomousModeOverException("Incorrect mode is running: " + this.getModeName() + " instead of " + (parent.getActiveMode() != null ? parent.getActiveMode().getModeName() : "null"));
        }
    }

    /**
     * Get the mode name of this mode.
     *
     * @return the mode's name.
     */
    public final String getModeName() {
        return modeName;
    }

}
