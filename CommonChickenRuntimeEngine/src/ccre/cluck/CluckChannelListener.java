package ccre.cluck;

/**
 * A listener for data sent over a Cluck channel. Can be registered with a
 * specific channel name or with all channels.
 *
 * @see CluckNode
 * @author skeggsc
 */
public interface CluckChannelListener {

    /**
     * Called when data has been received on a specific channel. Should not
     * modify data because it is the same array sent to all instances of this
     * function.
     *
     * @param channel The channel that the data was received on.
     * @param data The data that was received.
     */
    public void receive(String channel, byte[] data);
}
