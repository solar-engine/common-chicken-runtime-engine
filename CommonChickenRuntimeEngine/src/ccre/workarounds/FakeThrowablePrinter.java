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
package ccre.workarounds;

import java.io.PrintStream;

/**
 * A fake throwable printer for when nothing better is available. This just
 * prints out the result of .toString() on the Throwable object.
 *
 * @author skeggsc
 */
public class FakeThrowablePrinter extends ThrowablePrinter {

    @Override
    public void send(Throwable thr, PrintStream pstr) {
        pstr.println(thr);
    }
}
