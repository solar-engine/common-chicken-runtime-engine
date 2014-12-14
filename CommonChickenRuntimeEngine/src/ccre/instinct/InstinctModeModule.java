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

    public final String modeName;
    private InstinctMultiModule parent;

    public InstinctModeModule(String modeName) {
        if (modeName == null) {
            throw new NullPointerException("modeName is null");
        }
        this.modeName = modeName;
    }
    
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
     * @return the mode's name.
     */
    public final String getModeName() {
        return modeName;
    }

}
