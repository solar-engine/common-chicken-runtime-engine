/*
 * Copyright 2014 Colby Skeggs
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
package ccre.igneous;

/**
 * A class that holds the current Igneous Launcher. This is done because the
 * launcher variable must be set before the Igneous class is initialized.
 *
 * @author skeggsc
 */
class IgneousLauncherHolder {

    private static IgneousLauncher launcher;

    /**
     * @return the launcher
     */
    static IgneousLauncher getLauncher() {
        if (launcher == null) {
            throw new RuntimeException("No Igneous launcher! Are you on an Igneous platform? There should be a registered launcher before ccre.igneous.Igneous is initialized, or this error will occur.");
        }
        return launcher;
    }

    /**
     * @param aLauncher the launcher to set
     */
    static void setLauncher(IgneousLauncher aLauncher) {
        launcher = aLauncher;
    }

    private IgneousLauncherHolder() {
    }
}
