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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import ccre.util.CHashMap;

/**
 * A test that tests the CHashMap class.
 *
 * @author skeggsc
 */
public class TestCHashMap extends BaseTest {

    @Override
    public String getName() {
        return "CHashMap test";
    }

    private CHashMap<String, String> subject;
    private int expectedKeyCount;

    private void checkContains(String what, boolean does) throws TestingException {
        if (does) {
            assertFalse(subject.get(what) == null, "Bad existent element!");
            assertTrue(subject.containsKey(what), "Bad contains!");
        } else {
            assertObjectEqual(subject.get(what), null, "Bad nonexistent element!");
            assertFalse(subject.containsKey(what), "Bad contains!");
        }
    }

    @SuppressWarnings("unused")
    private void checkInvariants(int relCount) throws TestingException {
        checkContains("never", false);
        int keyCount = 0;
        for (String key : subject) {
            keyCount++;
        }
        assertIntsEqual(keyCount, subject.size(), "Bad keycount!");
        if (keyCount == 0) {
            assertTrue(subject.isEmpty(), "Bad isEmpty!");
        } else {
            assertFalse(subject.isEmpty(), "Bad isEmpty!");
        }
        expectedKeyCount += relCount;
        assertIntsEqual(keyCount, expectedKeyCount, "Bad active size!");
    }

    private void checkPut(String key, String value, String old) throws TestingException {
        checkInvariants(0);
        checkContains(key, old != null);
        assertObjectEqual(subject.put(key, value), old, "Bad put!");
        assertObjectEqual(subject.get(key), value, "Bad put!");
        if (value == null) {
            assertTrue(subject.containsKey(key), "Bad contains!");
        } else {
            checkContains(key, true);
        }
        checkInvariants(old == null ? 1 : 0);
    }

    private void checkRemove(String key, String value) throws TestingException {
        checkInvariants(0);
        checkContains(key, true);
        assertObjectEqual(subject.remove(key), value, "Bad remove!");
        checkContains(key, false);
        checkInvariants(-1);
    }

    private void checkAlreadyRemoved(String key) throws TestingException {
        checkInvariants(0);
        checkContains(key, false);
        assertObjectEqual(subject.remove(key), null, "Bad remove!");
        checkContains(key, false);
        checkInvariants(0);
    }

    private void checkGet(String key, String value) throws TestingException {
        checkInvariants(0);
        if (value == null) {
            assertTrue(subject.containsKey(key), "Bad contains!");
        } else {
            checkContains(key, true);
        }
        assertObjectEqual(subject.get(key), value, "Bad get!");
    }

    private void checkNull(String key) throws TestingException {
        checkInvariants(0);
        checkContains(key, false);
        assertObjectEqual(subject.get(key), null, "Bad get!");
    }

    private void checkNulls(String... keys) throws TestingException {
        for (String key : keys) {
            checkNull(key);
        }
    }

    @Override
    protected void runTest() throws TestingException {
        subject = new CHashMap<String, String>();
        testCHashMap();
        subject = new CHashMap<String, String>(10);
        testCHashMap();
        try {
            new CHashMap<String, String>(-10000);
            assertFail("did not get an IAE for negative size!");
        } catch (IllegalArgumentException ex) {
            // correct!
        }
        try {
            new CHashMap<String, String>(-1);
            assertFail("did not get an IAE for negative size!");
        } catch (IllegalArgumentException ex) {
            // correct!
        }
        try {
            new CHashMap<String, String>(0);
            assertFail("did not get an IAE for zero size!");
        } catch (IllegalArgumentException ex) {
            // correct!
        }
        subject = new CHashMap<String, String>(1);
        testCHashMap();
    }

    private void testCHashMap() throws TestingException {
        expectedKeyCount = 0;
        testGettingAndPutting();

        testClearing(-1);

        testRandomized();
        testRemovalNormal();
        testClearing(-1);
        testRemovalIterative();
        testClearing(-1);
        testModcounts();
        testClearing(-2);
        testModcountsOnLoose();
        testClearing(-1);
        testWeirdKeys();
    }

    private void testModcounts() throws TestingException {
        checkPut("Test", "ok", null);
        checkPut("Test2", "not ok", null);
        checkPut("Test3", "okay", null);
        checkContains("Test", true);
        {
            Iterator<String> iterator = subject.iterator();
            try {
                iterator.remove();
                assertFail("remove() before next() should have failed!");
            } catch (IllegalStateException ex) {
                // correct!
            }
            String lastKey = iterator.next();
            checkRemove(lastKey, subject.get(lastKey));
            try {
                iterator.next();
                assertFail("Failed to check concurrent modification!");
            } catch (ConcurrentModificationException ex) {
                // correct!
            }
            try {
                iterator.remove();
                assertFail("Failed to check concurrent modification!");
            } catch (ConcurrentModificationException ex) {
                // correct!
            }
        }
        checkInvariants(0);
    }

