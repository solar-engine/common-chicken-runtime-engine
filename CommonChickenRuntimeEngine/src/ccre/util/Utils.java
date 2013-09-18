package ccre.util;

import ccre.chan.FloatInputPoll;
import ccre.ctrl.Mixing;

/**
 * A class for utilities that don't fit anywhere else. Most utilites are in
 * Mixing or CArrayUtils.
 *
 * @see Mixing
 * @see CArrayUtils
 * @author skeggsc
 */
public class Utils {

    /**
     * Calculate a value with a deadzone. If the value is within the specified
     * deadzone, the result will be zero instead.
     *
     * @param value the value
     * @param deadzone the deadzone size
     * @return the deadzoned version of the value
     */
    public static float deadzone(float value, float deadzone) {
        return Math.abs(value) > deadzone ? value : 0.0f;
    }
    /**
     * An input representing the current time in seconds. The value is
     * equivalent to
     * <code>System.currentTimeMillis() / 1000.0f</code>
     *
     * @see java.lang.System#currentTimeMillis()
     */
    public static final FloatInputPoll currentTimeSeconds = new FloatInputPoll() {
        public float readValue() {
            return System.currentTimeMillis() / 1000.0f;
        }
    };
}
