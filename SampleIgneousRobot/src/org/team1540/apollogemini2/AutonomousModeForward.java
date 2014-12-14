/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 * 
 * This file is part of the Revised ApolloGemini2014 project.
 * 
 * ApolloGemini2014 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * ApolloGemini2014 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ApolloGemini2014.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.team1540.apollogemini2;

import ccre.channel.FloatInput;
import ccre.holders.TuningContext;
import ccre.instinct.AutonomousModeOverException;

public class AutonomousModeForward extends AutonomousModeBase {

    public AutonomousModeForward() {
        super("forward");
    }

    private FloatInput movement, delay;

    @Override
    protected void runAutonomous() throws InterruptedException, AutonomousModeOverException {
        driveFor(movement, delay);
    }

    @Override
    public void loadSettings(TuningContext context) {
        movement = context.getFloat("autom-forward-speed", -1f);
        delay = context.getFloat("autom-forward-delay", 0.5f);
    }
}
