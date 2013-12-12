/*
 * Copyright 2013 Vincent Miller
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
package ccre.obsidian.comms;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;

/**
 *
 * @author MillerV
 */
public class XBeeRadio {

    private XBee xbee;
    private String port;
    private int baudRate;
    private long[] timeouts;
    private XBeeRequest[] messages;
    private final boolean verified;

    public XBeeRadio(String port, int baudRate, boolean verified) {
        this.port = port;
        this.baudRate = baudRate;
        this.xbee = new XBee();
        this.verified = verified;
    }

    public void modify(String port, int baudRate) throws XBeeException {
        close();
        this.port = port;
        this.baudRate = baudRate;
        open();
    }

    public void open() throws XBeeException {
        xbee.open(port, baudRate);
    }

    public void close() {
        xbee.close();
    }
    
    public void sendPacket(int[] addr, int[] msg, int subTimeout, int timeout) throws XBeeException {
        if (verified) {
            sendPacketVerified(addr, msg, subTimeout, timeout);
        } else {
            sendPacketUnverified(addr, msg);
        }
    }
    
    public void sendPacketUnverified(int[] addr, int[] msg) throws XBeeException {
        XBeeAddress64 address = new XBeeAddress64(addr);
        ZNetTxRequest message = new ZNetTxRequest(address, msg);
        xbee.sendAsynchronous(message);
    }
    
    public void sendPacketVerified(int[] addr, int[] msg, int subTimeout, int timeout) throws XBeeException {
        ZNetTxStatusResponse response = null;

        long startTime = System.currentTimeMillis();

        while (true) {
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new XBeeTimeoutException();
            }

            XBeeAddress64 address = new XBeeAddress64(addr);
            ZNetTxRequest message = new ZNetTxRequest(address, msg);

            try {
                response = (ZNetTxStatusResponse) xbee.sendSynchronous(message, subTimeout);

                if (response.isSuccess()) {
                    return;
                } else {
                    System.out.println("Had to resend message");
                }
            } catch (XBeeTimeoutException e) {
            }
        }
    }

    public XBeeResponse recievePacket(int timeout) throws XBeeException, XBeeTimeoutException {
        return xbee.getResponse(timeout);
    }

    public XBeeResponse recievePacket() throws XBeeException {
        return xbee.getResponse();
    }

    public void addPacketListener(PacketListener listener) {
        xbee.addPacketListener(listener);
    }
}
