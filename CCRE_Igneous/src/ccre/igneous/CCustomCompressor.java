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
package ccre.igneous;

import ccre.chan.BooleanInputPoll;
import ccre.concurrency.ReporterThread;
import edu.wpi.first.wpilibj.Relay;

/**
 * A compressor that replaces the builtin compressor with an alternate source
 * for whether or not to enable the compressor.
 *
 * @author skeggsc
 */
class CCustomCompressor extends ReporterThread {

    /**
     * The pressure switch that decides whether or not the compressor should be
     * running.
     */
    private BooleanInputPoll pressureSwitch;
    /**
     * The relay that controls the compressor.
     */
    private Relay relay;

    /**
     * Create a new CCustomCompressor with the specified pressure control input,
     * and the specified channel for the compressor-control relay.
     *
     * @param pressureSwitch the switch that, when on, turns off the compressor.
     * @param compressorRelayChannel the channel to control the compressor's
     * status.
     */
    CCustomCompressor(BooleanInputPoll pressureSwitch, int compressorRelayChannel) {
        super("Custom-Compressor");
        this.pressureSwitch = pressureSwitch;
        relay = new Relay(compressorRelayChannel, Relay.Direction.kForward);
    }

    protected void threadBody() throws InterruptedException {
        while (true) {
            relay.set(pressureSwitch.readValue() ? Relay.Value.kOff : Relay.Value.kOn);
            Thread.sleep(500);
        }
    }
}