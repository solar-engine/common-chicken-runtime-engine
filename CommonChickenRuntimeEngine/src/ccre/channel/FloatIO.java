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

/**
 * A FloatIO is both a FloatInput and a FloatOutput.
 * 
 * @author skeggsc
 */
public interface FloatIO extends FloatInput, FloatOutput {

    /**
     * Returns the output side of this FloatIO. This is equivalent to upcasting
     * to FloatOutput.
     *
     * @return this status, as an output.
     */
    public default FloatOutput asOutput() {
        return this;
    }

    /**
     * Returns the input side of this FloatIO. This is equivalent to upcasting
     * to FloatInput.
     *
     * @return this status, as an input.
     */
    public default FloatInput asInput() {
        return this;
    }
}
