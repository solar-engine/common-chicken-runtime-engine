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
package ccre.ctrl.binding;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;

/**
 * A ControlBindingDataSource is an interface to a named set of boolean and
 * float inputs, so that a user can configure which of these inputs controls
 * what part of their robot. CluckControlBinder is usually used with one of
 * these.
 *
 * @author skeggsc
 */
public interface ControlBindingDataSource {
    /**
     * Enumerate the boolean inputs that are available through this interface.
     *
     * @return an array of names of inputs.
     */
    public String[] listBooleans();

    /**
     * Get access to a specific boolean input.
     *
     * @param name the input's name.
     * @return the input.
     */
    public BooleanInput getBoolean(String name);

    /**
     * Enumerate the float inputs that are available through this interface.
     *
     * @return an array of names of inputs.
     */
    public String[] listFloats();

    /**
     * Get access to a specific float input.
     *
     * @param name the input's name.
     * @return the input.
     */
    public FloatInput getFloat(String name);
}
