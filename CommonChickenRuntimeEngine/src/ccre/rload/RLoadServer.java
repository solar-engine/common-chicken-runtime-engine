/*
 * Copyright 2013-2014 Colby Skeggs
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
package ccre.rload;

import ccre.concurrency.ReporterThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import ccre.net.Network;
import ccre.net.ServerSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A server for the RLoad system primarily used by Obsidian projects.
 *
 * @author skeggsc
 */
public class RLoadServer extends ReporterThread {

    /**
     * The magic header used in RLoad.
     */
    static final long MAGIC_HEADER = 0x1540A0413CA6120AL;

    /**
     * The main launching function for an RLoad server.
     *
     * @param args The arguments to the program.
     */
    public static void main(String[] args) {
        if (args.length != 1 && (args.length != 2 || !args[1].equals("-tell-watcher"))) {
            System.err.println("Requires exactly one argument - a target for the file to upload to!");
            System.exit(1);
            return;
        }
        new RLoadServer(args[0], args.length == 2).start();
    }

    static int checksum(byte[] data) {
        int h = data.length;
        for (int i = 0; i < data.length; i++) {
            h = 43 * h + data[i];
        }
        return h;
    }
    private final File output;

    private final boolean watcher;

    /**
     * Create a new instance of an RLoad server that puts received data into a
     * file and optionally notifies watchers.
     *
     * @param targetFile The file to write to.
     * @param watcher If a watcher file should be generated.
     */
    public RLoadServer(String targetFile, boolean watcher) {
        super("RLoadServer");
        this.output = new File(targetFile);
        this.watcher = watcher;
    }

    @Override
    protected void threadBody() throws IOException {
        ServerSocket serv = Network.bind(11540);
        try {
            Logger.info("Started receiving for " + output);
            while (true) {
                ClientSocket clis = serv.accept();
                try {
                    Logger.info("Received client!");
                    handleClient(clis);
                } finally {
                    clis.close();
                }
            }
        } finally {
            serv.close();
        }
    }

    private void handleClient(ClientSocket clis) {
        try {
            DataInputStream din = clis.openDataInputStream();
            DataOutputStream dout = clis.openDataOutputStream();
            dout.writeLong(~MAGIC_HEADER);
            if (din.readLong() != MAGIC_HEADER) {
                throw new IOException("Invalid magic number!");
            }
            int length = din.readInt();
            if (length < 0 || length > 1024 * 1024) {
                throw new IOException("Length out of bounds! (up to 1 MB)");
            }
            byte[] buf = new byte[length];
            din.readFully(buf);
            int checksum = din.readInt();
            if (checksum != checksum(buf)) {
                throw new IOException("Invalid checksum - error while sending! Please retry.");
            }
            FileOutputStream fout = new FileOutputStream(this.output);
            try {
                fout.write(buf);
            } finally {
                fout.close();
            }
            dout.writeInt((int) ((MAGIC_HEADER >> 32) ^ MAGIC_HEADER));
            Logger.info("Finished upload of " + String.format("%.1f", length / 1024.0) + " KB!");
            if (watcher) {
                FileOutputStream watchout = new FileOutputStream("remote-watcher");
                try {
                    watchout.write(String.valueOf(System.currentTimeMillis()).getBytes("UTF-8"));
                    watchout.write('\n');
                } finally {
                    watchout.close();
                }
            }
            Logger.info("Created watcher file.");
        } catch (IOException ex) {
            Logger.severe("Error during client file transfer!", ex);
        }
    }
}
