package ccre.chan;

import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;

/**
 * A virtual node that is both a BooleanOutput and a BooleanInput. You can
 * modify its value, read its value, and subscribe to changes in its value.
 * BooleanStatus also provides a number of useful helper functions.
 *
 * @author skeggsc
 */
public class BooleanStatus implements BooleanOutput, BooleanInput {

    /**
     * Create a new BooleanStatus.
     */
    public BooleanStatus() {
    }

    /**
     * Create a new BooleanStatus that automatically updates the specified
     * BooleanOutput with the current state of this BooleanStatus. This is the
     * same as creating a new BooleanStatus and then adding the BooleanOutput as
     * a target.
     *
     * @see BooleanStatus#addTarget(ccre.chan.BooleanOutput)
     * @param target The BooleanOutput to automatically update.
     */
    public BooleanStatus(BooleanOutput target) {
        consumers = new CArrayList<BooleanOutput>();
        consumers.add(target);
        target.writeValue(false);
    }

    /**
     * Create a new BooleanStatus that automatically updates all of the
     * specified BooleanOutputs with the current state of this BooleanStatus.
     * This is the same as creating a new BooleanStatus and then adding all of
     * the BooleanOutputs as targets.
     *
     * @see BooleanStatus#addTarget(ccre.chan.BooleanOutput)
     * @param targets The BooleanOutputs to automatically update.
     */
    public BooleanStatus(BooleanOutput... targets) {
        consumers = new CArrayList<BooleanOutput>(CArrayUtils.asList(targets));
        for (BooleanOutput t : targets) {
            t.writeValue(false);
        }
    }
    /**
     * The current state (true or false) of this BooleanStatus. Do not directly
     * modify this field. Use the writeValue method instead.
     *
     * @see #writeValue(boolean)
     */
    protected boolean value;
    /**
     * The list of all the BooleanOutputs to modify when this BooleanStatus
     * changes value.
     *
     * @see #addTarget(ccre.chan.BooleanOutput)
     * @see #removeTarget(ccre.chan.BooleanOutput)
     */
    protected CArrayList<BooleanOutput> consumers = null;
    /**
     * The cached EventConsumer that sets the current value to true. Use
     * getSetTrueEvent() instead, because this might be null.
     *
     * @see #getSetTrueEvent()
     */
    protected EventConsumer setTrue;
    /**
     * The cached EventConsumer that sets the current value to false. Use
     * getSetFalseEvent() instead, because this might be null.
     *
     * @see #getSetFalseEvent()
     */
    protected EventConsumer setFalse;
    /**
     * The cached EventConsumer that toggles the current value. Use
     * getToggleEvent() instead, because this might be null.
     *
     * @see #getToggleEvent()
     */
    protected EventConsumer toggle;

    /**
     * When the specified event occurs, set the status to true.
     *
     * @param event When to set the status to true.
     * @see #getSetTrueEvent()
     */
    public void setTrueWhen(EventSource event) {
        event.addListener(getSetTrueEvent());
    }

    /**
     * When the specified event occurs, set the status to false.
     *
     * @param event When to set the status to false.
     * @see #getSetFalseEvent()
     */
    public void setFalseWhen(EventSource event) {
        event.addListener(getSetFalseEvent());
    }

    /**
     * When the specified event occurs, toggle the status.
     *
     * @param event When to toggle the status.
     * @see #getToggleEvent()
     */
    public void toggleWhen(EventSource event) {
        event.addListener(getToggleEvent());
    }

    /**
     * Get an EventConsumer that, when fired, will set the state to true.
     *
     * @return the firable EventConsumer.
     * @see #setTrueWhen(ccre.event.EventSource)
     */
    public EventConsumer getSetTrueEvent() {
        if (setTrue == null) {
            setTrue = new EventConsumer() {
                public void eventFired() {
                    writeValue(true);
                }
            };
        }
        return setTrue;
    }

    /**
     * Get an EventConsumer that, when fired, will set the state to false.
     *
     * @return the firable EventConsumer.
     * @see #setFalseWhen(ccre.event.EventSource)
     */
    public EventConsumer getSetFalseEvent() {
        if (setFalse == null) {
            setFalse = new EventConsumer() {
                public void eventFired() {
                    writeValue(false);
                }
            };
        }
        return setFalse;
    }

    /**
     * Get an EventConsumer that, when fired, will toggle the state.
     *
     * @return the firable EventConsumer.
     * @see #toggleWhen(ccre.event.EventSource)
     */
    public EventConsumer getToggleEvent() {
        if (toggle == null) {
            toggle = new EventConsumer() {
                public void eventFired() {
                    writeValue(!readValue());
                }
            };
        }
        return toggle;
    }

    public synchronized void writeValue(boolean value) {
        if (this.value == value) {
            return;
        }
        this.value = value;
        if (consumers != null) {
            for (BooleanOutput fws : consumers) {
                fws.writeValue(value);
            }
        }
    }

    public synchronized boolean readValue() {
        return value;
    }

    public synchronized void addTarget(BooleanOutput csm) {
        if (consumers == null) {
            consumers = new CArrayList<BooleanOutput>();
        }
        consumers.add(csm);
        csm.writeValue(value);
    }

    public synchronized boolean removeTarget(BooleanOutput consum) {
        if (consumers != null) {
            boolean out = consumers.remove(consum);
            if (consumers.isEmpty()) {
                consumers = null;
            }
            return out;
        } else {
            return false;
        }
    }
}
