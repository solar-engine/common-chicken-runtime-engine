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
    }
}
