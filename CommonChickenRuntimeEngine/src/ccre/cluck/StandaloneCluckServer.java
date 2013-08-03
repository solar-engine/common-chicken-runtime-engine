package ccre.cluck;

import ccre.event.EventConsumer;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.MultiTargetLogger;
import ccre.net.Network;
import java.io.IOException;

/**
 * A simple standalone cluck server for testing.
 *
 * @author skeggsc
 */
public class StandaloneCluckServer {

    /**
     * Start the simple server.
     *
     * @param args The program arguments. These are currently ignored.
     */
    public static void main(String[] args) {
        final long time = System.currentTimeMillis();
        CluckGlobals.ensureInitializedCore();
        Logger.target = new MultiTargetLogger(CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger"), Logger.target);
        CluckGlobals.encoder.publishEventConsumer("status-report", new EventConsumer() {
            public void eventFired() {
                StringBuilder b = new StringBuilder("Standalone server online on [");
                for (String addr : Network.listIPv4Addresses()) {
                    b.append(addr).append(", ");
                }
                b.setLength(b.length() - 2);
                Logger.info(b.append("] - uptime ").append((System.currentTimeMillis() - time) / 1000).append(" seconds.").toString());
            }
        });
        try {
            CluckGlobals.initializeServer(80);
        } catch (IOException ex) {
            Logger.log(LogLevel.SEVERE, "Could not start Cluck server!", ex);
            return;
        }
        Logger.info("Server is running.");
        CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger").log(LogLevel.INFO, "Remote logging appears to work!", (Throwable) null);
    }
}
