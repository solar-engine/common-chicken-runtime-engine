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
package org.team1540.example;

import ccre.chan.FloatOutput;
import ccre.obsidian.ObsidianCore;

public class Example extends ObsidianCore {

    @Override
    protected void createRobotControl() {
        FloatOutput o = makePWMOutput("P9_14", 0, 0.333f, 0.666f, 333f, true); // These parameters may need tuning.
        o.writeValue(0f);
    }
}
