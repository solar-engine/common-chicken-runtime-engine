/*
 * Copyright 2015 Cel Skeggs.
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
package ccre.channel;

/**
 * A once-off event that reverts some piece of dataflow set-up. For example,
 * {@link EventInput#send(EventOutput)} returns a <code>CancelOutput</code> that
 * cancels the connection created by the <code>send</code>.
 *
 * Each CancelOutput can only be cancelled once.
 *
 * @author skeggsc
 */
public interface CancelOutput {
    /**
     * A CancelOutput that cancels nothing.
     */
    public static final CancelOutput nothing = () -> {
    };

    /**
     * Cancels this CancelOutput, which means something based on where it was
     * acquired from.
     */
    public void cancel();

    /**
     * Combines this CancelOutput with another CancelOutput, such that the
     * combined CancelOutput, when cancelled, will cancel both of the original
     * CancelOutputs.
     *
     * @param other the other CancelOutput to include
     * @return the combined CancelOutput
     */
    public default CancelOutput combine(CancelOutput other) {
        return () -> {
            cancel();
            other.cancel();
        };
    }
}
