/*
 * Copyright 2013 Colby Skeggs
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
        AllocationPool<Object> aop = new AllocationPool<Object>() {
            @Override
            protected Object allocateNew() {
                Object out = new Object();
                alloced.add(out);
                return out;
            }
        };
        assertEqual(0, alloced.size(), "Should have different number of allocations!");
        Object o1 = aop.alloc();
        assertEqual(1, alloced.size(), "Should have different number of allocations!");
        assertEqual(o1, alloced.get(0), "Bad allocated object!");
        Object o2 = aop.alloc();
        assertEqual(2, alloced.size(), "Should have different number of allocations!");
        assertEqual(o2, alloced.get(1), "Bad allocated object!");
        Object o3 = aop.alloc();
        assertEqual(3, alloced.size(), "Should have different number of allocations!");
        assertEqual(o3, alloced.get(2), "Bad allocated object!");
        aop.release(o2);
        assertEqual(3, alloced.size(), "Should have different number of allocations!");
        assertEqual(o2, aop.alloc(), "Bad allocated object!");
        aop.release(o1);
        aop.release(o3);
        assertEqual(3, alloced.size(), "Should have different number of allocations!");
        Object o13 = aop.alloc();
        Object o31 = aop.alloc();
        assertEqual(3, alloced.size(), "Should have different number of allocations!");
        if (o13 == o1) {
            assertEqual(o31, o3, "Bad allocated object!");
        } else {
            assertEqual(o13, o3, "Bad allocated object!");
            assertEqual(o31, o1, "Bad allocated object!");
        }
        Object o4 = aop.alloc();
        assertEqual(4, alloced.size(), "Should have different number of allocations!");
        assertEqual(o4, alloced.get(3), "Bad allocated object!");
    }
}
