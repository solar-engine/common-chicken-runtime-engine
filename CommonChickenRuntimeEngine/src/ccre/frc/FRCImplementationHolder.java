/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.frc;

/**
 * A class that holds the current FRCImplementation. This is done because the
 * implementation variable must be available before the FRC class is
 * initialized.
 *
 * @author skeggsc
 */
public class FRCImplementationHolder {

    private static FRCImplementation impl;

    /**
     * @return the implementation
     */
    public static FRCImplementation getImplementation() {
        if (impl == null) {
            throw new RuntimeException("No FRC implementation! Are you on an FRC platform? There should be a registered implementation before ccre.frc.FRC is initialized, or this error will occur.");
        }
        return impl;
    }

    /**
     * @param implementation the implementation to set
     */
    public static void setImplementation(FRCImplementation implementation) {
        impl = implementation;
    }

    private FRCImplementationHolder() {
    }
}
