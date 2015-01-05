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
    void notifyCycle() {
        parent.notifyCycle();
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
