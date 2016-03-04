/*
 * Copyright 2016 Cel Skeggs
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
package ccre.behaviors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ccre.util.Values;

@SuppressWarnings("javadoc")
public class BehaviorTest {

    @Test
    public void testGetName() {
        for (String name : Values.getRandomStrings(30)) {
            assertEquals(name, new Behavior(null, null, name).getName());
        }
    }

    @Test
    public void testToString() {
        for (String arbitrator_name : Values.getRandomStrings(10)) {
            for (String name : Values.getRandomStrings(10)) {
                assertEquals("[Behavior " + arbitrator_name + "." + name + "]", new Behavior(new BehaviorArbitrator(arbitrator_name), null, name).toString());
            }
        }
    }
}
