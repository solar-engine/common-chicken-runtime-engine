/*
 * Copyright 2014 Colby Skeggs
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
package ccre.examples.cluck;

import ccre.cluck.CluckNode;
import ccre.cluck.any.CluckNullLink;
import ccre.cluck.rpc.RemoteProcedure;
import ccre.cluck.rpc.SimpleProcedure;
import ccre.log.Logger;

public class RPCTest {

    public static void main(String[] args) throws InterruptedException {
        CluckNode clientNode = new CluckNode();
        CluckNode serverNode = new CluckNode();
        CluckNullLink.connect(clientNode, "server", serverNode, "client");
        // Server
        serverNode.getRPCManager().publish("test-procedure", new SimpleProcedure() {
            @Override
            public byte[] invoke(byte[] in) {
                Logger.info("Server received input: " + new String(in));
                return "RESPONSE".getBytes();
            }
        });
        // Client
        RemoteProcedure proc = clientNode.getRPCManager().subscribe("server/test-procedure", 40);
        byte[] result = SimpleProcedure.invoke(proc, "REQUEST".getBytes(), 50);
        Logger.info("Client received response: " + (result == null ? "TIMEOUT" : new String(result)));
    }
}
