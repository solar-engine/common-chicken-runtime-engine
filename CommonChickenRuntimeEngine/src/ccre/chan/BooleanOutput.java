/*
 * Copyright 2013-2014 Colby Skeggs
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
package ccre.chan;

/**
 * A BooleanOutput is an interface for anything that can be turned on or off. It
 * can be set to true or false.
 *
 * @see BooleanInput
 * @author skeggsc
 */
public interface BooleanOutput {

    /**
     * Set the boolean value of this output. In other words, turn it on or off.
     *
     * @param value The new value to send to this output.
     */
    public void set(boolean value);
}
