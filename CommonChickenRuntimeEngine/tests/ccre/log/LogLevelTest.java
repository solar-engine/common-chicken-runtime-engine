/*
 * Copyright 2016 Colby Skeggs
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
package ccre.log;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class LogLevelTest {

    @Test
    public void testLevelIDs() {
        // Why is this part of the tests? Shouldn't it be allowed to change?
        // No. It's used in the network protocol, and changing it would mess up
        // Cluck logging.
        assertEquals(LogLevel.FINEST.id, -9);
        assertEquals(LogLevel.FINER.id, -6);
        assertEquals(LogLevel.FINE.id, -3);
        assertEquals(LogLevel.CONFIG.id, 0);
        assertEquals(LogLevel.INFO.id, 3);
        assertEquals(LogLevel.WARNING.id, 6);
        assertEquals(LogLevel.SEVERE.id, 9);
    }

    @Test
    public void testLevels() {
        // loop through the bytes
        Iterator<LogLevel> levels = LogLevel.allLevels.iterator();
        for (int i = -9; i <= 9; i += 3) {
            assertTrue(levels.hasNext());
            assertEquals(levels.next().id, i);
        }
        assertFalse(levels.hasNext());
    }

    @Test
    public void testFromByte() {
        for (LogLevel l : LogLevel.allLevels) {
            assertTrue(l == LogLevel.fromByte(l.id));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromByteBad1() {
        LogLevel.fromByte((byte) (LogLevel.FINEST.id - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromByteBad2() {
        LogLevel.fromByte((byte) (LogLevel.SEVERE.id + 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromByteBad3() {
        LogLevel.fromByte((byte) (LogLevel.INFO.id + 1));
    }

    @Test
    public void testToByte() {
        for (LogLevel level : LogLevel.allLevels) {
            assertEquals(LogLevel.toByte(level), level.id);
        }
    }

    @Test
    public void testAtLeastAsImportant() {
        assertTrue(LogLevel.WARNING.atLeastAsImportant(LogLevel.INFO));
        assertTrue(LogLevel.WARNING.atLeastAsImportant(LogLevel.WARNING));
        assertFalse(LogLevel.WARNING.atLeastAsImportant(LogLevel.SEVERE));
    }

    @Test
    public void testToString() {
        ArrayList<LogLevel> a = new ArrayList<>();
        for (LogLevel l : LogLevel.allLevels) {
            a.add(l);
        }
        assertEquals(a.toString(), "[FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE]");
    }

    @Test
    public void testNext() {
        assertEquals(LogLevel.INFO.next(), LogLevel.WARNING);
        assertEquals(LogLevel.SEVERE.next(), LogLevel.FINEST);
        Iterator<LogLevel> iterator = LogLevel.allLevels.iterator();
        LogLevel last = iterator.next();
        while (iterator.hasNext()) {
            LogLevel l = iterator.next();
            assertEquals(last.next(), l);
            last = l;
        }
    }
}
