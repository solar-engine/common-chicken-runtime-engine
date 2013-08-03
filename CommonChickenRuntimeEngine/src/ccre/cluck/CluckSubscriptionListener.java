package ccre.cluck;

/**
 * A listener for which channels on a CluckNode have explicit subscriptions.
 *
 * @see CluckNode#subscribeToSubscriptions(ccre.cluck.CluckSubscriptionListener)
 * @author skeggsc
 */
public interface CluckSubscriptionListener {

    /**
     * Called when a channel receives its first subscription.
     *
     * @param key the new channel
     * @see CluckNode#subscribe(java.lang.String,
     * ccre.cluck.CluckChannelListener)
     */
    public void addSubscription(String key);

    /**
     * Called when a channel loses its last subscription.
     *
     * @param key the old channel
     * @see CluckNode#unsubscribe(java.lang.String,
     * ccre.cluck.CluckChannelListener)
     */
    public void removeSubscription(String key);
}
