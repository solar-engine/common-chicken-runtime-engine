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
package ccre.cluck3;

public class CluckGlobals {

    public static CluckNode node;
    public static CluckTCPServer serv;
    public static CluckTCPClient cli;

    public static void ensureInitted() {
        if (node == null) {
            node = new CluckNode();
        }
    }

    public static void setupServer() {
        if (serv != null) {
            throw new IllegalStateException("Server already set up!");
        }
        serv = new CluckTCPServer(node);
        serv.start();
    }

    public static void setupClient(String remote, String linkName, String hintedRemoteName) {
        if (cli != null) {
            throw new IllegalStateException("Client already set up!");
        }
        cli = new CluckTCPClient(remote, node, linkName, hintedRemoteName);
        cli.start();
    }
}
