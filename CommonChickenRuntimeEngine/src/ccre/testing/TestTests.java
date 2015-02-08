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

import ccre.log.Logger;

/**
 * A test that tests that tests work correctly! This is, as you can tell, very
 * important!
 *
 * @author skeggsc
 */
public final class TestTests extends BaseTest {

    @Override
    public String getName() {
        return "Meta-Test";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        Logger.info("Beginning meta-tests...");
        for (BaseTest t : new BaseTest[] { new SucceedTest(), new FailTest1(), new FailTest2(), new FailTest3(), new FailTest4(), new FailTest5(), new ExceptionTest() }) {
            boolean out = t.test(false);
            if (t.getName().startsWith("Virtual Failure")) {
                assertFalse(out, "Meta-testing failed!");
            } else {
                assertTrue(out, "Meta-testing failed!");
            }
        }
        boolean threwException = false;
        try {
            new InterruptedTest().test(false);
        } catch (InterruptedException ex) {
            threwException = true;
        }
        assertTrue(threwException, "Meta-testing failed!");
    }

    private static final class SucceedTest extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Success Test";
        }

        @Override
        protected void runTest() throws TestingException {
            assertTrue(true, "Oops! Meta-test bug!");
        }
    }

    private static final class FailTest1 extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Failure Test 1";
        }

        @Override
        protected void runTest() throws TestingException {
            assertTrue(false, "Great! That's correct.");
        }
    }

    private static final class FailTest2 extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Failure Test 2";
        }

        @Override
        protected void runTest() throws TestingException {
            assertFalse(true, "Great! That's correct.");
        }
    }

    private static final class FailTest3 extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Failure Test 3";
        }

        @Override
        protected void runTest() throws TestingException {
            assertIntsEqual(0, 17, "Great! That's correct.");
        }
    }

    private static final class FailTest4 extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Failure Test 4";
        }

        @Override
        protected void runTest() throws TestingException {
            assertIdentityEqual(new String("TEST"), "TEST", "Great! That's correct.");
        }
    }

    private static final class FailTest5 extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Failure Test 5";
        }

        @Override
        protected void runTest() throws TestingException {
            assertObjectEqual("TEST2", "TEST", "Great! That's correct.");
        }
    }

    private static final class InterruptedTest extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Interrupted Test";
        }

        @Override
        protected void runTest() throws TestingException, InterruptedException {
            throw new InterruptedException();
        }
    }

    private static final class ExceptionTest extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Failure: Exception Test";
        }

        @Override
        protected void runTest() throws TestingException, InterruptedException {
            throw new RuntimeException("This should fail like this. I think.");
        }
    }
}
