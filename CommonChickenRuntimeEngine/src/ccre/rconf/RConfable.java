package ccre.rconf;

/**
 * An RConfable is a network endpoint that can be accessed remotely by an RConf
 * client.
 * 
 * @see ccre.rconf.RConf for more design info.
 * @author skeggsc
 */
public interface RConfable {
    /**
     * Query this RConfable for its status information.
     * 
     * This request may time out, in which case 'null' will be returned.
     * 
     * @return the list of entries to display for this RConfable, or null if the
     * request timed out.
     * @throws InterruptedException if the thread was interrupted while waiting
     * for a response.
     */
    public RConf.Entry[] queryRConf() throws InterruptedException;

    /**
     * Send a notification to this RConfable for one of its entries.
     * 
     * This request may time out, in which case false will be returned.
     * 
     * False may also be returned if the request was rejected or invalid for
     * some reason, but true may also be returned in these cases.
     * 
     * @param field the field index (from the array returned by queryRConf) of
     * the field to notify.
     * @param data the request data to modify the field by.
     * @return if the request was received successfully, and possibly processed
     * successfully.
     * @throws InterruptedException if the thread was interrupted while waiting
     * for the request to complete.
     */
    public boolean signalRConf(int field, byte[] data) throws InterruptedException;
}
