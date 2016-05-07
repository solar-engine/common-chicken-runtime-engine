/*
 * Copyright 2015-2016 Cel Skeggs
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

import ccre.channel.BooleanCell;
import ccre.channel.BooleanIO;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.verifier.SetupPhase;

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
    @SetupPhase
    public void addBoolean(String name, BooleanOutput output);

    /**
     * Provide a BooleanInput controllable by the user under the specified name.
     *
     * @param name the name of the control binding.
     * @return a BooleanInput controlled by the user
     */
    @SetupPhase
    public BooleanInput addBoolean(String name);

    /**
     * Make the specified FloatOutput controllable by the user under the
     * specified name.
     *
     * @param name the name of the control binding.
     * @param output the output to let the user control.
     */
    @SetupPhase
    public void addFloat(String name, FloatOutput output);

    /**
     * Provide a FloatInput controllable by the user under the specified name.
     *
     * @param name the name of the control binding.
     * @return a FloatInput controlled by the user
     */
    @SetupPhase
    public FloatInput addFloat(String name);

    /**
     * Make the specified EventOutput controllable by the user under the
     * specified name. (This will be mapped to a boolean button internally.)
     *
     * @param name the name of the control binding.
     * @param output the output to let the user control.
     */
    @SetupPhase
    public default void addEvent(String name, EventOutput output) {
        addBoolean(name, BooleanOutput.polarize(null, output));
    }

    /**
     * Provide an EventInput controllable by the user under the specified name.
     * (This will be mapped to a boolean button internally.)
     *
     * @param name the name of the control binding.
     * @return an EventInput controlled by the user
     */
    @SetupPhase
    public default EventInput addEvent(String name) {
        return addBoolean(name).onPress();
    }

    /**
     * Make the specified BooleanIO controllable by the user under the specified
     * three names by toggling and setting true and false. (This will be mapped
     * to boolean button press events internally.)
     *
     * @param setTrue the binding name for setting true.
     * @param setFalse the binding name for setting false.
     * @param toggle the binding name for toggling.
     * @param io the IO to let the user control.
     */
    @SetupPhase
    public default void addToggleButton(String setTrue, String setFalse, String toggle, BooleanIO io) {
        addEvent(setTrue, io.eventSet(true));
        addEvent(setFalse, io.eventSet(false));
        addEvent(toggle, io.eventToggle());
    }

    /**
     * Make the specified BooleanIO controllable by the user under the specified
     * name by toggling and setting true and false. (This will be mapped to
     * three boolean button press events internally.)
     *
     * @param name the name of the control binding.
     * @param io the IO to let the user control.
     */
    @SetupPhase
    public default void addToggleButton(String name, BooleanIO io) {
        addToggleButton(name + " Set", name + " Reset", name + " Toggle", io);
    }

    /**
     * Provide a BooleanIO controllable by the user under the specified three
     * names by toggling and setting true and false. (This will be mapped to
     * three boolean button press events internally.)
     *
     * @param setTrue the binding name for setting true.
     * @param setFalse the binding name for setting false.
     * @param toggle the binding name for toggling.
     * @return an EventInput controlled by the user
     */
    @SetupPhase
    public default BooleanIO addToggleButton(String setTrue, String setFalse, String toggle) {
        BooleanIO out = new BooleanCell();
        addToggleButton(setTrue, setFalse, toggle, out);
        return out;
    }

    /**
     * Provide a BooleanIO controllable by the user under the specified name by
     * toggling and setting true and false. (This will be mapped to three
     * boolean button press events internally.)
     *
     * @param name the name of the control binding.
     * @return an EventInput controlled by the user
     */
    @SetupPhase
    public default BooleanIO addToggleButton(String name) {
        BooleanIO out = new BooleanCell();
        addToggleButton(name, out);
        return out;
    }
}
