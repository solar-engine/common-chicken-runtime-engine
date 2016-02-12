package ccre.timers;

import ccre.channel.BooleanCell;
import ccre.time.Time;

/**
 * A timer that, when read, returns the amount of time since it was last reset.
 * 
 * @author amackworth
 */
public final class ElapsedTimer {
    /**
     * Is the timer currently running?
     */
    public final BooleanCell isRunning = new BooleanCell();
    private long startTime;

    /**
     * Starts the timer. Note: {@link #reset()} must be called first before a timer is started anew.
     */
    public void start() {
        if (isRunning.get()) {
            throw new IllegalStateException("Timer is running!");
        }

        isRunning.safeSet(true);
        startTime = Time.currentTimeMillis();
    }

    /**
     * Reads how long the timer has been running.
     * 
     * @return the milliseconds passed since the timer was started.
     */
    public long read() {
        if (!isRunning.get()) {
            throw new IllegalStateException("Timer not running!");
        }

        return Time.currentTimeMillis() - startTime;
    }

    /**
     * Stops and resets the timer.
     */
    public void reset() {
        isRunning.safeSet(false);
        startTime = 0;
    }
}