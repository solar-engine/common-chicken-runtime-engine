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
