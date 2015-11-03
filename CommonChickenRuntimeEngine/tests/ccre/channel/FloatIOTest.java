/*
 * Copyright 2015 Colby Skeggs
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
package ccre.channel;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FloatIOTest {

    private FloatCell cell;

    @Before
    public void setUp() throws Exception {
        this.cell = new FloatCell();
    }

    @After
    public void tearDown() throws Exception {
        this.cell = null;
    }

    @Test
    public void testAsOutput() {
        assertEquals(cell, cell.asOutput());
    }

    @Test
    public void testAsInput() {
        assertEquals(cell, cell.asInput());
    }
}
