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
package ccre.testing;

import java.io.IOException;

import ccre.util.LineCollectorOutputStream;

/**
 * Test LineCollectorOutputStream
 * 
 * @author skeggsc
 */
public class TestLineCollectorOutputStream extends BaseTest {

    @Override
    public String getName() {
        return "LineCollectorOutputStream";
    }
    
    private int count = 0, lastCount = 0;
    private String lastLine = null;
    
    private final LineCollectorOutputStream stream = new LineCollectorOutputStream() {
        @Override
        protected void collect(String param) {
            count++;
            lastLine = param;
        }
    };
    
    private void check(String shouldBe) throws TestingException {
        assertIntsEqual(count, ++lastCount, "wrong number of new entries");
        assertObjectEqual(shouldBe, lastLine, "bad most recent entry");
    }
    
    private void fullCheck(String test) throws TestingException, IOException {
        stream.write((test + "\n").getBytes());
        check(test);
        stream.write(test.getBytes());
        stream.flush();
        assertIntsEqual(count, lastCount, "partial result");
        stream.write('\n');
        check(test);
        stream.write(test.getBytes());
        stream.write(test.getBytes());
        stream.write('\n');
        check(test + test);
    }

    @Override
    protected void runTest() throws Throwable {
        stream.write('\n');
        check("");
        for (String str : new String[] {"", "orange", "borange", "mountain", "embedded\0nulls", "same spaces and such", "a carriage\rreturn or\rtwo", "and some\ttab characters", "11111111111111111111"}) {
            fullCheck(str);
        }
    }

}
