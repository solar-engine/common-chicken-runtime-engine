/*
 * Copyright 2014-2016 Cel Skeggs
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

/**
 * A class used to ensure that an output is only set to the correct value and in
 * the correct interval of time - and not anywhen else.
 *
 * Set {@link #valueExpected} to the expected value, let the code run that
 * should update the value, and then call {@link #check()}.
 *
 * If a value is received when valueExpected is null, an exception will be
 * thrown. Note that this also happens if a value is received more than once,
 * because valueExpected is set to null after the first value written.
 *
 * check() will fail if valueExpected is not null, because that means that means
 * that a value was never received.
 *
 * @author skeggsc
 */
public class CountingStringOutput {
    /**
     * The value expected to be received, or null if nothing is expected to be
     * received.
     */
    public String valueExpected;

    private boolean anyUnexpected;

    /**
     * Check <code>value</code> against the expected value.
     *
     * @param value the value to check
     */
    public synchronized void set(String value) {
        if (valueExpected == null) {
            anyUnexpected = true;
            fail("Unexpected set of: " + value);
        }
        if (!valueExpected.equals(value)) {
            anyUnexpected = true;
            fail("Incorrect set: " + value + " rather than " + valueExpected);
        }
        valueExpected = null;
    }

    /**
     * Ensure that the correct value has been received since the last time that
     * valueExpected was set to a value.
     *
     * @throws RuntimeException if a write did not occur.
     */
    public void check() throws RuntimeException {
        assertFalse("Already failed earlier!", anyUnexpected);
        if (valueExpected != null) {
            anyUnexpected = true;
            fail("Did not get expected set of: " + valueExpected);
        }
    }
}
