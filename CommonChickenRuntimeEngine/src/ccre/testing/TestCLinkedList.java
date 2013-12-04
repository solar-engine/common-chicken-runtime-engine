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

import ccre.util.CArrayUtils;
import ccre.util.CLinkedList;

/**
 * A test that the CLinkedList class.
 *
 * @author skeggsc
 */
public class TestCLinkedList extends BaseTestList {

    @Override
    public String getName() {
        return "CLinkedList test";
    }

    @Override
    protected void runTest() throws TestingException {
        super.runTest(new CLinkedList<String>());
        CLinkedList<String> test = new CLinkedList<String>(CArrayUtils.asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));
        assertEqual(test.toString(), "[Alpha, Beta, Gamma, Delta, Epsilon]", "Invalid constructor-loaded array!");
        test = new CLinkedList<String>(new String[]{"Alpha", "Beta", "Gamma", "Delta", "Epsilon"});
        assertEqual(test.toString(), "[Alpha, Beta, Gamma, Delta, Epsilon]", "Invalid constructor-loaded array!");
        assertEqual(test.getFirst(), "Alpha", "Bad getFirst!");
        assertEqual(test.getLast(), "Epsilon", "Bad getLast!");
        test.addFirst("Null");
        assertEqual(test.getFirst(), "Null", "Bad addFirst!");
        assertEqual(test.removeFirst(), "Null", "Bad removeFirst!");
        test.addLast("Suffix");
        assertEqual(test.getLast(), "Suffix", "Bad addLast!");
        assertEqual(test.removeLast(), "Suffix", "Bad removeLast!");
        assertEqual(test.removeLast(), "Epsilon", "Bad removeLast!");
        assertEqual(test.removeFirst(), "Alpha", "Bad removeFirst!");
        test.addLast("Epsilon");
        test.addFirst("Alpha");
        assertEqual(test.toString(), "[Alpha, Beta, Gamma, Delta, Epsilon]", "List contents corrupted!");
    }
}
