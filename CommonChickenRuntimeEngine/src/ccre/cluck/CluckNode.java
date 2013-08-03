package ccre.cluck;

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CHashMap;
import ccre.util.CLinkedList;

/**
 * A message router for Cluck. Allows for listeners to subscribe to various
 * channels, and allows sending data to whichever listeners want the data.
 *
 * @author skeggsc
 */
public class CluckNode {

    /**
     * The mapping between channels and the listeners that want to receive data
     * for the channel.
     */
    protected CHashMap<String, CLinkedList<CluckChannelListener>> channels = new CHashMap<String, CLinkedList<CluckChannelListener>>();
    /**
     * The listeners that want to receive all data coming across this node.
     */
    protected CArrayList<CluckChannelListener> wildcarded = new CArrayList<CluckChannelListener>();
    /**
     * The listeners that want to know when a new channel is subscribed to, or
     * when a channel loses all subscribers.
     */
    protected CArrayList<CluckSubscriptionListener> subscriptionListeners = new CArrayList<CluckSubscriptionListener>();

    /**
     * Register a subscripting listener. Whenever a channel receives its first
     * subscription or a channel loses all subscriptions, the listener will be
     * notified. The listener will also be notified right now of all current
     * channels with subscriptions.
     *
     * @param listener the listener to register.
     * @see #unsubscribeFromSubscriptions(ccre.cluck.CluckSubscriptionListener)
     */
    public synchronized void subscribeToSubscriptions(CluckSubscriptionListener listener) {
        subscriptionListeners.add(listener);
        for (String keyname : channels) {
            CLinkedList<CluckChannelListener> cl = channels.get(keyname);
            if (!cl.isEmpty()) {
                listener.addSubscription(keyname);
            }
        }
    }

    /**
     * Removes a listener previously subscribed using subscribeToSubscriptions.
     *
     * @param listener the listener to remove.
     * @see #subscribeToSubscriptions(ccre.cluck.CluckSubscriptionListener)
     */
    public synchronized void unsubscribeFromSubscriptions(CluckSubscriptionListener listener) {
        subscriptionListeners.remove(listener);
    }

    /**
     * Subscribes to the specified channel, or all channels if the specified
     * channel is null. The specified listener will be notified whenever new
     * data is received over the specified channel.
     *
     * @param channel the channel to listen on, or null to listen on all
     * channels.
     * @param listener the listener to notify when data is received.
     * @see #unsubscribe(java.lang.String, ccre.cluck.CluckChannelListener)
     */
    public synchronized void subscribe(String channel, CluckChannelListener listener) {
        if (channel == null) {
            wildcarded.add(listener);
        } else {
            CLinkedList<CluckChannelListener> lsns = channels.get(channel);
            if (lsns == null || lsns.isEmpty()) {
                for (CluckSubscriptionListener c : subscriptionListeners) {
                    c.addSubscription(channel);
                }
            }
            if (lsns == null) {
                lsns = new CLinkedList<CluckChannelListener>();
                channels.put(channel, lsns);
            }
            lsns.addLast(listener);
        }
    }

    /**
     * Removes a listener previously added using subscribe.
     *
     * @param channel the channel to remove from, or null to remove from the
     * channel that receives everything.
     * @param listener the listener to remove.
     * @see #subscribe(java.lang.String, ccre.cluck.CluckChannelListener)
     */
    public synchronized void unsubscribe(String channel, CluckChannelListener listener) {
        if (channel == null) {
            wildcarded.remove(listener);
        } else {
            CLinkedList<CluckChannelListener> lsns = channels.get(channel);
            if (lsns != null) {
                lsns.remove(listener);
                if (lsns.isEmpty()) {
                    for (CluckSubscriptionListener c : subscriptionListeners) {
                        c.removeSubscription(channel);
                    }
                }
            }
        }
    }

    /**
     * Send the given packet of data over the specified channel. All listeners
     * registered on the specified channel or registered on the channel that
     * receives everything will receive the given message.
     *
     * @param channel the channel to send over.
     * @param data the data array to send.
     */
    public synchronized void publish(String channel, byte[] data) {
        publish(channel, data, null);
    }
    private static final byte[] empty = new byte[0];

    /**
     * Sends data to all listeners except for the specified listener. Works like
     * publish(String, byte[]) otherwise.
     *
     * @param channel the channel to send over.
     * @param data the data array to send.
     * @param ignore the listener to not notify.
     * @see #publish(java.lang.String, byte[])
     */
    public synchronized void publish(String channel, byte[] data, CluckChannelListener ignore) {
        if (data == null) {
            data = empty;
        }
        for (CluckChannelListener listener : wildcarded) {
            if (listener == ignore) {
                continue;
            }
            try {
                listener.receive(channel, data);
            } catch (Throwable thr) {
                Logger.log(LogLevel.WARNING, "Throwable during Cluck wildcard publish", thr);
            }
        }
        CLinkedList<CluckChannelListener> chs = channels.get(channel);
        if (chs == null) {
            return;
        }
        for (CluckChannelListener listener : chs) {
            if (listener == ignore) {
                continue;
            }
            try {
                listener.receive(channel, data);
            } catch (Throwable thr) {
                Logger.log(LogLevel.WARNING, "Throwable during Cluck publish: " + channel, thr);
            }
        }
    }
}
