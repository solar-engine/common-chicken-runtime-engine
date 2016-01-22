/*
 * Copyright 2015-2016 Alexander Mackworth
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
package ccre.deployment.eggs;

import ccre.deployment.Artifact;

/**
 * An interface describing a task to be run upon the hatching of a CCRE Egg. In
 * general, should call DepEgg.deploy() or DepEgg.emulate().
 * 
 * @see DepEgg
 * @author amackworth
 */
public interface ArtifactDeployer {
    public abstract void deployArtifact(Artifact artifact) throws Throwable;
}
