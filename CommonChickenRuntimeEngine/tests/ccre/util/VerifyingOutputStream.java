/*
 * Copyright 2016 Cel Skeggs
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public final class VerifyingOutputStream extends OutputStream {

    public boolean errored = false;
    public byte[] bytesExpected;
    public int indexExpected = -1;

    @Override
    public void write(int b) throws IOException {
        try {
            assertTrue("Not expected!", indexExpected >= 0);
            assertFalse("Unexpected bytes!", indexExpected >= bytesExpected.length);
            assertEquals((byte) b, bytesExpected[indexExpected++]);
        } catch (Throwable thr) {
            errored = true;
            throw thr;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            assertTrue(off >= 0);
            assertTrue(len >= 0);
            assertTrue(off + len <= b.length);
            assertTrue("Not expected!", indexExpected >= 0);
            assertFalse("Unexpected bytes!", indexExpected + len > bytesExpected.length);
            assertArrayEquals(Arrays.copyOfRange(b, off, off + len), Arrays.copyOfRange(bytesExpected, indexExpected, indexExpected + len));
            indexExpected += len;
        } catch (Throwable thr) {
            errored = true;
            throw thr;
        }
    }

    public void check() {
        assertFalse("Did not receive expected event.", indexExpected >= 0 && indexExpected != bytesExpected.length);
        assertFalse("Failed during individual fire.", errored);
    }
}