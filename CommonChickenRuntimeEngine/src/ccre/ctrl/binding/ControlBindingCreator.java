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
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;

/**
 * A ControlBindingCreator is an abstract mechanism by which a program or module
 * can make available a set of booleans and/or floats that it wants to be
 * controlled by the user.
 *
 * @author skeggsc
 */
public interface ControlBindingCreator {
    /**
     * Make the specified BooleanOutput controllable by the user under the
     * specified name.
     *
     * @param name the name of the control binding.
     * @param output the output to let the user control.
     */
    public void addBoolean(String name, BooleanOutput output);

    /**
     * Provide a BooleanInput controllable by the user under the specified name.
     *
     * @param name the name of the control binding.
     * @return a BooleanInput controlled by the user
     */
    public BooleanInput addBoolean(String name);

    /**
     * Make the specified FloatOutput controllable by the user under the
     * specified name.
     *
     * @param name the name of the control binding.
     * @param output the output to let the user control.
     */
    public void addFloat(String name, FloatOutput output);

    /**
     * Provide a FloatInput controllable by the user under the specified name.
     *
     * @param name the name of the control binding.
     * @return a FloatInput controlled by the user
     */
    public FloatInput addFloat(String name);
}
