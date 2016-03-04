/*
 * Copyright 2015 Cel Skeggs
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
package ccre.cluck;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

final class VerifyingCluckLink implements CluckLink {

    public String expectedSource, expectedDestination;
    public byte[] expectedMessage;
    public boolean keepAlive = true;
    public boolean errored = false;
    public boolean ifExpected;

    @Override
    public boolean send(String dest, String source, byte[] data) {
        try {
            assertTrue("Not expected!", ifExpected);
            assertEquals(expectedDestination, dest);
            assertEquals(expectedSource, source);
            assertArrayEquals(expectedMessage, data);
            ifExpected = false;
        } catch (Throwable thr) {
            errored = true;
            throw thr;
        }
        return keepAlive;
    }

    public void check() {
        assertFalse("Did not receive expected event.", ifExpected);
        assertFalse("Failed during individual fire.", errored);
    }
}