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

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CList;
import ccre.util.Utils;
import java.util.Iterator;
import java.util.Map;

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
            float here = Utils.currentTimeSeconds.readValue();
            try {
                Thread.sleep(99); // 99 because it causes Java SE to make the request more accurate...
            } catch (InterruptedException ex) {
                assertFail("Interrupted during timing test.");
                throw ex;
            }
            float there = Utils.currentTimeSeconds.readValue();
            float dt = Math.abs(there - here - (99 / 1000f));
            if (dt < 0.002) {
                success = true;
                break;
            } else {
                Logger.warning("Failed timing test: " + here + " to " + there + " is " + (there - here) + " and expected " + (100 / 1000f));
                /*Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
                for (Map.Entry<Thread, StackTraceElement[]> elem : allStackTraces.entrySet()) {
                    Throwable tmp = new Throwable("Trace for " + elem.getKey());
                    tmp.setStackTrace(elem.getValue());
                    Logger.log(LogLevel.INFO, "Traces", tmp);
                }*/
            }
        }
        assertTrue(success, "Five timing check attempts failed!");
        // Utils.deadzone
        for (float i = -17.6f; i <= 17.6f; i += 0.1f) {
            float dz = Utils.deadzone(i, 7.2f);
            if (i >= -7.2f && i <= 7.2f) {
                assertEqual(dz, 0f, "Bad deadzoning!");
            } else {
                assertEqual(dz, i, "Bad deadzoning!");
            }
        }
        // Utils.split
        String ss = " This is a    very  long test sentence ... sort of! ";
        String[] split = Utils.split(ss, ' ');
        assertEqual(split.length, 16, "Bad split length!");
        StringBuffer sb = new StringBuffer(50);
        for (String p : split) {
            sb.append(p).append(' ');
        }
        sb.setLength(sb.length() - 1);
        assertEqual(sb.toString(), ss, "Bad split data!");
        // CArrayUtils.EMPTY_LIST, CArrayUtils.getEmptyList()
        CList<Object> c = CArrayUtils.getEmptyList();
        assertEqual(CArrayUtils.EMPTY_LIST, c, "Expected getEmptyList() to be the same as EMPTY_LIST!");
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
            c.addAll(new CArrayList<String>(new String[]{"Test", "Exactly!"}));
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException u) {
            // Correct!
        }
        try {
            c.addAll(0, new CArrayList<String>(new String[]{"Test", "Exactly!"}));
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException u) {
            // Correct!
        }
        assertFalse(c.contains(3), "Should not contain anything!");
        assertTrue(c.containsAll(new CArrayList<Object>()), "Should contain nothing!");
        assertFalse(c.containsAll(new CArrayList<Object>(new Object[]{null})), "Should not contain anything!");
        assertEqual(c.fillArray(new Object[0]), 0, "Should be empty!");
        try {
            c.get(0);
            assertFail("Should have errored!");
        } catch (IndexOutOfBoundsException u) {
            // Correct!
        }
        assertEqual(c.indexOf(2), -1, "Should not contain anything!");
        assertTrue(c.isEmpty(), "Should be empty!");
        assertFalse(c.iterator().hasNext(), "Should be empty!");
        assertEqual(c.lastIndexOf(2), -1, "Should not contain anything!");
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
        assertEqual(c.size(), 0, "Should be empty!");
        assertEqual(c.toArray().length, 0, "Should be empty!");
        assertEqual(c.toString(), "[]", "Bad toString!");
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
        assertEqual(asList.fillArray(test), 0, "Should have correct length!");
        assertEqual(test[0].intValue(), 10, "Bad fillArray!");
        assertEqual(test[1].intValue(), 20, "Bad fillArray!");
        assertEqual(test[2].intValue(), 30, "Bad fillArray!");
        assertEqual(test[3].intValue(), 40, "Bad fillArray!");
        Object[] arr = asList.toArray();
        for (int i = 0; i < arr.length; i++) {
            assertEqual(test[i], arr[i], "Bad toArray result!");
        }
        assertEqual(arr.length, test.length, "Bad toArray length!");
        assertEqual(asList.get(0).intValue(), 10, "Bad contents!");
        assertEqual(asList.get(1).intValue(), 20, "Bad contents!");
        assertEqual(asList.get(2).intValue(), 30, "Bad contents!");
        assertEqual(asList.get(3).intValue(), 40, "Bad contents!");
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
        assertEqual(asList.indexOf(30), 2, "Bad index!");
        assertEqual(asList.lastIndexOf(40), 3, "Bad index!");
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
            assertEqual(itr.next(), asList.get(i++), "Bad iterator result!");
        }
        assertEqual(i, asList.size(), "Bad iterator count!");
        try {
            asList.remove(0);
            assertFail("Should have errored!");
        } catch (UnsupportedOperationException o) {
            // Correct!
        }
        asList.set(1, 100);
        assertEqual(asList.get(1).intValue(), 100, "Bad set operation!");
        assertEqual(asList.toString(), "[10, 100, 30, 40]", "Bad toString!");
        // CArrayUtils.castToGeneric not tested here, because it's really hard to test and only exists to counter compiler warnings.
        // CArrayUtils.copyOf
        Object[] old = new Object[]{"A", "B", "C", "D"};
        Object[] arr2 = CArrayUtils.copyOf(old, 3);
        Object[] arr3 = CArrayUtils.copyOf(arr2, 5);
        assertEqual(arr2.length, 3, "Bad copyOf!");
        assertEqual(arr3.length, 5, "Bad copyOf!");
        for (int j = 0; j < arr2.length; j++) {
            assertEqual(old[j], arr2[j], "Bad copyOf!");
            assertEqual(old[j], arr3[j], "Bad copyOf!");
        }
        assertEqual(arr3[3], null, "Bad copyOf!");
        assertEqual(arr3[4], null, "Bad copyOf!");
    }
}
