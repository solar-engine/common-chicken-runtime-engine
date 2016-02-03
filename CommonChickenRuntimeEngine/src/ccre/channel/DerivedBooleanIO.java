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
package ccre.channel;

/**
 * A DerivedBooleanIO is the same as a DerivedBooleanInput, but the implementer
 * additionally implements the {@link #set(boolean)} method of BooleanOutput.
 *
 * @author skeggsc
 * @see DerivedBooleanInput
 */
public abstract class DerivedBooleanIO extends DerivedBooleanInput implements BooleanIO {
    /**
     * Creates a derived BooleanIO that may update when anything in
     * <code>updates</code> is changed.
     *
     * @param updates the UpdatingInputs to monitor.
     * @see DerivedBooleanInput#DerivedBooleanInput(UpdatingInput...)
     */
    public DerivedBooleanIO(UpdatingInput... updates) {
        super(updates);
    }
}
