/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.util.AllocationPool;
import ccre.util.CArrayList;

/**
 * Tests the allocation pool.
 *
 * @author skeggsc
 */
public class TestAllocationPool extends BaseTest {

    @Override
    public String getName() {
        return "AllocationPool test";
    }

    @Override
    protected void runTest() throws TestingException {
        final CArrayList<Object> alloced = new CArrayList<Object>();
        AllocationPool<Object> aop = new ReportingPool(alloced);
        assertIntsEqual(0, alloced.size(), "Should have different number of allocations!");
        Object o1 = aop.alloc();
        assertIntsEqual(1, alloced.size(), "Should have different number of allocations!");
        assertObjectEqual(o1, alloced.get(0), "Bad allocated object!");
        Object o2 = aop.alloc();
        assertIntsEqual(2, alloced.size(), "Should have different number of allocations!");
        assertObjectEqual(o2, alloced.get(1), "Bad allocated object!");
        Object o3 = aop.alloc();
        assertIntsEqual(3, alloced.size(), "Should have different number of allocations!");
        assertObjectEqual(o3, alloced.get(2), "Bad allocated object!");
        aop.release(o2);
        assertIntsEqual(3, alloced.size(), "Should have different number of allocations!");
        assertObjectEqual(o2, aop.alloc(), "Bad allocated object!");
        aop.release(o1);
        aop.release(o3);
        assertIntsEqual(3, alloced.size(), "Should have different number of allocations!");
        Object o13 = aop.alloc();
        Object o31 = aop.alloc();
        assertIntsEqual(3, alloced.size(), "Should have different number of allocations!");
        if (o13 == o1) {
            assertObjectEqual(o31, o3, "Bad allocated object!");
        } else {
            assertObjectEqual(o13, o3, "Bad allocated object!");
            assertObjectEqual(o31, o1, "Bad allocated object!");
        }
        Object o4 = aop.alloc();
        assertIntsEqual(4, alloced.size(), "Should have different number of allocations!");
        assertObjectEqual(o4, alloced.get(3), "Bad allocated object!");
    }

    private static class ReportingPool extends AllocationPool<Object> {

        private final CArrayList<Object> alloced;

        ReportingPool(CArrayList<Object> alloced) {
            this.alloced = alloced;
        }

        @Override
        protected Object allocateNew() {
            Object out = new Object();
            alloced.add(out);
            return out;
        }
    }
}
