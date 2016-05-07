/*
 * Copyright 2015 Cel Skeggs
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

import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;
import ccre.verifier.SetupPhase;

/**
 * An interface to a module that provides a named set of boolean and float
 * outputs that it wants to be controlled by the user in some fashion.
 *
 * This can be used with a CluckControlBinder to allow a user to customize how
 * they want to control these outputs.
 *
 * For example, this could be a Drive Code module, and this interface would
 * provide access to outputs like "shift high button" and "forward drive axis".
 *
 * @author skeggsc
 */
public interface ControlBindingDataSink {
    /**
     * Enumerate the boolean outputs of this module.
     *
     * @return a list of the names of the boolean outputs.
     */
    @SetupPhase
    public String[] listBooleans();

    /**
     * Access a specified boolean output.
     *
     * @param name the name of the boolean output.
     * @return the discovered output.
     */
    @SetupPhase
    public BooleanOutput getBoolean(String name);

    /**
     * Enumerate the float outputs of this module.
     *
     * @return a list of the names of the float outputs.
     */
    @SetupPhase
    public String[] listFloats();

    /**
     * Access a specified float output.
     *
     * @param name the name of the float output.
     * @return the discovered output.
     */
    @SetupPhase
    public FloatOutput getFloat(String name);
}
