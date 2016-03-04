/*
 * Copyright 2016 Cel Skeggs
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
package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.EventOutput;

/**
 * A device that can report one of a set of faults as currently occurring, or as
 * having occurred recently - i.e. not yet cleared.
 *
 * @author skeggsc
 * @param <Fault> the type of the faults. often an enum.
 */
public interface Faultable<Fault> {
    /**
     * Lists the possible faults that can be queried on this Faultable.
     *
     * @return a newly-allocated array of possible faults.
     */
    public Fault[] getPossibleFaults();

    /**
     * Returns a BooleanInput that is true when <code>fault</code> is currently
     * occurring. If a fault occurs for only a very short amount of time, the
     * BooleanInput might not update, depending on the details of the
     * implementation.
     *
     * @param fault the fault to query
     * @return the fault state input
     */
    public BooleanInput getIsFaulting(Fault fault);

    /**
     * Returns a BooleanInput that is true when <code>fault</code> has occurred
     * since the last time that the result of {@link #getClearStickyFaults()}
     * has been fired. This should, in theory, be set regardless of the amount
     * of time for which the fault occurs.
     *
     * There may be other ways by which the faults may be cleared.
     *
     * @param fault the fault to query
     * @return the fault state input
     */
    public BooleanInput getIsStickyFaulting(Fault fault);

    /**
     * Provides an EventOutput that clears all of the sticky faults on this
     * device.
     *
     * @return the EventOutput
     */
    public EventOutput getClearStickyFaults();
}
