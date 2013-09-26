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

/**
 * A test that tests that tests work correctly! This is, as you can tell, very
 * important!
 *
 * @author skeggsc
 */
public final class TestTests extends BaseTest {

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

    private static final class FailTest extends BaseTest {

        @Override
        public String getName() {
            return "Virtual Failure Test";
        }

        @Override
        protected void runTest() throws TestingException {
            assertTrue(false, "Great! That's correct.");
            throw new RuntimeException("Nope! Testing failed!");
        }
    }

    @Override
    public String getName() {
        return "Meta-Test";
    }

    @Override
    protected void runTest() throws TestingException {
        boolean out = new SucceedTest().test(false);
        assertTrue(out, "Meta-testing failed!");
        if (out != true) {
            throw new RuntimeException("Meta-testing failed!");
        }
        out = new FailTest().test(false);
        assertFalse(out, "Meta-testing failed!");
        if (out != false) {
            throw new RuntimeException("Meta-testing failed!");
        }
    }
}
