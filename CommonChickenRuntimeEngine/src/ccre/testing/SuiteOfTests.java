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
 * The suite of tests to run. This will run all of the current tests.
 *
 * This needs more tests! There are a lot of untested parts of the code, and the
 * tests that do exist are incomplete!
 *
 * @author skeggsc
 */
public class SuiteOfTests { // TODO: This package needs more tests!

    private SuiteOfTests() {
    }

    /**
     * Run all the tests.
     *
     * @param args the application arguments. these are ignored.
     */
    public static void main(String[] args) {
        new TestTests().test();
        new TestCArrayList().test();
        new TestConcurrentDispatchArray().test();
        new TestEvent().test();
        new TestCHashMapPartial().test();

        // ccre.chan
        new TestBooleanStatus().test();
    }
}
