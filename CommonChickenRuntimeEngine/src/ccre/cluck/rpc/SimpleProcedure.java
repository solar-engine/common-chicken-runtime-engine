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
package ccre.cluck.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.time.Time;

/**
 * A simple implementation of RemoteProcedure that takes care of returning
 * results for the implementation.
 *
 * This is not suitable for blocking invocations!
 *
 * Also in this class is a method that will invoke a procedure with specified
 * arguments and return the data from it, to match the simplicity of a
 * SimpleProcedure.
 *
 * @author skeggsc
 */
public abstract class SimpleProcedure implements RemoteProcedure {
    private static Logger logger = LoggerFactory.getLogger(SimpleProcedure.class);

    /**
     * The result returned when a SimpleProcedure call times out.
     */
    public static final Object TIMED_OUT = null;

    /**
     * Invoke the specified RemoteProcedure with the specified byte array and
     * return the result, or null (TIMED_OUT) if the request times out.
     *
     * @param rp The procedure to invoke.
     * @param in The input to pass it.
     * @param timeout The maximum number of milliseconds to wait. (Note: zero
     * means don't wait for a result, not wait indefinitely.)
     * @return The output from the procedure, or null if the request times out.
     * @throws java.lang.InterruptedException If the current thread is
     * interrupted while waiting for a response.
     * @see #TIMED_OUT
     */
    public static byte[] invoke(RemoteProcedure rp, byte[] in, int timeout) throws InterruptedException {
        final boolean[] b = new boolean[1];
        final Object invk = new Object();
        ByteArrayOutputStream out = new ByteArrayOutputStream() {
            @Override
            public void close() {
                synchronized (invk) {
                    b[0] = true;
                    invk.notifyAll();
                }
            }
        };
        rp.invoke(in, out);
        if (!b[0]) {
            synchronized (invk) {
                long endAt = Time.currentTimeMillis() + timeout;
                while (!b[0]) {
                    long now = Time.currentTimeMillis();
                    if (now >= endAt) {
                        break;
                    }
                    Time.wait(invk, endAt - now);
                }
            }
            if (!b[0]) {
                return (byte[]) TIMED_OUT;
            }
        }
        return out.toByteArray();
    }

    public final void invoke(byte[] in, OutputStream out) {
        try {
            out.write(invoke(in));
            out.close();
        } catch (IOException ex) {
            logger.warn("IO Exception during response from SimpleProcedure!", ex);
        }
    }

    /**
     * Implement this to implement a non-blocking procedure, that reads in data
     * from the argument and returns data to give to the sender.
     *
     * @param in The inputs to the procedure.
     * @return The results from the procedure.
     */
    protected abstract byte[] invoke(byte[] in);
}
