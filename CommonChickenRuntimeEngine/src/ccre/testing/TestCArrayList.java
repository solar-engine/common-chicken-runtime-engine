package ccre.testing;

import ccre.util.CArrayList;
import java.util.Iterator;

/**
 * A test that tests some parts of the CArrayList class.
 *
 * @author skeggsc
 */
public class TestCArrayList extends BaseTest {

    @Override
    public String getName() {
        return "CArrayList basic test";
    }

    @Override
    protected void runTest() throws TestingException {
        CArrayList<Object> arr = new CArrayList<Object>();
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
