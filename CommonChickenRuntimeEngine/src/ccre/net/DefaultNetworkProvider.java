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
package ccre.net;

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CCollection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * The default network provider. This is chosen by default if no other provider
 * is registered on Network.
 *
 * @see Network
 * @author skeggsc
 */
public class DefaultNetworkProvider implements Network.Provider {

    public ClientSocket openClient(String targetAddress, int port) throws IOException {
        try {
            return new ClientSocketImpl(new Socket(targetAddress, port));
        } catch (ConnectException ctc) {
            if (ctc.getMessage().equals("Connection timed out: connect")) {
                throw new ConnectException("Timed out while connecting."); // Smaller traceback.
            }
            throw ctc;
        }
    }

    public String getPlatformType() {
        return "NetFull";
    }

    /**
     * Implementation detail.
     */
    private static class ClientSocketImpl implements ClientSocket {

        private final Socket sock;

        ClientSocketImpl(Socket s) {
            sock = s;
        }

        public InputStream openInputStream() throws IOException {
            return sock.getInputStream();
        }

        public OutputStream openOutputStream() throws IOException {
            return sock.getOutputStream();
        }

        public DataInputStream openDataInputStream() throws IOException {
            return new DataInputStream(openInputStream());
        }

        public DataOutputStream openDataOutputStream() throws IOException {
            return new DataOutputStream(openOutputStream());
        }

        public void close() throws IOException {
            sock.close();
        }
    }

    public ServerSocket openServer(int port) throws IOException {
        return new ServerSocketImpl(new java.net.ServerSocket(port));
    }

    /**
     * Implementation detail.
     */
    private static class ServerSocketImpl implements ServerSocket {

        private final java.net.ServerSocket sock;

        ServerSocketImpl(java.net.ServerSocket ss) {
            sock = ss;
        }

        public ClientSocket accept() throws IOException {
            return new ClientSocketImpl(sock.accept());
        }

        public void close() throws IOException {
            sock.close();
        }
    }

    public CCollection<String> listIPv4Addresses() {
        Enumeration<NetworkInterface> enm;
        try {
            enm = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            Logger.log(LogLevel.SEVERE, "Could not enumerate IP addresses!", ex);
            return CArrayUtils.getEmptyList();
        }
        if (enm == null) {
            return CArrayUtils.getEmptyList();
        }
        CArrayList<String> allAddresses = new CArrayList<String>();
        while (enm.hasMoreElements()) {
            NetworkInterface ni = enm.nextElement();
            Enumeration<InetAddress> ins = ni.getInetAddresses();
            while (ins.hasMoreElements()) {
                InetAddress addr = ins.nextElement();
                byte[] raw = addr.getAddress();
                if (raw.length == 4) {
                    allAddresses.add(((Inet4Address) addr).getHostAddress());
                } else if (raw.length != 16) {
                    Logger.warning("Found an address that's not 4 or 16 long: " + Arrays.toString(raw));
                }
            }
        }
        return allAddresses;
    }
}
