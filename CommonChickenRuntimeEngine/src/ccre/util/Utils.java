package ccre.util;

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

    public static float deadzone(float value, float deadzone) {
        return Math.abs(value) > deadzone ? value : 0.0f;
    }
}
