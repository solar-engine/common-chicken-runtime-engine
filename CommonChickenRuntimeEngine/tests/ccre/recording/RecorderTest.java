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
package ccre.recording;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.storage.Storage;
import ccre.util.Values;

// TODO: complete this set of tests
@SuppressWarnings("javadoc")
public class RecorderTest {

    @Before
    public void setUp() throws Exception {
        for (String name : Storage.list()) {
            // this behavior ensures that we don't delete anyone else's data
            assertFalse("cannot run a test when there are lingering record files", name.startsWith("rec-") || name.startsWith("TEST"));
        }
    }

    @After
    public void tearDown() throws Exception {
        for (String name : Storage.list()) {
            if (name.startsWith("rec-") || name.startsWith("TEST")) {
                Storage.delete(name);
            }
        }
    }

    @Test
    public void testLUN_Nothing() {
        assertArrayEquals(new int[0], Recorder.listUsedNumbers());
    }

    @Test
    public void testLUN_NothingCorrect() throws IOException {
        populateLUNIncorrect();
        assertArrayEquals(new int[0], Recorder.listUsedNumbers());
    }

    @Test
    public void testLUN_OneCorrect() throws IOException {
        for (int i = 0; i < 100; i++) {
            populateLUNStream(Storage.openOutput("rec-" + i));
            assertArrayEquals(new int[] { i }, Recorder.listUsedNumbers());
            Storage.delete("rec-" + i);
        }
    }

    @Test
    public void testLUN_ManyCorrect() throws IOException {
        int[] out = new int[40];
        int next = 0;
        for (int i = 0; i < out.length; i++) {
            out[i] = next;
            next += 1 + Values.getRandomInt(7);
        }
        for (int i = 0; i < 100; i++) {
            populateLUNStream(Storage.openOutput("rec-" + i));
            assertArrayEquals(new int[] { i }, Recorder.listUsedNumbers());
            Storage.delete("rec-" + i);
        }
    }

    @Test
    public void testLUN_Jumble() throws IOException {
        int[] out = new int[40];
        int next = 0;
        for (int i = 0; i < out.length; i++) {
            out[i] = next;
            populateLUNStream(Storage.openOutput("rec-" + next + (Values.getRandomBoolean() ? ".gz" : "")));
            next += 1 + Values.getRandomInt(7);
        }
        populateLUNIncorrect();
        assertArrayEquals(out, Recorder.listUsedNumbers());
    }

    private void populateLUNStream(OutputStream out) throws IOException {
        try {
            out.write("test\n".getBytes());
        } finally {
            out.close();
        }
    }

    private void populateLUNIncorrect() throws IOException {
        for (String name : Values.getRandomStrings(15)) {
            if (name.matches("(.*[/\r\n\0].*)|(^$)")) {
                continue;
            }
            populateLUNStream(Storage.openOutput("TEST" + name));
        }
        for (String name : Values.getRandomStrings(15)) {
            if (name.matches("(.*[/\r\n\0].*)|(^$)")) {
                continue;
            }
            try {
                Integer.parseInt(name);
                // if it continues, not a name we want.
            } catch (NumberFormatException ex) {
                // we want one of these
                populateLUNStream(Storage.openOutput("rec-" + name + (Values.getRandomBoolean() ? ".gz" : "")));
            }
        }
        populateLUNStream(Storage.openOutput("rec-5a.gz"));
    }

    // TODO: make this a more complete test

    @Test
    public void testOpenStreamUnderstuffed() throws IOException {
        int[] is = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
        assertArrayEquals(new int[0], Recorder.listUsedNumbers());
        for (int i = 1; i <= 8; i++) {
            populateLUNStream(Recorder.openStream(Values.getRandomBoolean(), 8));
            assertArrayEquals(Arrays.copyOf(is, i), Recorder.listUsedNumbers());
        }
    }
    
    @Test
    public void testOpenStreamOverstuffed() throws IOException {
        testOpenStreamUnderstuffed();
        for (int i = 0; i < 10; i++) {
            int[] expected = new int[8];
            for (int j = 0; j < expected.length; j++) {
                expected[j] = j + i;
            }
            assertArrayEquals(expected, Recorder.listUsedNumbers());
            populateLUNStream(Recorder.openStream(Values.getRandomBoolean(), 8));
        }
    }
}
