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

import ccre.util.UniqueIds;

/**
 * Tests the UniqueIds class.
 * 
 * @author skeggsc
 */
public class TestUniqueIds extends BaseTest {

    @Override
    public String getName() {
        return "UniqueIds";
    }

    @Override
    protected void runTest() throws Throwable {
        UniqueIds local = new UniqueIds();
        assertObjectEqual(local.nextHexId(), "0", "bad id");
        assertObjectEqual(local.nextId(), 1, "bad id");
        assertObjectEqual(local.nextHexId(), "2", "bad id");
        assertObjectEqual(local.nextHexId("prefix"), "prefix-3", "bad id");
        assertObjectEqual(local.nextHexId(), "4", "bad id");
        assertObjectEqual(local.nextHexId("other-"), "other--5", "bad id");
        assertObjectEqual(local.nextHexId("other\0-\0"), "other\0-\0-6", "bad id");
        assertObjectEqual(local.nextHexId(), "7", "bad id");
        assertObjectEqual(local.nextHexId(), "8", "bad id");
        assertObjectEqual(local.nextHexId(), "9", "bad id");
        assertObjectEqual(local.nextHexId(), "a", "bad id");
        assertObjectEqual(local.nextHexId(), "b", "bad id");
        assertObjectEqual(local.nextHexId(), "c", "bad id");
        assertObjectEqual(local.nextId(), 13, "bad id");
        assertObjectEqual(local.nextHexId(), "e", "bad id");
        assertObjectEqual(local.nextHexId(), "f", "bad id");
        assertObjectEqual(local.nextHexId(), "10", "bad id");
        assertObjectEqual(local.nextHexId(), "11", "bad id");
        for (int i=0x12; i<0x4A; i++) {
            assertIntsEqual(local.nextId(), i, "bad id");
        }
        assertObjectEqual(local.nextHexId(), "4a", "bad id");
        for (int i=0x4B; i<0xDC; i++) {
            assertIntsEqual(local.nextId(), i, "bad id");
        }
        assertObjectEqual(local.nextHexId("000"), "000-dc", "bad id");
        for (int i=0xDD; i<0xFF; i++) {
            assertIntsEqual(local.nextId(), i, "bad id");
        }
        assertObjectEqual(local.nextHexId(), "ff", "bad id");
        assertObjectEqual(local.nextHexId(), "100", "bad id");
        assertObjectEqual(local.nextHexId(), "101", "bad id");
        assertObjectEqual(local.nextHexId("-102"), "-102-102", "bad id");
        
        synchronized (UniqueIds.global) {
            for (int i=0; i<1000; i++) {
                assertIntsEqual(UniqueIds.global.nextId() + 1, UniqueIds.global.nextId(), "bad global id");
            }
        }
    }
}
