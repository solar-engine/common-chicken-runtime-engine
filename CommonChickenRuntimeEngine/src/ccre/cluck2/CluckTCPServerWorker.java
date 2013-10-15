/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.cluck2;

import ccre.concurrency.ReporterThread;
import ccre.log.Logger;
import ccre.net.ClientSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author skeggsc
 */
public class CluckTCPServerWorker extends ReporterThread {

    private final ClientSocket cli;
    private final CluckModule serving;
    private DataInputStream din;
    private DataOutputStream dout;
    private final Object outLock = new Object();
    
    protected class ConnLine implements CluckConnectionReceiver {

        public CluckConnection active;
        public final int id;
        
        public ConnLine(int id) {
            this.id = id;
        }

        public void receive() throws IOException {
            // Use din
            if (active == null) {
                String remote = din.readUTF();
                CluckConnection conn;
                try {
                    conn = serving.connect(remote);
                } catch (IOException ex) {
                    synchronized (outLock) {
                        dout.writeShort(id);
                        dout.writeBoolean(false);
                        dout.writeUTF(ex.getMessage());
                    }
                    return;
                }
                synchronized (outLock) {
                    dout.writeShort(id);
                    dout.writeBoolean(true);
                }
                active = conn;
                conn.setConnectionReceiver(this);
            } else {
                byte[] b = new byte[din.readInt()];
                din.readFully(b);
                active.send(b);
            }
        }

        public void recv(CluckConnection source, byte[] cur) {
            if (active == source) {
                synchronized (outLock) {
                    dout.writeShort(id);
                    dout.writeInt(cur.length);
                    dout.write(cur);
                }
            }
        }
    }

    public CluckTCPServerWorker(CluckModule serving, ClientSocket cli) {
        super("CluckTCPServer-Worker");
        this.cli = cli;
        this.serving = serving;
    }

    @Override
    protected void threadBody() throws Throwable {
        din = cli.openDataInputStream();
        dout = cli.openDataOutputStream();
        dout.writeInt(0x1540CA24);
        if (din.readInt() != 0x1540CA42) {
            Logger.warning("Remote gave wrong magic number!");
            return;
        }
        ArrayList<ConnLine> lines = new ArrayList<ConnLine>();
        while (true) {
            short b = din.readShort();
            if (b >= 0 && b < lines.size()) {
                lines.get(b).receive();
            } else if (b == lines.size()) {
                ConnLine conn = new ConnLine(b);
                lines.add(conn);
                conn.receive();
                // Add new line
            } else {
                throw new IOException("Remote end gave invalid line number!");
            }
        }
    }
}
