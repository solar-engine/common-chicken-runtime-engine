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
package ccre.tuning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanStatus;
import ccre.channel.EventOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckPublisher;
import ccre.storage.Storage;
import ccre.storage.StorageSegment;
import ccre.util.UniqueIds;

/**
 * A TuningContext represents a context in which variables can be saved and
 * published to the network.
 *
 * @author skeggsc
 */
public final class TuningContext {
    private static Logger logger = LoggerFactory.getLogger(TuningContext.class);

    /**
     * The node to publish the values to.
     */
    private final CluckNode node;
    /**
     * The segment to store the values in.
     */
    private final StorageSegment segment;

    /**
     * Create a new TuningContext from a specified CluckNode and name of storage
     * (used to find the StorageSegment)
     *
     * @param node the CluckNode to share values over.
     * @param storageName the storage name to save values to.
     */
    public TuningContext(CluckNode node, String storageName) {
        this(node, Storage.openStorage(storageName));
    }

    /**
     * Create a new TuningContext from a specified CluckNode and a specified
     * StorageSegment.
     *
     * @param enc the CluckNode to share values over.
     * @param seg the segment to save values to.
     */
    public TuningContext(CluckNode enc, StorageSegment seg) {
        this.node = enc;
        this.segment = seg;
    }

    /**
     * Create a new TuningContext from the global CluckNode and name of storage
     * (used to find the StorageSegment)
     *
     * @param storageName the storage name to save values to.
     */
    public TuningContext(String storageName) {
        this(Cluck.getNode(), Storage.openStorage(storageName));
    }

    /**
     * Create a new TuningContext from the global CluckNode and a specified
     * StorageSegment.
     *
     * @param seg the segment to save values to.
     */
    public TuningContext(StorageSegment seg) {
        this(Cluck.getNode(), seg);
    }

    /**
     * Get the attached Cluck node.
     *
     * @return the CluckNode behind this TuningContext.
     */
    public CluckNode getNode() {
        return node;
    }

    /**
     * Get the attached storage segment.
     *
     * @return the storage segment behind this TuningContext.
     */
    public StorageSegment getSegment() {
        return segment;
    }

    /**
     * Get a FloatStatus with the specified name and default value. This will be
     * tunable over the network and saved on the roboRIO once flush() is called.
     *
     * @param name the name of the tunable value.
     * @param default_ the default value.
     * @return the FloatStatus representing the current value.
     */
    public FloatStatus getFloat(String name, float default_) {
        FloatStatus out = new FloatStatus(default_);
        segment.attachFloatHolder(name, out);
        CluckPublisher.publish(node, name, out);
        return out;
    }

    /**
     * Get a BooleanStatus with the specified name and default value. This will
     * be tunable over the network and saved on the roboRIO once flush() is
     * called.
     *
     * @param name the name of the tunable value.
     * @param default_ the default value.
     * @return the BooleanStatus representing the current value.
     */
    public BooleanStatus getBoolean(String name, boolean default_) {
        BooleanStatus out = new BooleanStatus(default_);
        segment.attachBooleanHolder(name, out);
        CluckPublisher.publish(node, name, out);
        return out;
    }

    /**
     * Flush the StorageSegment - save the current value.
     */
    public void flush() {
        segment.flush();
        logger.info("Flushed storage segment " + segment.getName());
    }

    /**
     * Get an event that flushes this object.
     *
     * @return the EventOutput that will flush this object.
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
     * Publish an EventOutput that can be used to save the tuning variables on
     * this context.
     *
     * @param name The name for the EventOutput to be published under. (Prefixed
     * by "Save Tuning for ".)
     * @return This TuningContext. Returned for method chaining purposes.
     */
    public TuningContext publishSavingEvent(String name) {
        CluckPublisher.publish(node, "Save Tuning for " + name, getFlushEvent());
        return this;
    }

    /**
     * Publish an EventOutput that can be used to save the tuning variables on
     * this context. The name will be "Save Tuning for " followed by the name of
     * the StorageSegment that this context uses. If no name is available,
     * "anonymous-N" will be used instead, where N is an arbitrary number.
     *
     * @return This TuningContext. Returned for method chaining purposes.
     */
    public TuningContext publishSavingEvent() {
        String name = segment.getName();
        return publishSavingEvent(name == null ? UniqueIds.global.nextHexId("anonymous") : name);
    }
}
