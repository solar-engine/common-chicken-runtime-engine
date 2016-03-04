/*
 * Copyright 2015 Cel Skeggs.
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
package ccre.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class StorageTest {

    private static final String FILENAME = "unit_testing_test.txt";
    private static final String FILENAME_NONEXIST = "unit_testing_missing.txt";

    @Test
    public void testReadWrite() throws IOException {
        String string = "Testing " + System.currentTimeMillis();
        try (OutputStream out = Storage.openOutput(FILENAME)) {
            out.write((string + "\n").getBytes("UTF-8"));
        }
        try (BufferedReader bread = new BufferedReader(new InputStreamReader(Storage.openInput(FILENAME)))) {
            assertEquals(string, bread.readLine());
            assertNull(bread.readLine());
        }
    }

    @Test
    public void testReadNonexistent() throws IOException {
        assertNull(Storage.openInput(FILENAME_NONEXIST));
    }
}
