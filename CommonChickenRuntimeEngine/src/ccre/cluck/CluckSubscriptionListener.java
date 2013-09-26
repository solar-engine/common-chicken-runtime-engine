/*
 * Copyright 2013 Colby Skeggs
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
