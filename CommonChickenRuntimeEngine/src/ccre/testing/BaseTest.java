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

import ccre.log.Logger;

/**
 * A superclass for the various tests in this package. This provides a framework
 * for the code for each test, as well as providing various assertion methods.
 *
 * @author skeggsc
 */
public abstract class BaseTest {

    /**
     * Get the name of this test.
     *
     * @return the test's name.
     */
    public abstract String getName();

    /**
     * Run this test. This will throw a TestingException if the test fails.
     *
     * @throws TestingException if the test fails.
     */
    protected abstract void runTest() throws TestingException, InterruptedException;

    /**
     * Run this test verbosely. This will log various status messages during the
     * test.
     *
     * @return true if the test succeeded and false if it failed.
     */
    public final boolean test() {
        return test(true);
    }

    /**
     * Run this test. If ran verbosely, this will log various status messages
     * during the test.
     *
     * @param verbose should status messages be logged?
     * @return true if the test succeeded and false if it failed.
     */
    public final synchronized boolean test(boolean verbose) { // Synchronized so that only one instance of the test will be running.
        if (verbose) {
            Logger.fine("Attempting test: " + getName());
        }
        try {
            runTest();
        } catch (TestingException ex) {
            if (verbose) {
                Logger.warning("Failed test: " + getName());
                ex.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (verbose) {
                Logger.warning("Exception during test: " + getName());
                t.printStackTrace();
            }
            return false;
        }
        if (verbose) {
            Logger.info("Test succeeded: " + getName());
        }
        return true;
    }

    /**
     * The test has failed! Report this now and stop the test.
     *
     * @param message the explanation of what went wrong.
     * @throws TestingException always - the test has failed.
     */
    protected void assertFail(String message) throws TestingException {
        throw new TestingException(message);
    }

    /**
     * The bool argument should be true. If it isn't, the test has failed!
     * Report this and stop the test.
     *
     * @param bool the boolean to test.
     * @param message the explanation of what went wrong.
     * @throws TestingException if bool is false.
     */
    protected void assertTrue(boolean bool, String message) throws TestingException {
        if (!bool) {
            throw new TestingException(message);
        }
    }

    /**
     * The bool argument should be false. If it isn't, the test has failed!
     * Report this and stop the test.
     *
     * @param bool the boolean to test.
     * @param message the explanation of what went wrong.
     * @throws TestingException if bool is true.
     */
    protected void assertFalse(boolean bool, String message) throws TestingException {
        if (bool) {
            throw new TestingException(message);
        }
    }

    /**
     * The integer arguments should be equal! If not, the test has failed!
     * Report this and stop the test.
     *
     * @param a the first integer.
     * @param b the second integer.
     * @param message the explanation of what went wrong.
     * @throws TestingException if the integers are unequal.
     */
    protected void assertEqual(int a, int b, String message) throws TestingException {
        assertTrue(a == b, message + "( " + a + " != " + b + " )");
    }

    /**
     * The object arguments should be identity-equal, as in
     * <code>a == b</code>! If not, the test has failed! Report this and stop
     * the test.
     *
     * @param a the first object.
     * @param b the second object.
     * @param message the explanation of what went wrong.
     * @throws TestingException if the objects are unequal.
     */
    protected void assertIEqual(Object a, Object b, String message) throws TestingException {
        assertTrue(a == b, message + "( " + a + " != " + b + " )");
    }

    /**
     * The object arguments should be value-equal, as in
     * <code>a == null ? b == null : a.equals(b)</code>! If not, the test has
     * failed! Report this and stop the test.
     *
     * @param a the first object.
     * @param b the second object.
     * @param message the explanation of what went wrong.
     * @throws TestingException if the objects are unequal.
     */
    protected void assertEqual(Object a, Object b, String message) throws TestingException {
        assertTrue(a == null ? b == null : a.equals(b), message + "( " + a + " != " + b + " )");
    }
}
