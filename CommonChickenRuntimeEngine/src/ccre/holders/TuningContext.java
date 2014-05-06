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
package ccre.holders;

import ccre.channel.EventOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.log.Logger;
import ccre.saver.StorageProvider;
import ccre.saver.StorageSegment;

/**
 * A TuningContext represents a context in which variables can be saved and
 * published to the network.
 *
 * @author skeggsc
 */
public final class TuningContext { // TODO: Support booleans for tuning.

    /**
     * The node to publish the value to.
     */
    private final CluckNode enc;
    /**
     * The segment to store the value in.
     */
    private final StorageSegment seg;

    /**
     * Create a new TuningContext from a specified CluckNode and name of storage
     * (used to find the StorageSegment)
     *
     * @param node the CluckNode to share values over.
     * @param storageName the storage name to save values to.
     */
    public TuningContext(CluckNode node, String storageName) {
        this(node, StorageProvider.openStorage(storageName));
    }

    /**
     * Create a new TuningContext from a specified CluckNode and a specified
     * StorageSegment.
     *
     * @param enc the CluckNode to share values over.
     * @param seg the segment to save values to.
     */
    public TuningContext(CluckNode enc, StorageSegment seg) {
        this.enc = enc;
        this.seg = seg;
    }

    /**
     * Get a FloatStatus with the specified name and default value. This will be
     * tunable over the network and saved on the cRIO once flush() is called.
     *
     * @param name the name of the tunable value.
     * @param default_ the default value.
     * @return the FloatStatus representing the current value.
     */
    public FloatStatus getFloat(String name, float default_) {
        FloatStatus out = new FloatStatus(default_);
        seg.attachFloatHolder(name, out);
        CluckPublisher.publish(enc, name, out);
        return out;
    }

    /**
     * Flush the StorageSegment - save the current value.
     */
    public void flush() {
        seg.flush();
        Logger.info("Flushed storage segment.");
    }

    /**
     * Get an event that flushes this object.
     *
     * @return the EventConsumer that will flush this object.
     * @see #flush()
     */
    public EventOutput getFlushEvent() {
        return new EventOutput() {
            public void event() {
                flush();
            }
        };
    }

    /**
     * Publish an EventConsumer that can be used to save the tuning variables on
     * this context.
     *
     * @param name The name for the EventConsumer to be published under.
     * (Prefixed by "Save Tuning for ".)
     * @return This TuningContext. Returned for method chaining purposes.
     */
    public TuningContext publishSavingEvent(String name) {
        CluckPublisher.publish(enc, "Save Tuning for " + name, getFlushEvent());
        return this;
    }
}
