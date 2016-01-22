/*
 * Copyright 2014-2016 Colby Skeggs
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
package ccre.testing;

import static org.junit.Assert.*;
import ccre.channel.BooleanOutput;

/**
 * A class used to ensure that an output is only set to the correct value and in
 * the correct interval of time - and not anywhen else.
 *
 * Set {@link #valueExpected} to the expected value and {@link #ifExpected} to
 * true, let the code run that should update the value, and then call
 * {@link #check()}.
 *
 * If a value is received when ifExpected is not set, an exception will be
 * thrown. Note that this also happens if a value is received more than once,
 * because ifExpected is cleared after the first value written.
 *
 * check() will fail if ifExpected is still true, because that means that means
 * that the value was never received.
 *
 * @author skeggsc
 */
public class CountingBooleanOutput implements BooleanOutput {
    /**
     * The value expected to be received.
     */
    public boolean valueExpected;
    /**
     * Whether or not we're still expected a value to be received.
     */
    public boolean ifExpected;

    private boolean anyUnexpected;

    public synchronized void set(boolean value) {
        if (!ifExpected) {
            anyUnexpected = true;
            fail("Unexpected set: " + value);
        }
        if (value != valueExpected) {
            anyUnexpected = true;
            fail("Incorrect set: " + value + " instead of " + valueExpected);
        }
        ifExpected = false;
    }

    /**
     * Ensure that the correct value has been received since the last time that
     * ifExpected was set to true.
     *
     * @throws RuntimeException if a write did not occur.
     */
    public void check() throws RuntimeException {
        assertFalse("Already failed earlier!", anyUnexpected);
        if (ifExpected) {
            anyUnexpected = true;
            fail("Did not get expected set of " + valueExpected + "!");
        }
    }
}
