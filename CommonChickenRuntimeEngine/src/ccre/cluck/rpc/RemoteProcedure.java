/*
 * Copyright 2014-2015 Cel Skeggs
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

import java.io.OutputStream;

import ccre.verifier.SetupPhase;

/**
 * A procedure that can be published over Cluck to allow for calling it across
 * the network.
 *
 * Usually SimpleProcedure is a good way to implement and invoke these.
 *
 * @author skeggsc
 * @see SimpleProcedure
 */
public interface RemoteProcedure {

    /**
     * Run this procedure with data from the specified input and put the results
     * into the specified output. The invoke method itself should not block, but
     * a procedure may delay responding for a small amount of time, as long as
     * invoke itself returns quickly. (This implies that a different thread
     * should be used.)
     *
     * Replies work by writing results to the output, and then closing the
     * output.
     *
     * @param in Inputs to the procedure.
     * @param out Results from the procedure. Send the result by closing this
     * stream.
     */
    @SetupPhase
    public void invoke(byte[] in, OutputStream out);
}
