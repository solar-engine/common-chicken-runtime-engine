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

import ccre.util.CArrayList;
import ccre.util.CArrayUtils;

/**
 * A test that tests the CArrayList class.
 *
 * @author skeggsc
 */
public class TestCArrayList extends BaseTestList {

    @Override
    public String getName() {
        return "CArrayList test";
    }

    @Override
    protected void runTest() throws TestingException {
        super.runTest(new CArrayList<String>());
        super.runTest(new CArrayList<String>(72));
        CArrayList<String> test = new CArrayList<String>(CArrayUtils.asList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));
        assertObjectEqual(test.toString(), "[Alpha, Beta, Gamma, Delta, Epsilon]", "Invalid constructor-loaded array!");
        test = new CArrayList<String>(new String[] { "Alpha", "Beta", "Gamma", "Delta", "Epsilon" });
        assertObjectEqual(test.toString(), "[Alpha, Beta, Gamma, Delta, Epsilon]", "Invalid constructor-loaded array!");
    }
}
