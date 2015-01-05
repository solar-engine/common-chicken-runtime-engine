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

import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CList;
import ccre.util.Utils;

/**
 * Tests the Utils class and the CArrayUtils class.
 *
 * @author skeggsc
 */
public class TestUtils extends BaseTest {

    @Override
    public String getName() {
        return "Utils and CArrayUtils";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        // Utils.currentTimeSeconds
        boolean success = false;
        for (int i = 0; i < 5; i++) {
            float here = Utils.currentTimeSeconds.get();
            try {
                Thread.sleep(99); // 99 because it causes Java SE to make the request more accurate...
            } catch (InterruptedException ex) {
                assertFail("Interrupted during timing test.");
                throw ex;
            }
            float there = Utils.currentTimeSeconds.get();
            float dt = Math.abs(there - here - (99 / 1000f));
            if (dt >= 0.002) {
                Logger.warning("Failed timing test: " + here + " to " + there + " is " + (there - here) + " and expected " + (100 / 1000f));
            } else {
                success = true;
                break;
            }
        }
        assertTrue(success, "Five timing check attempts failed!");
        // Utils.deadzone
        for (float i = -17.6f; i <= 17.6f; i += 0.1f) {
            float dz = Utils.deadzone(i, 7.2f);
            if (i >= -7.2f && i <= 7.2f) {
                assertObjectEqual(dz, 0f, "Bad deadzoning!");
            } else {
                assertObjectEqual(dz, i, "Bad deadzoning!");
            }
        }
        // Utils.split
        String ss = " This is a    very  long test sentence ... sort of! ";
        String[] split = Utils.split(ss, ' ');
        assertIntsEqual(split.length, 16, "Bad split length!");
        StringBuffer sb = new StringBuffer(50);
        for (String p : split) {
            sb.append(p).append(' ');
        }
        sb.setLength(sb.length() - 1);
        assertObjectEqual(sb.toString(), ss, "Bad split data!");
        // CArrayUtils.EMPTY_LIST, CArrayUtils.getEmptyList()
        CList<Object> c = CArrayUtils.getEmptyList();
        assertObjectEqual(CArrayUtils.EMPTY_LIST, c, "Expected getEmptyList() to be the same as EMPTY_LIST!");
        try {
            c.add(10);
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException u) {
            // Correct!
        }
        try {
            c.add(0, 10);
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException u) {
            // Correct!
        }
        try {
            c.addAll(new CArrayList<String>(new String[] { "Test", "Exactly!" }));
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException u) {
            // Correct!
        }
        try {
            c.addAll(0, new CArrayList<String>(new String[] { "Test", "Exactly!" }));
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException u) {
            // Correct!
        }
        assertFalse(c.contains(3), "Should not contain anything!");
        assertTrue(c.containsAll(new CArrayList<Object>()), "Should contain nothing!");
        assertFalse(c.containsAll(new CArrayList<Object>(new Object[] { null })), "Should not contain anything!");
        assertIntsEqual(c.fillArray(new Object[0]), 0, "Should be empty!");
        try {
            c.get(0);
            assertFail("Should have errored!");
        } catch (IndexOutOfBoundsException u) {
            // Correct!
        }
        assertIntsEqual(c.indexOf(2), -1, "Should not contain anything!");
        assertTrue(c.isEmpty(), "Should be empty!");
        assertFalse(c.iterator().hasNext(), "Should be empty!");
        assertIntsEqual(c.lastIndexOf(2), -1, "Should not contain anything!");
        try {
            assertFalse(c.remove("Hi"), "Should not contain anything!");
        } catch (UnsupportedOperationException u) {
            // Potentially correct.
        }
        try {
            c.remove(2);
            assertFail("Should have errored!");
        } catch (IndexOutOfBoundsException e) {
            // Correct!
        } catch (UnsupportedOperationException e) {
            // Also correct!
        }
        try {
            c.set(0, null);
            assertFail("Should have errored!");
        } catch (IndexOutOfBoundsException e) {
            // Correct!
        } catch (UnsupportedOperationException e) {
            // Also correct!
        }
        assertIntsEqual(c.size(), 0, "Should be empty!");
        assertIntsEqual(c.toArray().length, 0, "Should be empty!");
        assertObjectEqual(c.toString(), "[]", "Bad toString!");
        // CArrayUtils.asList
        CList<Integer> asList = CArrayUtils.asList(10, 20, 30, 40);
        try {
            asList.add(10);
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException e) {
            // Correct!
        }
        try {
            asList.clear();
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException e) {
            // Correct!
        }
        assertTrue(asList.contains(30), "Should contain element!");
        assertFalse(asList.contains(35), "Should not contain element!");
        Integer[] test = new Integer[4];
        assertIntsEqual(asList.fillArray(test), 0, "Should have correct length!");
        assertIntsEqual(test[0], 10, "Bad fillArray!");
        assertIntsEqual(test[1], 20, "Bad fillArray!");
        assertIntsEqual(test[2], 30, "Bad fillArray!");
        assertIntsEqual(test[3], 40, "Bad fillArray!");
        Object[] arr = asList.toArray();
        for (int i = 0; i < arr.length; i++) {
            assertObjectEqual(test[i], arr[i], "Bad toArray result!");
        }
        assertIntsEqual(arr.length, test.length, "Bad toArray length!");
        assertIntsEqual(asList.get(0), 10, "Bad contents!");
        assertIntsEqual(asList.get(1), 20, "Bad contents!");
        assertIntsEqual(asList.get(2), 30, "Bad contents!");
        assertIntsEqual(asList.get(3), 40, "Bad contents!");
        try {
            asList.get(-1);
            assertFail("Should have errored!");
        } catch (IndexOutOfBoundsException o) {
            // Correct!
        }
        try {
            asList.get(4);
            assertFail("Should have errored!");
        } catch (IndexOutOfBoundsException o) {
            // Correct!
        }
        assertIntsEqual(asList.indexOf(30), 2, "Bad index!");
        assertIntsEqual(asList.lastIndexOf(40), 3, "Bad index!");
        try {
            asList.remove(Integer.valueOf(10));
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException o) {
            // Correct!
        }
        assertFalse(asList.isEmpty(), "Should not be empty!");
        Iterator<Integer> itr = asList.iterator();
        int i = 0;
        while (itr.hasNext()) {
            assertObjectEqual(itr.next(), asList.get(i++), "Bad iterator result!");
        }
        assertIntsEqual(i, asList.size(), "Bad iterator count!");
        try {
            asList.remove(0);
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException o) {
            // Correct!
        }
        asList.set(1, 100);
        assertIntsEqual(asList.get(1), 100, "Bad set operation!");
        assertObjectEqual(asList.toString(), "[10, 100, 30, 40]", "Bad toString!");
        // CArrayUtils.castToGeneric not tested here, because it's really hard to test and only exists to counter compiler warnings.
        // CArrayUtils.copyOf
        Object[] old = new Object[] { "A", "B", "C", "D" };
        Object[] arr2 = CArrayUtils.copyOf(old, 3);
        Object[] arr3 = CArrayUtils.copyOf(arr2, 5);
        assertIntsEqual(arr2.length, 3, "Bad copyOf!");
        assertIntsEqual(arr3.length, 5, "Bad copyOf!");
        for (int j = 0; j < arr2.length; j++) {
            assertObjectEqual(old[j], arr2[j], "Bad copyOf!");
            assertObjectEqual(old[j], arr3[j], "Bad copyOf!");
        }
        assertObjectEqual(arr3[3], null, "Bad copyOf!");
        assertObjectEqual(arr3[4], null, "Bad copyOf!");
    }
}
