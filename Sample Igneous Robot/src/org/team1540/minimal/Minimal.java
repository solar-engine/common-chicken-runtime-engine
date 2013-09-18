package org.team1540.minimal;

import ccre.igneous.SimpleCore;
import ccre.util.Utils;

/**
 * A very simple example program.
 *
 * @author skeggsc
 */
public class Minimal extends SimpleCore {

    protected void createSimpleControl() {
        makeDSFloatReadout("I live!", 1, Utils.currentTimeSeconds, globalPeriodic);
    }
}
