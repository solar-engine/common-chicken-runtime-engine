/*
 * Copyright 2013-2015 Colby Skeggs
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Enumeration;

import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CCollection;

/**
 * The default network provider. This is chosen by default if no other provider
 * is registered on Network.
 *
 * @see Network
 * @author skeggsc
 */
class DefaultNetworkProvider implements NetworkProvider {

    @SuppressWarnings("resource")
    public ClientSocket openClient(String targetAddress, int port) throws IOException {
        try {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(targetAddress, port), 500); // TODO: What timeout should be used?
            return new ClientSocketImpl(sock);
        } catch (SocketTimeoutException ex) {
            throw new ConnectException("Timed out while connecting."); // Smaller traceback.
        } catch (ConnectException ctc) {
            if (ctc.getMessage().startsWith("Connection timed out")) {
                throw new ConnectException("Timed out while connecting."); // Smaller traceback.
            } else if (ctc.getMessage().startsWith("Connection refused")) {
                throw new ConnectException("Remote server not available."); // Smaller traceback.
            }
            throw ctc;
        }
    }

    public String getPlatformType() {
        return "NetFull";
    }

    @SuppressWarnings("resource")
    public ServerSocket openServer(int port) throws IOException {
        return new ServerSocketImpl(new java.net.ServerSocket(port));
    }

    public CCollection<String> listIPv4Addresses() {
        Enumeration<NetworkInterface> enm;
        try {
            enm = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            Logger.severe("Could not enumerate IP addresses!", ex);
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
                    allAddresses.add(addr.getHostAddress());
                } else if (raw.length != 16) {
                    Logger.warning("Found an address that's not 4 or 16 long: " + Arrays.toString(raw));
                }
            }
        }
        return allAddresses;
    }

    /**
     * Implementation detail.
     */
    private static class ClientSocketImpl implements ClientSocket {

        private final Socket sock;

        ClientSocketImpl(Socket s) {
            sock = s;
        }

        public boolean setSocketTimeout(int millis) throws IOException {
            sock.setSoTimeout(millis);
            return true;
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

    public boolean isTimeoutException(IOException ex) {
        return ex instanceof SocketTimeoutException;
    }
}
