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
 * The suite of tests to run. This will run all of the current tests.
 *
 * This needs more tests! There are a lot of untested parts of the code, and the
 * tests that do exist are incomplete!
 *
 * @author skeggsc
 */
public class SuiteOfTests {

    // TODO: This package needs more tests!
    /**
     * Run all the tests.
     *
     * @param args the application arguments. these are ignored.
     * @throws java.lang.InterruptedException If the main thread is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        BaseTest[] tests = new BaseTest[] {
                // ccre.chan
        new TestBooleanStatus(), new TestFloatStatus(),
                // ccre.cluck - Tests needed!

                // ccre.concurrency
                // CollapsingWorkerThread - Needed!
                // ConcurrentDispatchArray - Needed to improve!
        new TestConcurrentDispatchArray(), new TestReporterThread(),
                // ReporterThread - Needed!

                // ccre.ctrl
                // DriverImpls - Needed!
                // ExpirationTimer - Needed!
                // Mixing - Needed to be fleshed out a lot!
        new TestMixing(),
                // ModeDispatcher - Needed!
                // MultipleSourceBooleanController - Needed!
        new TestTicker(),
                // ccre.event
        new TestEvent(),
                // EventLogger - Needed!

                // ccre.holders - Needed!

                // ccre.instinct - Needed!

                // ccre.launcher - Not needed.

                // ccre.log - Needed!

                // ccre.net - Needed!

                // ccre.phidget
                // PhidgetReader - Needed!

                // ccre.rload - Do I need this?

                // ccre.saver - Needed!

                // ccre.testing - DONE
        new TestTests(),
                // ccre.utils - DONE
        new TestAllocationPool(),
                // CAbstractList - Included in CArrayList and CLinkedList tests
        new TestCArrayList(), new TestCHashMap(), new TestCLinkedList(),
                // Heap - Not needed because it's a commented-out unfinished class.
        new TestUtils(), // Tests both Utils and CArrayUtils
        // ccre.workarounds - Needed!
        };
        int count = 0;
        for (BaseTest bt : tests) {
            if (bt.test()) {
                count++;
            }
        }
        if (count < tests.length) {
            Logger.warning(count + "/" + tests.length + " tests succeeded.");
            Logger.warning("Read above to see which tests failed.");
        } else {
            Logger.info(count + "/" + tests.length + " tests succeeded.");
        }
    }

    private SuiteOfTests() {
    }
}
