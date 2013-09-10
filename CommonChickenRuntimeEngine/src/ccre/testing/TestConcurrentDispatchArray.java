package ccre.testing;

import ccre.concurrency.ConcurrentDispatchArray;
import java.util.Iterator;

/**
 * A test that tests some parts of the ConcurrentDispatchArray class.
 *
 * @author skeggsc
 */
public class TestConcurrentDispatchArray extends BaseTest {

    @Override
    public String getName() {
        return "ConcurrentDispatchArray basic test";
    }

    @Override
    protected void runTest() throws TestingException {
        ConcurrentDispatchArray<Object> arr = new ConcurrentDispatchArray<Object>();
        Object test = new Object();
        assertFalse(arr.contains(test), "Bad contains!");
        arr.add(test);
        assertTrue(arr.contains(test), "Bad contains!");
        Iterator itr = arr.iterator();
        assertTrue(itr.hasNext(), "Bad iterator!");
        assertEqual(itr.next(), test, "Bad iterator!");
        assertFalse(itr.hasNext(), "Bad iterator!");
        arr.remove(test);
        assertFalse(arr.contains(test), "Bad remove!");
    }
}
