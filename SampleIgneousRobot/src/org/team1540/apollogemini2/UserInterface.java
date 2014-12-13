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
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatFilter;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.EventMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystick;
import ccre.igneous.Igneous;
import ccre.phidget.PhidgetReader;

public class UserInterface {
    private static final IJoystick driveJoystick = Igneous.joystick1;
    private static final FloatFilter deadzone = FloatMixing.deadzone(0.1f);

    public static FloatInputPoll getLeftAxis() {
        return deadzone.wrap(FloatMixing.negate(driveJoystick.getAxisChannel(2)));
    }

    public static FloatInputPoll getRightAxis() {
        return deadzone.wrap(FloatMixing.negate(driveJoystick.getAxisChannel(6)));
    }

    public static FloatInputPoll getForwardAxis() {
        final FloatInputPoll positive = driveJoystick.getAxisChannel(3), negative = driveJoystick.getAxisChannel(4);
        return deadzone.wrap(FloatMixing.negate(FloatMixing.subtraction.of(positive, negative)));
    }

    public static EventInput getHighGearShift() {
        return driveJoystick.getButtonSource(1);
    }

    public static EventInput getLowGearShift() {
        return driveJoystick.getButtonSource(3);
    }

    public static EventInput getArmLower() {
        return BooleanMixing.onPress(PhidgetReader.getDigitalInput(0));
    }

    public static EventInput getArmRaise() {
        return BooleanMixing.onPress(PhidgetReader.getDigitalInput(5));
    }

    public static EventInput getArmAlign() {
        return BooleanMixing.onPress(PhidgetReader.getDigitalInput(7));
    }

    public static BooleanOutput getArmDownLight() {
        return PhidgetReader.getDigitalOutput(0);
    }

    public static BooleanOutput getArmUpLight() {
        return PhidgetReader.getDigitalOutput(1);
    }

    public static BooleanInputPoll getRollersInSwitch() {
        return PhidgetReader.getDigitalInput(3);
    }

    public static BooleanInputPoll getRollersOutSwitch() {
        return PhidgetReader.getDigitalInput(4);
    }

    public static EventInput getFireButton() {
        return EventMixing.combine(
                BooleanMixing.onPress(PhidgetReader.getDigitalInput(1)),
                EventMixing.filterEvent(ApolloGemini.isKidMode, false,
                        driveJoystick.getButtonSource(6)));
    }

    public static EventInput getRearmCatapult() {
        return BooleanMixing.onPress(PhidgetReader.getDigitalInput(2));
    }

    public static void showFiring(BooleanInput notFiring) {
        notFiring.send(PhidgetReader.getDigitalOutput(2));
    }

    public static BooleanInputPoll getShouldOverrideDrivingDisable() {
        return BooleanMixing.orBooleans(
                driveJoystick.getButtonChannel(7),
                driveJoystick.getButtonChannel(8));
    }
}
