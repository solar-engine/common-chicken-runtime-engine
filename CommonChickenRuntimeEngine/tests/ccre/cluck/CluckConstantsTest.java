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
package ccre.cluck;

import static org.junit.Assert.*;

import org.junit.Test;

public class CluckConstantsTest {

    // There's not much to test in this class, so we just check for off-by-one
    // errors and unknown handling.

    @Test
    public void testRmtToStringFirst() {
        assertEquals(CluckConstants.RMT_PING, 0);
        assertEquals(CluckConstants.rmtToString(0), "Ping");
    }

    @Test
    public void testRmtToStringLast() {
        assertEquals(CluckConstants.COUNT_RMTS - 1, CluckConstants.RMT_LEGACY_FLOATINPUT_UNSUB);
        assertEquals(CluckConstants.rmtToString(CluckConstants.RMT_LEGACY_FLOATINPUT_UNSUB), "LEGACY_FloatInputUnsubscription");
    }

    @Test
    public void testRmtToStringNoUnknowns() {
        for (int i = 0; i < CluckConstants.COUNT_RMTS; i++) {
            assertFalse(CluckConstants.rmtToString(i).toLowerCase().contains("unknown"));
        }
    }

    @Test
    public void testRmtToStringUnknownsNegative() {
        for (int i = -500; i < 0; i++) {
            assertTrue(CluckConstants.rmtToString(i).toLowerCase().contains("unknown"));
        }
    }
    
    @Test
    public void testRmtToStringUnknownsLarge() {
        for (int i = CluckConstants.COUNT_RMTS; i < 500; i++) {
            assertTrue(CluckConstants.rmtToString(i).toLowerCase().contains("unknown"));
        }
    }

    @Test
    public void testCorrectBroadcast() {
        assertEquals("*", CluckConstants.BROADCAST_DESTINATION);
    }
}
