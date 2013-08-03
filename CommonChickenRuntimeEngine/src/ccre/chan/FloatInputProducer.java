package ccre.chan;

/**
 * A FloatInputProducer is a way to subscribe to notifications of changes in a
 * float input's value.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @see FloatOutput
 * @author skeggsc
 */
public interface FloatInputProducer {

    /**
     * Subscribe to changes in this float input's value. The float output will
     * be modified whenever the value of this input changes.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @param output The float output to notify when the value changes.
     * @see FloatOutput#writeValue(float)
     * @see #removeTarget(ccre.chan.FloatOutput)
     */
    public void addTarget(FloatOutput output);

    /**
     * Unsubscribe from changes in this float input's value. This reverses the
     * actions of a previous addTarget call.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @param output The output to unsubscribe.
     * @return Whether the output was actually subscribed.
     * @see #addTarget(ccre.chan.FloatOutput)
     */
    public boolean removeTarget(FloatOutput output);
}
