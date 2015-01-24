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

import java.util.Iterator;
import java.util.NoSuchElementException;

import ccre.util.CArrayUtils;
import ccre.util.CList;

/**
 * A base class for testing the various kinds of lists in the CCRE.
 *
 * @author skeggsc
 */
public abstract class BaseTestList extends BaseTest {

    /**
     * Test if the specified CList works properly.
     *
     * @param a The CList&lt;String&gt; to test.
     * @throws TestingException If the test fails.
     */
    protected void runTest(CList<String> a) throws TestingException {
        // isEmpty
        assertTrue(a.isEmpty(), "Bad isEmpty!");
        // size
        assertIntsEqual(a.size(), 0, "Bad size!");
        // add
        a.add("Alpha");
        assertFalse(a.isEmpty(), "Bad isEmpty!");
        assertIntsEqual(a.size(), 1, "Bad size!");
        // add, indexed
        a.add(0, "Beta");
        
        boolean exception = false;
        try {
            a.add(-1, "Test");
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception, "Should have thrown IndexOutOfBoundsException!");
        
        exception = false;
        try {
            a.add(3, "Test");
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception, "Should have thrown IndexOutOfBoundsException!");
        
        assertFalse(a.isEmpty(), "Bad isEmpty!");
        assertIntsEqual(a.size(), 2, "Bad size!");
        // addAll
        a.addAll(CArrayUtils.asList("10", "20", "30"));
        assertFalse(a.isEmpty(), "Bad isEmpty!");
        assertIntsEqual(a.size(), 5, "Bad size!");
        // addAll, indexed
        a.addAll(3, CArrayUtils.asList("40", "40", "40"));
        
        exception = false;
        try {
            a.addAll(-1, CArrayUtils.asList("40", "40"));
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception, "Should have thrown IndexOutOfBoundsException!");
        
        exception = false;
        try {
            a.addAll(9, CArrayUtils.asList("40", "40"));
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception, "Should have thrown IndexOutOfBoundsException!");
        
        // get
        assertObjectEqual(a.get(0), "Beta", "Bad element!");
        assertObjectEqual(a.get(1), "Alpha", "Bad element!");
        assertObjectEqual(a.get(2), "10", "Bad element!");
        assertObjectEqual(a.get(3), "40", "Bad element!");
        assertObjectEqual(a.get(4), "40", "Bad element!");
        assertObjectEqual(a.get(5), "40", "Bad element!");
        assertObjectEqual(a.get(6), "20", "Bad element!");
        assertObjectEqual(a.get(7), "30", "Bad element!");
        
        exception = false;
        try {
            a.get(-1);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception, "Should have thrown IndexOutOfBoundsException!");
        
        exception = false;
        try {
            a.get(8);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception, "Should have thrown IndexOutOfBoundsException!");
        
        // indexOf
        assertIntsEqual(a.indexOf("Beta"), 0, "Bad index!");
        assertIntsEqual(a.indexOf("Alpha"), 1, "Bad index!");
        assertIntsEqual(a.indexOf("30"), 7, "Bad index!");
        assertIntsEqual(a.indexOf("40"), 3, "Bad index!");
        assertIntsEqual(a.indexOf("82"), -1, "Bad index!");
        // lastIndexOf
        assertIntsEqual(a.lastIndexOf("Beta"), 0, "Bad index!");
        assertIntsEqual(a.lastIndexOf("40"), 5, "Bad index!");
        assertIntsEqual(a.lastIndexOf("30"), 7, "Bad index!");
        assertIntsEqual(a.lastIndexOf("35"), -1, "Bad index!");
        // iterator
        Iterator<String> itr = a.iterator();
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "Beta", "Bad element from iterator!");
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "Alpha", "Bad element from iterator!");
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "10", "Bad element from iterator!");
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "40", "Bad element from iterator!");
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "40", "Bad element from iterator!");
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "40", "Bad element from iterator!");
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "20", "Bad element from iterator!");
        assertTrue(itr.hasNext(), "Bad result from iterator!");
        assertObjectEqual(itr.next(), "30", "Bad element from iterator!");
        assertFalse(itr.hasNext(), "Bad result from iterator!");
        
        exception = false;
        try {
            itr.next();
        } catch (NoSuchElementException e) {
            exception = true;
        }
        assertTrue(exception, "Should have thrown IndexOutOfBoundsException!");
        
        // remove, indexed
        assertObjectEqual(a.remove(1), "Alpha", "Bad remove result!");
        assertObjectEqual(a.get(0), "Beta", "Bad element!");
        assertObjectEqual(a.get(1), "10", "Bad element!");
        assertObjectEqual(a.get(2), "40", "Bad element!");
        assertObjectEqual(a.get(3), "40", "Bad element!");
        assertObjectEqual(a.get(4), "40", "Bad element!");
        assertObjectEqual(a.get(5), "20", "Bad element!");
        assertObjectEqual(a.get(6), "30", "Bad element!");
        assertIntsEqual(a.size(), 7, "Bad size!");
        assertFalse(a.isEmpty(), "Bad isEmpty!");
        // remove, valued
        assertTrue(a.remove("20"), "Bad remove!");
        assertFalse(a.remove("WHUT"), "Bad remove!");
        assertObjectEqual(a.get(0), "Beta", "Bad element!");
        assertObjectEqual(a.get(1), "10", "Bad element!");
        assertObjectEqual(a.get(2), "40", "Bad element!");
        assertObjectEqual(a.get(3), "40", "Bad element!");
        assertObjectEqual(a.get(4), "40", "Bad element!");
        assertObjectEqual(a.get(5), "30", "Bad element!");
        assertIntsEqual(a.size(), 6, "Bad size!");
        // toString
        assertObjectEqual(a.toString(), "[Beta, 10, 40, 40, 40, 30]", "Bad toString!");
        // set
        a.set(1, "Testing");
        assertObjectEqual(a.get(0), "Beta", "Bad element!");
        assertObjectEqual(a.get(1), "Testing", "Bad set!");
        assertObjectEqual(a.get(2), "40", "Bad element!");
        // contains
        assertTrue(a.contains("40"), "Bad contains!");
        assertFalse(a.contains("42"), "Bad contains!");
        // containsAll
        assertTrue(a.containsAll(CArrayUtils.asList("30", "Testing", "40")), "Bad contains!");
        assertFalse(a.containsAll(CArrayUtils.asList("30", "Tester", "40")), "Bad contains!");
        // fillArray
        String[] target = new String[6];
        assertIntsEqual(a.fillArray(target), 0, "Bad fillArray result!");
        for (int i = 0; i < target.length; i++) {
            assertObjectEqual(target[i], a.get(i), "Bad filled array contents!");
        }
        target = new String[5];
        assertIntsEqual(a.fillArray(target), 1, "Bad fillArray result!");
        for (int i = 0; i < target.length; i++) {
            assertObjectEqual(target[i], a.get(i), "Bad filled array contents!");
        }
        target = new String[7];
        assertIntsEqual(a.fillArray(target), -1, "Bad fillArray result!");
        for (int i = 0; i < target.length - 1; i++) {
            assertObjectEqual(target[i], a.get(i), "Bad filled array contents!");
        }
        assertObjectEqual(target[6], null, "Bad filled array contents!");
        // removeAll
        assertTrue(a.removeAll(CArrayUtils.asList("Testing", "40", "Wait...")), "Remove didn't cause modifications!");
        assertFalse(a.removeAll(CArrayUtils.asList("Nope", "Noep", "Ulp")), "Remove caused modifications!");
        assertObjectEqual(a.get(0), "Beta", "Bad element!");
        assertObjectEqual(a.get(1), "40", "Bad element!");
        assertObjectEqual(a.get(2), "40", "Bad element!");
        assertObjectEqual(a.get(3), "30", "Bad element!");
        assertIntsEqual(a.size(), 4, "Bad size!");
        // retainAll
        assertFalse(a.retainAll(CArrayUtils.asList("Beta", "40", "30", "Extra!")), "Retain caused modifications!");
        assertTrue(a.retainAll(CArrayUtils.asList("Beta", "Noep", "30")), "Retain didn't cause modifications!");
        assertObjectEqual(a.get(0), "Beta", "Bad element!");
        assertObjectEqual(a.get(1), "30", "Bad element!");
        assertIntsEqual(a.size(), 2, "Bad size!");
        // toArray
        Object[] o = a.toArray();
        assertIntsEqual(o.length, a.size(), "Bad toArray length!");
        for (int i = 0; i < o.length; i++) {
            assertObjectEqual(o[i], a.get(i), "Bad toArray!");
        }
        // clear
        a.clear();
        assertTrue(a.isEmpty(), "Supposed to be empty!");
        assertIntsEqual(a.size(), 0, "Supposed to be 0-lengthed!");
    }
}