    private void testModcountsOnLoose() throws TestingException {
        checkPut("Test", "ok", null);
        checkPut("Test2", "not ok", null);
        checkPut("Test3", "okay", null);
        checkContains("Test", true);
        {
            Iterator<String> iterator = subject.looseIterator();
            String lastKey = iterator.next();
            checkRemove(lastKey, subject.get(lastKey));
            iterator.next(); // should not cause an exception, because this is a loose iterator
            iterator.next(); // should also not cause an exception, because this should be the third item
            iterator.remove(); // should ALSO not cause an exception
            assertFalse(iterator.hasNext(), "bad number of entries in iterator");
            try {
                iterator.next();
                assertFail("Should have error'd!");
            } catch (NoSuchElementException ex) {
                // correct!
            }
        }
        checkInvariants(-1);
    }

    private void testRemovalIterative() throws TestingException {
        checkPut("Test", "ok", null);
        checkPut("Test2", "not ok", null);
        checkContains("Test", true);
        int count = 0;
        for (Iterator<String> iterator = subject.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            if (key.equals("Test")) {
                iterator.remove();
                count++;
            }
        }
        assertIntsEqual(count, 1, "Bad remove count!");
        checkContains("Test", false);
        checkInvariants(-1);
        checkGet("Test2", "not ok");
        checkAlreadyRemoved("Test");
    }

    private void testRemovalNormal() throws TestingException {
        checkPut("Test", "ok", null);
        checkPut("Test2", "not ok", null);
        checkRemove("Test", "ok");
        checkGet("Test2", "not ok");
        checkAlreadyRemoved("Test");
    }

    private void testClearing(int rel) throws TestingException {
        subject.clear();
        checkInvariants(rel);
        assertIntsEqual(expectedKeyCount, 0, "count mismatch");
    }

    private void testGettingAndPutting() throws TestingException {
        checkInvariants(0);
        checkPut("test", "of course", null);
        checkPut("test2", "secondary", null);
        checkGet("test", "of course");
        checkGet("test2", "secondary");
        checkNull("test3");
        checkPut("test", "replacement", "of course");
        checkGet("test2", "secondary");
        checkNull("test3");
        subject.clear();
        checkInvariants(-2);
        assertIntsEqual(expectedKeyCount, 0, "count mismatch");
        checkNulls("test", "test2", "test3");
        checkPut("other", "ten", null);
        checkPut("other", null, "ten");
        checkGet("other", null);
    }

    private void testWeirdKeys() throws TestingException {
        checkPut("random", "which should hopefully have a negative hashCode", null);
        // cause the hashMap to resize itself
        for (int i = 0; i < 1000; i++) {
            subject.put(Integer.toString(i), "a");
        }
        for (int i = 0; i < 1000; i++) {
            subject.remove(Integer.toString(i));
        }
        try {
            subject.containsKey(null);
            assertFail("should have failed due to nullity!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            subject.get(null);
            assertFail("should have failed due to nullity!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            subject.put(null, "hello");
            assertFail("should have failed due to nullity!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            subject.put(null, null);
            assertFail("should have failed due to nullity!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            subject.remove(null);
            assertFail("should have failed due to nullity!");
        } catch (NullPointerException ex) {
            // correct!
        }
    }

    private void testRandomized() throws TestingException {
        String[] randoms = { "abcd", "aoeu", "pickles", "overstatement", "understudy", "arrays", "backwards", "sdrawkcab", "additional", "foo", "bar", "1540", "413", "612", "1025", "1111", "yes that is", "many-sided-pickle-adventurer", "chickens", "ignited", "pi", "are you even reading these by now?", "long string!!!!!!......//////_______ hahahahah", "I hope that this works.", "I could do brainstorming in here.", "Finishing up...", "green", "violet", "magenta", "purple", "indigo", "cerulean", "cobalt", "cheese", "potatos", "nitrates", "sodium", "carbohydrates", "snakes", "office", "pocketbook" };
        String[] keys = new String[10];
        String[] values = new String[keys.length];
        Random r = new Random();

        for (int i = 0; i < keys.length; i++) {
            String want;
            do {
                want = randoms[r.nextInt(randoms.length)];
                for (int j = 0; j < i; j++) {
                    if (want.equals(keys[j])) {
                        want = null;
                        break;
                    }
                }
            } while (want == null);
            keys[i] = want;
            values[i] = randoms[r.nextInt(randoms.length)];
        }
        CHashMap<String, String> testee = new CHashMap<String, String>();
        for (int i = 0; i < keys.length; i++) {
            testee.put(keys[i], values[i]);
        }
        for (int i = 0; i < keys.length; i++) {
            assertIdentityEqual(testee.get(keys[i]), values[i], "HashMap returned wrong value!");
        }
        boolean[] found = new boolean[keys.length];
        for (String s : testee) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i].equals(s)) {
                    assertFalse(found[i], "Already found that key!");
                    found[i] = true;
                }
            }
        }
        for (boolean b : found) {
            assertTrue(b, "Did not complete all keys!");
        }
        subject.clear();
        checkInvariants(0);
    }
}
