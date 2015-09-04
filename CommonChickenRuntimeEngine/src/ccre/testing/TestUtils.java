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
        testUpdateRamping();
        testBytesToInt();
        testBytesToFloat();
        testIsStringEmpty();
        testDoesStringContain();
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
            Thread.sleep(99); // 99 because it causes Java SE to make the request more accurate...
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
}
