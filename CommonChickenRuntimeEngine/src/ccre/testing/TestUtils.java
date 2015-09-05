/*
 * Copyright 2013-2015 Colby Skeggs
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
import java.util.Random;

import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CLinkedList;
import ccre.util.CList;
import ccre.util.CallerInfo;
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
        testCurrentTimeSeconds();
        testDeadzone();
        testSplit();
        testEmptyList();
        testAsList();
        // CArrayUtils.castToGeneric not tested here, because it's really hard to test and only exists to counter compiler warnings.
        testCopyOf();
        testSortLists();
        testSortArrays();
        testUpdateRamping();
        testBytesToInt();
        testBytesToFloat();
        testIsStringEmpty();
        testDoesStringContain();
        testCallerInfo();
        testMethodCaller();
        testThrowablePrinting();
    }

    private void checkStringContains(String str, String check) throws TestingException {
        boolean checking = Utils.doesStringContain(str, check);
        boolean actuallyHas = false;
        for (int i = 0; i <= str.length() - check.length(); i++) {
            if (str.substring(i, i + check.length()).equals(check)) {
                actuallyHas = true;
                break;
            }
        }
        assertTrue(checking == actuallyHas, "containment failure: thought " + checking + " but was " + actuallyHas);
    }

    private void testDoesStringContain() throws TestingException {
        String[] strs = new String[] { "", "aha", "a testy tester testing", "micromanage", "!!@!@!", "\0\0\0\0", "123412341234", "repeated repeaters repeating", "but no" };
        for (String str : strs) {
            for (String supersubstr : strs) {
                for (int i = 0; i < supersubstr.length(); i++) {
                    for (int j = i; j < supersubstr.length(); j++) {
                        String check = supersubstr.substring(i, j);
                        checkStringContains(str, check);
                    }
                }
            }
        }
    }

    private void testIsStringEmpty() throws TestingException {
        assertTrue(Utils.isStringEmpty(""), "string was actually empty!");
        assertTrue(Utils.isStringEmpty("hello".substring(4, 4)), "string was actually empty!");
        assertFalse(Utils.isStringEmpty("hello world"), "string was not empty!");
        assertFalse(Utils.isStringEmpty("\0"), "string was not empty!");
        assertFalse(Utils.isStringEmpty("\1"), "string was not empty!");
        assertFalse(Utils.isStringEmpty(" "), "string was not empty!");
        assertFalse(Utils.isStringEmpty("\n"), "string was not empty!");
        assertFalse(Utils.isStringEmpty("\0\0\0\0"), "string was not empty!");
        assertFalse(Utils.isStringEmpty("THIS IS A TEST!"), "string was not empty!");
    }

    private void checkFloat(float f) throws TestingException {
        int ibits = Float.floatToIntBits(f);
        byte[] d = new byte[] { (byte) (ibits >> 24), (byte) (ibits >> 16), (byte) (ibits >> 8), (byte) ibits };
        assertObjectEqual(Utils.bytesToInt(d, 0), ibits, "int mismatch!");
        assertObjectEqual(Utils.bytesToFloat(d, 0), f, "float mismatch!");
    }

    private void testBytesToFloat() throws TestingException {
        checkFloat(0);
        checkFloat(1);
        checkFloat(-1);
        checkFloat(Float.NaN);
        checkFloat(Float.NEGATIVE_INFINITY);
        checkFloat(Float.POSITIVE_INFINITY);
        checkFloat(Float.MIN_VALUE);
        checkFloat(Float.MAX_VALUE);
        checkFloat(-Float.MIN_VALUE);
        checkFloat(-Float.MAX_VALUE);
        Random r = new Random();
        for (int i = 0; i < 10000; i++) {
            checkFloat((float) (r.nextGaussian() / 10000));
            checkFloat((float) r.nextGaussian());
            checkFloat((float) (r.nextGaussian() * 10000));
        }
    }

    private void checkInts(int a, int b, int c, int d, int expect) throws TestingException {
        assertIntsEqual(Utils.bytesToInt(new byte[] { (byte) a, (byte) b, (byte) c, (byte) d }, 0), expect, "mismatched ints!");
        assertIntsEqual(Utils.bytesToInt(new byte[] { 0, 0, 0, (byte) a, (byte) b, (byte) c, (byte) d }, 3), expect, "offset failed!");
    }

    private void testBytesToInt() throws TestingException {
        checkInts(0x00, 0x00, 0x00, 0x00, 0);
        checkInts(0xFF, 0xFF, 0xFF, 0xFF, -1);
        checkInts(0x00, 0x00, 0x00, 0x01, 1);
        checkInts(0x00, 0x00, 0x00, 0x0A, 10);
        checkInts(0x00, 0x00, 0x00, 0x10, 16);
        checkInts(0x00, 0x00, 0x00, 0x42, 66);
        checkInts(0x00, 0x00, 0x01, 0x00, 256);
        checkInts(0x00, 0x00, 0x01, 0x42, 322);
        checkInts(0x00, 0x00, 0xA1, 0x42, 0xA142);
        checkInts(0x00, 0xBC, 0xFF, 0x02, 0xBCFF02);
        checkInts(0x72, 0xEE, 0x99, 0x31, 0x72EE9931);
        checkInts(0xCA, 0xAC, 0xBD, 0xDB, 0xCAACBDDB);
    }

    private void testUpdateRamping() throws TestingException {
        for (float base : new float[] { -1, 0, 1, 0.5f, -0.5f, 0.1f, -0.1f, 0.001f, -0.001f, 0.7f, -0.7f, 56, -56, -1.3f, 1.3f }) {
            for (float limit : new float[] { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 1f, 2f, 2.5f, 6f, 10f }) {
                assertObjectEqual(Utils.updateRamping(base, base, limit), base, "ramping mismatch!");
                assertObjectEqual(Utils.updateRamping(base, base, -limit), base, "ramping mismatch!");
                for (float rel : new float[] { 0f, 0.01f, 0.1f, 0.2f, 0.3f, 0.5f, 1f, 2f, 30f }) {
                    if (rel >= limit) {
                        assertObjectEqual(Utils.updateRamping(base, base + rel, limit), base + limit, "ramping mismatch!");
                        assertObjectEqual(Utils.updateRamping(base, base + rel, -limit), base + limit, "ramping mismatch!");
                        assertObjectEqual(Utils.updateRamping(base, base - rel, limit), base - limit, "ramping mismatch!");
                        assertObjectEqual(Utils.updateRamping(base, base - rel, -limit), base - limit, "ramping mismatch!");
                    } else {
                        assertObjectEqual(Utils.updateRamping(base, base + rel, limit), base + rel, "ramping mismatch!");
                        assertObjectEqual(Utils.updateRamping(base, base + rel, -limit), base + rel, "ramping mismatch!");
                        assertObjectEqual(Utils.updateRamping(base, base - rel, limit), base - rel, "ramping mismatch!");
                        assertObjectEqual(Utils.updateRamping(base, base - rel, -limit), base - rel, "ramping mismatch!");
                    }
                }
            }
            for (float rel : new float[] { 0f, 0.01f, 0.1f, 0.2f, 0.3f, 0.5f, 1f, 2f, 30f }) {
                assertObjectEqual(Utils.updateRamping(base, base + rel, 0), base + rel, "ramping mismatch!");
            }
        }
    }

    private void testSortArrays() throws TestingException {
        Random r = new Random();
        Integer[] array = new Integer[] { 7, 2, 9123, 19124, 123, 52, 1, -162, 0, 0, 0, 12, 80 };
        CArrayUtils.sort(array);
        for (int index = 1; index < array.length; index++) {
            assertTrue(array[index - 1] <= array[index], "List not sorted!");
        }
        assertIntsEqual(array[0], -162, "Bad first element!");
        assertIntsEqual(array[1], 0, "Bad second element!");
        assertIntsEqual(array[2], 0, "Bad third element!");
        assertIntsEqual(array[3], 0, "Bad fourth element!");
        assertIntsEqual(array[4], 1, "Bad fifth element!");
        array = new Integer[1000];
        for (int j = 0; j < array.length; j++) {
            array[j] = r.nextInt(1000);
        }
        CArrayUtils.sort(array);
        for (int index = 1; index < array.length; index++) {
            assertTrue(array[index - 1] <= array[index], "List not sorted!");
        }
    }

    private void testSortLists() throws TestingException {
        CLinkedList<Integer> list = new CLinkedList<Integer>(CArrayUtils.asList(7, 2, 9123, 19124, 123, 52, 1, -162, 0, 0, 0, 12, 80));
        int size = list.size();
        CArrayUtils.sort(list);
        assertIntsEqual(size, list.size(), "Bad size!");
        for (int index = 1; index < size; index++) {
            assertTrue(list.get(index - 1) <= list.get(index), "List not sorted!");
        }
        assertTrue(list.contains(19124) & list.contains(0) & list.contains(7), "List removed elements during sorting!");
        list.clear();
        Random r = new Random();
        for (int j = 0; j < 1000; j++) {
            list.add(r.nextInt(1000));
        }
        size = list.size();
        CArrayUtils.sort(list);
        assertIntsEqual(size, list.size(), "Bad size!");
        for (int index = 1; index < size; index++) {
            assertTrue(list.get(index - 1) <= list.get(index), "List not sorted!");
        }
    }

    private void testCopyOf() throws TestingException {
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

    private void testAsList() throws TestingException {
        CList<Integer> asList = CArrayUtils.asList(10, 20, 30, 40);

        boolean exception = false;
        try {
            asList.add(10);
        } catch (UnsupportedOperationException e) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        exception = false;
        try {
            asList.clear();
        } catch (UnsupportedOperationException e) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

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

        exception = false;
        try {
            asList.get(-1);
        } catch (IndexOutOfBoundsException o) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        exception = false;
        try {
            asList.get(4);
        } catch (IndexOutOfBoundsException o) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        assertIntsEqual(asList.indexOf(30), 2, "Bad index!");
        assertIntsEqual(asList.lastIndexOf(40), 3, "Bad index!");

        exception = false;
        try {
            asList.remove(Integer.valueOf(10));
        } catch (UnsupportedOperationException o) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        assertFalse(asList.isEmpty(), "Should not be empty!");
        Iterator<Integer> itr = asList.iterator();
        int i = 0;
        while (itr.hasNext()) {
            assertObjectEqual(itr.next(), asList.get(i++), "Bad iterator result!");
        }
        assertIntsEqual(i, asList.size(), "Bad iterator count!");

        exception = false;
        try {
            asList.remove(0);
        } catch (UnsupportedOperationException o) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        asList.set(1, 100);
        assertIntsEqual(asList.get(1), 100, "Bad set operation!");
        assertObjectEqual(asList.toString(), "[10, 100, 30, 40]", "Bad toString!");
    }

    private void testEmptyList() throws TestingException {
        CList<Object> c = CArrayUtils.getEmptyList();
        assertObjectEqual(CArrayUtils.EMPTY_LIST, c, "Expected getEmptyList() to be the same as EMPTY_LIST!");
        boolean exception = false;
        try {
            c.add(10);
        } catch (UnsupportedOperationException u) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        exception = false;
        try {
            c.add(0, 10);
        } catch (UnsupportedOperationException u) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        exception = false;
        try {
            c.addAll(new CArrayList<String>(new String[] { "Test", "Exactly!" }));
        } catch (UnsupportedOperationException u) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        exception = false;
        try {
            c.addAll(0, new CArrayList<String>(new String[] { "Test", "Exactly!" }));
        } catch (UnsupportedOperationException u) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        assertFalse(c.contains(3), "Should not contain anything!");
        assertTrue(c.containsAll(new CArrayList<Object>()), "Should contain nothing!");
        assertFalse(c.containsAll(new CArrayList<Object>(new Object[] { null })), "Should not contain anything!");
        assertIntsEqual(c.fillArray(new Object[0]), 0, "Should be empty!");

        exception = false;
        try {
            c.get(0);
        } catch (IndexOutOfBoundsException u) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        assertIntsEqual(c.indexOf(2), -1, "Should not contain anything!");
        assertTrue(c.isEmpty(), "Should be empty!");
        assertFalse(c.iterator().hasNext(), "Should be empty!");
        assertIntsEqual(c.lastIndexOf(2), -1, "Should not contain anything!");

        assertFalse(c.remove("Hi"), "Should not contain anything!");

        exception = false;
        try {
            c.remove(2);
        } catch (UnsupportedOperationException e) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        exception = false;
        try {
            c.set(0, null);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception, "Should have errored!");

        assertIntsEqual(c.size(), 0, "Should be empty!");
        assertIntsEqual(c.toArray().length, 0, "Should be empty!");
        assertObjectEqual(c.toString(), "[]", "Bad toString!");
    }

    private void testSplit() throws TestingException {
        String ss = " This is a    very  long test sentence ... sort of! ";
        String[] split = Utils.split(ss, ' ');
        assertIntsEqual(split.length, 16, "Bad split length!");
        StringBuffer sb = new StringBuffer(50);
        for (String p : split) {
            sb.append(p).append(' ');
        }
        sb.setLength(sb.length() - 1);
        assertObjectEqual(sb.toString(), ss, "Bad split data!");
    }

    private void testDeadzone() throws TestingException {
        for (float i = -17.6f; i <= 17.6f; i += 0.1f) {
            float dz = Utils.deadzone(i, 7.2f);
            if (i >= -7.2f && i <= 7.2f) {
                assertObjectEqual(dz, 0f, "Bad deadzoning!");
            } else {
                assertObjectEqual(dz, i, "Bad deadzoning!");
            }
        }
    }

    private void testCurrentTimeSeconds() throws InterruptedException, TestingException {
        boolean success = false;
        for (int i = 0; i < 5; i++) {
            float here = Utils.getCurrentTimeSeconds();
            Thread.sleep(99);// 99 because it causes Java SE to make the request more accurate...
            float there = Utils.getCurrentTimeSeconds();
            float dt = Math.abs(there - here - (99 / 1000f));
            if (dt >= 0.002) {
                //Logger.warning("Failed timing test: " + here + " to " + there + " is " + (there - here) + " and expected " + (100 / 1000f));
                Logger.warning("Failed timing attempt...");
            } else {
                success = true;
                break;
            }
        }
        assertTrue(success, "Five timing check attempts failed!");
    }

    private void testCallerInfo() throws TestingException {
        CallerInfo info = new CallerInfo("class", "method", "file", 10);
        assertObjectEqual(info.getClassName(), "class", "Bad passthrough!");
        assertObjectEqual(info.getMethodName(), "method", "Bad passthrough!");
        assertObjectEqual(info.getFileName(), "file", "Bad passthrough!");
        assertObjectEqual(info.getLineNum(), 10, "Bad passthrough!");
        assertObjectEqual(info.toString(), "class.method(file:10)", "Bad toString()!");
        info = new CallerInfo("class", null, null, -1);
        assertObjectEqual(info.getClassName(), "class", "Bad passthrough!");
        assertObjectEqual(info.getMethodName(), null, "Bad passthrough!");
        assertObjectEqual(info.getFileName(), null, "Bad passthrough!");
        assertObjectEqual(info.getLineNum(), -1, "Bad passthrough!");
        // TODO: maybe this should be changed to a more useful description string?
        assertObjectEqual(info.toString(), "class.null(null:-1)", "Bad toString()!");
        try {
            new CallerInfo(null, "method", "file", 10);
            assertFail("Expected an IllegalArgumentException - can't have a NULL class!");
        } catch (IllegalArgumentException ex) {
            // correct!
        }
        try {
            new CallerInfo(null, null, null, -1);
            assertFail("Expected an IllegalArgumentException - can't have a NULL class!");
        } catch (IllegalArgumentException ex) {
            // correct!
        }
    }

    private void testMethodCaller() throws TestingException {
        CallerInfo info = Utils.getMethodCaller(0);
        CallerInfo info2 = Utils.getMethodCaller(0);
        // TODO: Check that toString() is accurate?
        String expect = "ccre.testing.TestWorkarounds.testMethodCaller(TestWorkarounds.java:";
        String istr = info.toString(), istr2 = info2.toString();
        assertObjectEqual(istr.substring(0, expect.length()), expect, "bad caller info");
        assertObjectEqual(istr2.substring(0, expect.length()), expect, "bad caller info");
        assertIntsEqual(istr.charAt(istr.length() - 1), ')', "bad caller info");
        assertIntsEqual(istr2.charAt(istr.length() - 1), ')', "bad caller info");
        int line = Integer.parseInt(istr.substring(expect.length(), istr.length() - 1));
        int line2 = Integer.parseInt(istr2.substring(expect.length(), istr.length() - 1));
        assertIntsEqual(line, line2 - 1, "line numbers not one apart");

        for (int i = -10; i < 0; i++) {
            assertIdentityEqual(Utils.getMethodCaller(i), null, "got caller info for internals");
        }
        assertIdentityEqual(Utils.getMethodCaller(1000), null, "got caller info for what should be off the end of the stack trace");
    }

    private void testThrowablePrinting() throws TestingException {
        int expectedLine = Utils.getMethodCaller(0).getLineNum() + 1;
        String got = Utils.toStringThrowable(new Throwable("Example"));
        String[] pts = Utils.split(got, '\n');
        assertObjectEqual(pts[0], "java.lang.Throwable: Example", "bad line 1 of Throwable dump");
        assertObjectEqual(pts[1], "\tat ccre.testing.TestWorkarounds.testThrowablePrinting(TestWorkarounds.java:" + expectedLine + ")", "bad line 1 of Throwable dump");

        assertIdentityEqual(Utils.toStringThrowable(null), null, "should have returned null!");
    }
}
