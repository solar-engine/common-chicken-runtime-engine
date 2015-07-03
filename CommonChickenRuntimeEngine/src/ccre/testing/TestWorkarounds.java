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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import ccre.util.Utils;
import ccre.workarounds.CallerInfo;
import ccre.workarounds.ThrowablePrinter;

/**
 * A test that tests the ccre.workarounds package.
 *
 * @author skeggsc
 */
public class TestWorkarounds extends BaseTest {

    @Override
    public String getName() {
        return "ccre.workarounds Tests";
    }

    @Override
    protected void runTest() throws Throwable {
        testCallerInfo();
        testMethodCaller();
        testThrowablePrinting();
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
            info = new CallerInfo(null, "method", "file", 10);
            assertFail("Expected an IllegalArgumentException - can't have a NULL class!");
        } catch (IllegalArgumentException ex) {
            // correct!
        }
        try {
            info = new CallerInfo(null, null, null, -1);
            assertFail("Expected an IllegalArgumentException - can't have a NULL class!");
        } catch (IllegalArgumentException ex) {
            // correct!
        }
    }

    private void testMethodCaller() throws TestingException {
        CallerInfo info = ThrowablePrinter.getMethodCaller(0);
        CallerInfo info2 = ThrowablePrinter.getMethodCaller(0);
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
            assertIdentityEqual(ThrowablePrinter.getMethodCaller(i), null, "got caller info for internals");
        }
        assertIdentityEqual(ThrowablePrinter.getMethodCaller(1000), null, "got caller info for what should be off the end of the stack trace");
    }

    private void testThrowablePrinting() throws TestingException {
        int expectedLine = ThrowablePrinter.getMethodCaller(0).getLineNum() + 1;
        String got = ThrowablePrinter.toStringThrowable(new Throwable("Example"));
        String[] pts = Utils.split(got, '\n');
        assertObjectEqual(pts[0], "java.lang.Throwable: Example", "bad line 1 of Throwable dump");
        assertObjectEqual(pts[1], "\tat ccre.testing.TestWorkarounds.testThrowablePrinting(TestWorkarounds.java:" + expectedLine + ")", "bad line 1 of Throwable dump");

        assertIdentityEqual(ThrowablePrinter.toStringThrowable(null), null, "should have returned null!");

        try {
            ThrowablePrinter.printThrowable(new Throwable(), null);
            assertFail("Should have thrown NullPointerException!");
        } catch (NullPointerException ex) {
            // correct!
        }

        try {
            ThrowablePrinter.printThrowable(null, null);
            assertFail("Should have thrown NullPointerException!");
        } catch (NullPointerException ex) {
            // correct!
        }

        try {
            ThrowablePrinter.printThrowable(null, new PrintStream(new ByteArrayOutputStream()));
            assertFail("Should have thrown NullPointerException!");
        } catch (NullPointerException ex) {
            // correct!
        }
    }
}
