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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UniqueIdsTest {

    UniqueIds local;

    @Before
    public void setUp() throws Exception {
        local = new UniqueIds();
    }

    @Test
    public void testNextId() {
        for (int i = 0; i < 3000; i++) {
            assertEquals(i, local.nextId());
        }
    }

    @Test
    public void testNextHexId() {
        for (int i = 0; i < 3000; i++) {
            assertEquals(i, Integer.parseInt(local.nextHexId(), 16));
        }
    }

    @Test
    public void testNextHexIdString() {
        for (int i = 0; i < 3000; i++) {
            String k = local.nextHexId("test!");
            assertTrue(k.startsWith("test!-"));
            assertEquals(i, Integer.parseInt(k.substring(6), 16));
        }
    }

    @Test
    public void testNextHexIdStringRandom() {
        for (int i = 0; i < 3000; i++) {
            String str = Values.getRandomString();
            String k = local.nextHexId(str);
            assertTrue(k.startsWith(str));
            assertEquals(k.charAt(str.length()), '-');
            assertEquals(i, Integer.parseInt(k.substring(str.length() + 1), 16));
        }
    }

}
