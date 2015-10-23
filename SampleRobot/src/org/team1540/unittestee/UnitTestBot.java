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
package org.team1540.unittestee;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventCell;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatCell;
import ccre.cluck.Cluck;
import ccre.frc.FRCApplication;
import ccre.log.LogLevel;

/**
 * A deployable robot used for Cluck autotesting. Works with the associated
 * PoultryInspector component.
 *
 * @author skeggsc
 */
public class UnitTestBot implements FRCApplication {

    @Override
    public void setupRobot() {
        String targetRoot = "pinspector/"; // TODO: make this work again.
        // Note: currently incomplete.
        BooleanCell b = new BooleanCell();
        FloatCell f = new FloatCell();
        EventCell e = new EventCell();
        Cluck.publish("utest-os0", Cluck.subscribeOS(targetRoot + "utest-os1"));
        Cluck.publish("utest-lt0", Cluck.subscribeLT(targetRoot + "utest-lt1"));
        Cluck.publish("utest-fo0", (FloatOutput) f);
        Cluck.publish("utest-fi0", (FloatInput) f);
        Cluck.publish("utest-bo0", (BooleanOutput) b);
        Cluck.publish("utest-bi0", (BooleanInput) b);
        Cluck.publish("utest-eo0", (EventOutput) e);
        Cluck.publish("utest-ei0", (EventInput) e);
    }
}
