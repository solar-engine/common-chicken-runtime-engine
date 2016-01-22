/*
 * Copyright 2016 Colby Skeggs
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

import ccre.channel.BooleanInput;

/**
 * A named behavior state belonging to a specific Behavior Arbitrator.
 *
 * @see BehaviorArbitrator
 *
 * @author skeggsc
 */
public final class Behavior {
    final BooleanInput request;
    final BehaviorArbitrator parent;
    private final String name;

    Behavior(BehaviorArbitrator parent, BooleanInput request, String name) {
        this.parent = parent;
        this.request = request;
        this.name = name;
    }

    /**
     * Gets the name assigned to this Behavior when it was created.
     *
     * @return the behavior's name.
     */
    public String getName() {
        return name;
    }

    public String toString() {
        return "[Behavior " + parent.getName() + "." + name + "]";
    }
}
