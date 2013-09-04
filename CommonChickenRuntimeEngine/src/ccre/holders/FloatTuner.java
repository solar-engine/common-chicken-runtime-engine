package ccre.holders;

import ccre.chan.FloatInput;
import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;
import ccre.cluck.CluckEncoder;

public interface FloatTuner extends FloatInput, FloatOutput {

    /**
     * Fetch the automatic tuning channel (see getNetworkChannelForAutomatic).
     * This is the same as getAutomaticChannel(CluckGlobals.encoder)
     *
     * @return the automatic tuning channel or null if none exists.
     */
    FloatInputProducer getAutomaticChannel();

    /**
     * Fetch the automatic tuning channel (see getNetworkChannelForAutomatic)
     * from the given CluckEncoder. As in, subscribe to the FloatInputProducer
     * with the given name.
     *
     * @param encoder the encoder to fetch from.
     * @return the automatic tuning channel or null if none exists.
     */
    FloatInputProducer getAutomaticChannel(CluckEncoder encoder);

    /**
     * Get the current value for the Tuner, or null if it is unknown.
     *
     * @return the current value or null if it is unknown.
     */
    Float getCurrentValue();

    /**
     * Get the channel name for the network channel that contains the value to
     * set this to for automatic tuning. If one is not provided, null will be
     * returned.
     *
     * @return the automatic tuning channel or null if none exists
     * @see #getAutomaticChannel()
     * @see #getAutomaticChannel(ccre.cluck.CluckEncoder)
     */
    String getNetworkChannelForAutomatic();

    /**
     * Change the current value to the specified value.
     *
     * @param newValue The new value to have.
     */
    void tuneTo(float newValue);
}
