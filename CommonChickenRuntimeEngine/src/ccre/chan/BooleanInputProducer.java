package ccre.chan;

/**
 * A BooleanInputProducer is a way to subscribe to notifications of changes in a
 * boolean input's value.
 *
 * @see BooleanOutput
 * @author skeggsc
 */
public interface BooleanInputProducer {

    /**
     * Subscribe to changes in this boolean input's value. The boolean output
     * will be modified whenever the value of this input changes.
     *
     * @param output The boolean output to notify when the value changes.
     * @see BooleanOutput#writeValue(boolean) 
     * @see #removeTarget(ccre.chan.BooleanOutput)
     */
    public void addTarget(BooleanOutput output);

    /**
     * Unsubscribe from changes in this boolean input's value. This reverses the
     * actions of a previous addTarget call.
     *
     * @param output The output to unsubscribe.
     * @return Whether the output was actually subscribed.
     * @see #addTarget(ccre.chan.BooleanOutput)
     */
    public boolean removeTarget(BooleanOutput output);
}
