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

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatFilter;
import ccre.channel.FloatInput;
import ccre.ctrl.IJoystick;
import ccre.igneous.Igneous;
import ccre.phidget.PhidgetReader;

public class UserInterface {
    private static final IJoystick driveJoystick = Igneous.joystick1;
    private static final FloatFilter deadzone = FloatFilter.deadzone(0.1f);

    public static FloatInput getLeftAxis() {
        return deadzone.wrap(driveJoystick.axis(2).negated());
    }

    public static FloatInput getRightAxis() {
        return deadzone.wrap(driveJoystick.axis(6).negated());
    }

    public static FloatInput getForwardAxis() {
        return deadzone.wrap(driveJoystick.axis(4).minus(driveJoystick.axis(3)));
    }

    public static EventInput getHighGearShift() {
        return driveJoystick.onPress(1);
    }

    public static EventInput getLowGearShift() {
        return driveJoystick.onPress(3);
    }

    public static EventInput getArmLower() {
        return PhidgetReader.getDigitalInput(0).onPress();
    }

    public static EventInput getArmRaise() {
        return PhidgetReader.getDigitalInput(5).onPress();
    }

    public static EventInput getArmAlign() {
        return PhidgetReader.getDigitalInput(7).onPress();
    }

    public static BooleanOutput getArmDownLight() {
        return PhidgetReader.getDigitalOutput(0);
    }

    public static BooleanOutput getArmUpLight() {
        return PhidgetReader.getDigitalOutput(1);
    }

    public static BooleanInput getRollersInSwitch() {
        return PhidgetReader.getDigitalInput(3);
    }

    public static BooleanInput getRollersOutSwitch() {
        return PhidgetReader.getDigitalInput(4);
    }

    public static EventInput getFireButton() {
        return PhidgetReader.getDigitalInput(1).onPress().or(driveJoystick.onPress(6).and(ApolloGemini.isKidMode));
    }

    public static EventInput getRearmCatapult() {
        return PhidgetReader.getDigitalInput(2).onPress();
    }

    public static void showFiring(BooleanInput notFiring) {
        notFiring.send(PhidgetReader.getDigitalOutput(2));
    }

    public static BooleanInput getShouldOverrideDrivingDisable() {
        return driveJoystick.button(7).or(driveJoystick.button(8));
    }
}
