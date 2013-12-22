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
package ccre.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Collects each line sent on this OutputStream and sends them to a single
 * collector. Does not properly decode strings! Only works with ASCII.
 *
 * (I figured that it was better to be simple, fast, and reliable than correct.)
 *
 * @author skeggsc
 */
public abstract class LineCollectorOutputStream extends OutputStream {

    private final StringBuffer running = new StringBuffer();
    
    @Override
    public final void write(int b) throws IOException {
        if (b == '\n') {
            collect(running.toString());
            running.setLength(0);
        } else {
            running.append((char) b);
        }
    }

    protected abstract void collect(String toString);
}
