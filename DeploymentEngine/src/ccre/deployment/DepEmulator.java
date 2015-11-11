/*
 * Copyright 2015 Colby Skeggs.
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
package ccre.deployment;

import ccre.frc.DeviceListMain;

/**
 * An interface to the Emulator that allows for running an emulator instance on
 * user code.
 *
 * @author skeggsc
 */
public class DepEmulator {
    /**
     * Emulate the specified user code.
     *
     * @param userCode the Artifact for the user code.
     * @throws Exception if an error occurs while setting up emulation.
     */
    public static void emulate(Artifact userCode) throws Exception {
        DeviceListMain.startEmulator(userCode.toJar(false).toFile(), DepProject.directoryOrCreate("emulator-logs"));
    }
}
