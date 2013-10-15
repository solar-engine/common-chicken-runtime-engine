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

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CHashMap;

public class CluckNode {

    public CHashMap<String, CluckLink> links = new CHashMap<String, CluckLink>();

    public void transmit(String target, String source, byte[] data) {
        if (target == null) {
            Logger.log(LogLevel.WARNING, "Received message addressed to unreceving node (source: " + source + ")", new Exception("Embedded Traceback"));
            return;
        }
        int t = target.indexOf('/');
        String base, rest;
        if (t == -1) {
            base = target;
            rest = null;
        } else {
            base = target.substring(0, t);
            rest = target.substring(t+1);
        }
        CluckLink link = links.get(base);
        if (!link.transmit(rest, source, data)) {
            links.put(base, null);
        }
    }

    public String getLinkName(CluckNullLink link) {
        if (link == null) {
            throw new NullPointerException();
        }
        for (String key : links) {
            if (links.get(key) == link) {
                return key;
            }
        }
        throw new RuntimeException("No such link!");
    }

    public void addLink(CluckLink link, String linkName) {
        if (links.get(linkName) != null) {
            throw new IllegalStateException("Link name already used!");
        }
        links.put(linkName, link);
    }
    
    public void addOrReplaceLink(CluckLink link, String linkName) {
        if (links.get(linkName) != null) {
            Logger.fine("Replaced current link on: " + linkName);
        }
        links.put(linkName, link);
    }
}
