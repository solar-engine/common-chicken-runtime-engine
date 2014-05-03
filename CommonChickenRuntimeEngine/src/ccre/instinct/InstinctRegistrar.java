/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventOutput;

/**
 * An object that can have InstinctModules attached to it.
 *
 * @author skeggsc
 */
public interface InstinctRegistrar {

    /**
     * @return the input representing when subscribed modules should be running.
     */
    public BooleanInputPoll getWhenShouldAutonomousBeRunning();

    /**
     * Register the specified EventConsumer to be fired each period, however
     * that is defined.
     *
     * @param toUpdate The EventConsumer to update.
     */
    public void updatePeriodicallyAlways(EventOutput toUpdate);
}
