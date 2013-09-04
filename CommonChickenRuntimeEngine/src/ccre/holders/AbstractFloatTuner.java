package ccre.holders;

import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;
import ccre.cluck.CluckEncoder;
import ccre.cluck.CluckGlobals;
import ccre.util.CArrayList;

/**
 * A RemoteFloatTuner is an interface that allows for easy tuning of a float
 * value, most frequently a FloatStatus.
 *
 * @author skeggsc
 */
public abstract class AbstractFloatTuner implements FloatTuner {
    
    protected CArrayList<FloatOutput> consumers = new CArrayList<FloatOutput>();
    
    protected void notifyConsumers() {
        for (FloatOutput o : consumers) {
            o.writeValue(getCurrentValue());
        }
    }

    /**
     * Fetch the automatic tuning channel (see getNetworkChannelForAutomatic).
     * This is the same as getAutomaticChannel(CluckGlobals.encoder)
     *
     * @return the automatic tuning channel or null if none exists.
     */
    @Override
    public FloatInputProducer getAutomaticChannel() {
        return getAutomaticChannel(CluckGlobals.encoder);
    }

    /**
     * Fetch the automatic tuning channel (see getNetworkChannelForAutomatic)
     * from the given CluckEncoder. As in, subscribe to the FloatInputProducer
     * with the given name.
     *
     * @param encoder the encoder to fetch from.
     * @return the automatic tuning channel or null if none exists.
     */
    @Override
    public FloatInputProducer getAutomaticChannel(CluckEncoder encoder) {
        String cur = getNetworkChannelForAutomatic();
        if (cur == null) {
            return null;
        }
        return encoder.subscribeFloatInputProducer(cur, 0);
    }

    @Override
    public float readValue() {
        return getCurrentValue();
    }

    @Override
    public void addTarget(FloatOutput output) {
        consumers.add(output);
    }

    @Override
    public boolean removeTarget(FloatOutput output) {
        return consumers.remove(output);
    }

    @Override
    public void writeValue(float newValue) {
        tuneTo(newValue);
    }
}
