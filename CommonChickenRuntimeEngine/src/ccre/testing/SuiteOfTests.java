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
        // ccre.chan
        new TestBooleanStatus().test();
        new TestFloatStatus().test();

        // ccre.cluck - Tests needed!

        // ccre.concurrency
        // CollapsingWorkerThread - Needed!
        // ConcurrentDispatchArray - Needed to improve!
        new TestConcurrentDispatchArray().test();
        // ReporterThread - Needed!

        // ccre.ctrl
        // DriverImpls - Needed!
        // ExpirationTimer - Needed!
        // Mixing - Needed!
        // ModeDispatcher - Needed!
        // MultipleSourceBooleanController - Needed!
        // Ticker - Needed!

        // ccre.event
        new TestEvent().test();
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

        // ccre.testing
        new TestTests().test();

        // ccre.utils
        new TestAllocationPool().test();
        // AllocationPool - Needed!
        // CAbstractList - Included in CArrayList and CLinkedList tests
        new TestCArrayList().test();
        new TestCHashMap().test();
        new TestCLinkedList().test();
        // Heap - Not needed because it's a commented-out unfinished class.
        new TestUtils().test(); // Tests both Utils and CArrayUtils

        // ccre.workarounds - Needed!
    }
}
