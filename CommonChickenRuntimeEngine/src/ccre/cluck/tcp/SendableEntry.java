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
package ccre.cluck.tcp;

/**
 * Stored in a queue of the messages that need to be sent over a connection.
 * @author skeggsc
 */
public class SendableEntry {

    public String src, dst;
    public byte[] data;

    public SendableEntry(String src, String dst, byte[] data) {
        this.src = src;
        this.dst = dst;
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "[" + src + "->" + dst + "#" + data.length + "]";
    }
}
