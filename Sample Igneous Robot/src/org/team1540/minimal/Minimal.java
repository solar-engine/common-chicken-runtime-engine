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
package org.team1540.minimal;

import ccre.cluck.CluckGlobals;
import ccre.event.EventConsumer;
import ccre.igneous.SimpleCore;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.testing.ComputationBenchmark;
import ccre.util.Utils;

/**
 * A very simple example program.
 *
 * @author skeggsc
 */
public class Minimal extends SimpleCore {

    protected void createSimpleControl() {
        CluckGlobals.node.publish("compute-benchmarks", new EventConsumer() {
            public void eventFired() {
                Logger.info("Start");
                try {
                    ComputationBenchmark.main(new String[0]);
                } catch (Throwable thr) {
                    Logger.log(LogLevel.WARNING, "Oops!", thr);
                }
                Logger.info("End");
            }
        });
        makeDSFloatReadout("I live!", 1, Utils.currentTimeSeconds, globalPeriodic);
    }
}
