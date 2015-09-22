/*
 * Copyright 2015 Colby Skeggs
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class LineCollectorOutputStreamTest {

    private LineCollectorOutputStream stream;

    private int count = 0, lastCount = 0;
    private String lastLine = null;

    @Before
    public void setUp() throws Exception {
        count = lastCount = 0;
        lastLine = null;
        stream = new LineCollectorOutputStream() {
            @Override
            protected void collect(String param) {
                count++;
                lastLine = param;
            }
        };
    }

    private void check(String shouldBe) {
        assertEquals(count, ++lastCount);
        assertEquals(shouldBe, lastLine);
    }

    @Test
    public void testSimpleUsage() throws IOException {
        stream.write(("hello world\n").getBytes());
        check("hello world");
    }

    @Test
    public void testPartialResult() throws IOException {
        stream.write("hello world".getBytes());
        stream.flush();
        assertEquals(count, lastCount); // nothing yet
        stream.write('\n');
        check("hello world");
    }

    @Test
    public void testStrangeStrings() throws IOException {
        for (int i = 0; i < 3000; i++) {
            String str = Values.getRandomString();
            if (str.contains("\n")) {
                continue;
            }
            stream.write((str + "\n").getBytes());
            check(str);
        }
    }

}
