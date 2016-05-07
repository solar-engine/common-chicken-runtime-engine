/*
 * Copyright 2013-2014 Cel Skeggs
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

import ccre.verifier.FlowPhase;

/**
 * Collects each line sent on this OutputStream and sends them to a single
 * collector. Does not properly decode strings! Only works with ASCII.
 *
 * (I figured that it was better to be simple, fast, and reliable than correct.)
 *
 * @author skeggsc
 */
public abstract class LineCollectorOutputStream extends OutputStream {

    private byte[] running = new byte[10];
    private int run_i = 0;

    @Override
    public final synchronized void write(int b) throws IOException {
        if (b == '\n') {
            collect(new String(running, 0, run_i, "UTF-8"));
            run_i = 0;
        } else {
            if (run_i >= running.length) {
                byte[] nrun = new byte[running.length * 2];
                System.arraycopy(running, 0, nrun, 0, run_i);
                running = nrun;
            }
            running[run_i++] = (byte) b;
        }
    }

    /**
     * Override this to be called each time a new line is received.
     *
     * @param param The received line.
     */
    @FlowPhase
    protected abstract void collect(String param);
}
