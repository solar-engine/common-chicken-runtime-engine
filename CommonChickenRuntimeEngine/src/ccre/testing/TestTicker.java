/*
 * Copyright 2014 Colby Skeggs and Vincent Miller
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

import ccre.ctrl.Ticker;
import ccre.channel.EventOutput;

/**
 *
 * @author skeggsc
 */
public class TestTicker extends BaseTest {

    @Override
    public String getName() {
        return "Ticker Test";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        final int[] cur = new int[1];
        EventOutput a = new EventOutput() {
            public void event() {
                try {
                    Thread.sleep(15);
                    cur[0]++;
                } catch (InterruptedException ex) {
                    // Ignore it.
                }
            }
        };
        {
            cur[0] = 0;
            Ticker t = new Ticker(19, true);
            try {
                t.send(a);
                Thread.sleep(500);
                assertTrue(24 <= cur[0] && cur[0] <= 26, "Bad Ticker count: " + cur[0]);
            } finally {
                t.terminate();
            }
        }
        {
            cur[0] = 0;
            Ticker t = new Ticker(19, false);
            try {
                t.send(a);
                Thread.sleep(500);
                assertTrue(13 <= cur[0] && cur[0] <= 14, "Bad Ticker count!");
            } finally {
                t.terminate();
            }
        }
    }
}
