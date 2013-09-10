package ccre.holders;

import ccre.chan.FloatStatus;
import ccre.cluck.CluckEncoder;
import ccre.cluck.CluckNode;
import ccre.event.EventConsumer;
import ccre.saver.StorageProvider;
import ccre.saver.StorageSegment;

/**
 * A TuningContext represents a context in which variables can be saved and
 * published to the network.
 *
 * @author skeggsc
 */
public class TuningContext {

    /**
     * The encoder to publish the value to.
     */
    protected CluckEncoder enc;
    /**
     * The segment to store the value in.
     */
    protected StorageSegment seg;

    /**
     * Create a new TuningContext from a specified CluckNode (creates a
     * CluckEncoder on the node) and name of storage (used to find the
     * StorageSegment)
     *
     * @param node the CluckNode to share values over.
     * @param storageName the storage name to save values to.
     */
    public TuningContext(CluckNode node, String storageName) {
        this(new CluckEncoder(node), StorageProvider.openStorage(storageName));
    }

    /**
     * Create a new TuningContext from a specified CluckNode (creates a
     * CluckEncoder on the node) and a specified StorageSegment.
     *
     * @param node the CluckNode to share values over.
     * @param seg the segment to save values to.
     */
    public TuningContext(CluckNode node, StorageSegment seg) {
        this(new CluckEncoder(node), seg);
    }

    /**
     * Create a new TuningContext from a specified CluckEncoder and name of
     * storage (used to find the StorageSegment)
     *
     * @param enc the CluckEncoder to share values over.
     * @param storageName the storage name to save values to.
     */
    public TuningContext(CluckEncoder enc, String storageName) {
        this(enc, StorageProvider.openStorage(storageName));
    }

    /**
     * Create a new TuningContext from a specified CluckEncoder and a specified
     * StorageSegment.
     *
     * @param enc the CluckEncoder to share values over.
     * @param seg the segment to save values to.
     */
    public TuningContext(CluckEncoder enc, StorageSegment seg) {
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
        FloatStatus out = new FloatStatus();
        out.writeValue(default_);
        out.hasBeenModified = false;
        seg.attachFloatHolder(name, out);
        enc.publishTunableFloat(name, out, null);
        return out;
    }

    /**
     * Get a FloatStatus with the specified name, default value, and the name of
     * a encoded channel for a FloatInputProducer that should be an option to
     * tune the variable to. This will be tunable over the network and saved on
     * the cRIO once flush() is called.
     *
     * @param name the name of the tunable value.
     * @param default_ the default value.
     * @param targetref the name of the shared value for the tuning's default.
     * @return the FloatStatus representing the current value.
     */
    public FloatStatus getFloat(String name, float default_, String targetref) {
        FloatStatus out = new FloatStatus();
        out.writeValue(default_);
        out.hasBeenModified = false;
        seg.attachFloatHolder(name, out);
        enc.publishTunableFloat(name, out, targetref);
        return out;
    }

    /**
     * Flush the StorageSegment - save the current value.
     */
    public void flush() {
        seg.flush();
    }

    /**
     * Get an event that flushes this object.
     *
     * @return the EventConsumer that will flush this object.
     * @see #flush()
     */
    public EventConsumer getFlushEvent() {
        return new EventConsumer() {
            public void eventFired() {
                flush();
            }
        };
    }
    
    public void publishSavingEvent(String name) {
        enc.publishEventConsumer("Save Tuning for " + name, getFlushEvent());
    }
}
