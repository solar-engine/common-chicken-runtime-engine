/*
 * Copyright 2014 Colby Skeggs, Connor Hansen, Gregor Peach
 * 
 * This file is part of the ApolloGemini2014 project.
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
package org.team1540.apollogemini;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;

public class TestMode {

    public final BooleanInputPoll inTest;

    public TestMode(BooleanInputPoll inTest) {
        this.inTest = inTest;
    }

    private static String testify(String name) {
        return "test_" + name;
    }

    public BooleanStatus testPublish(String s, BooleanStatus b) {
        s = testify(s);
        Cluck.publish(s + ".input", (BooleanInput) b);
        testPublish(s + ".output", (BooleanOutput) b);
        return b;
    }

    public BooleanInput testPublish(String s, BooleanInput b) {
        Cluck.publish(testify(s), (BooleanInput) b);
        return b;
    }

    public BooleanOutput testPublish(String s, final BooleanOutput b) {
        Cluck.publish(testify(s), new BooleanOutput() {
            public void set(boolean bln) {
                if (inTest.get()) {
                    b.set(bln);
                }
            }
        });
        return b;
    }

    public FloatStatus testPublish(String s, FloatStatus b) {
        s = testify(s);
        Cluck.publish(s + ".input", (FloatInput) b);
        testPublish(s + ".output", (FloatOutput) b);
        return b;
    }

    public FloatInput testPublish(String s, FloatInput b) {
        Cluck.publish(testify(s), (FloatInput) b);
        return b;
    }

    public FloatOutput testPublish(String s, final FloatOutput b) {
        Cluck.publish(testify(s), new FloatOutput() {
            public void set(float bln) {
                if (inTest.get()) {
                    b.set(bln);
                }
            }
        });
        return b;
    }

    public EventOutput testPublish(String s, final EventOutput o) {
        Cluck.publish(testify(s), new EventOutput() {
            public void event() {
                if (inTest.get()) {
                    o.event();
                }
            }
        });
        return o;
    }

    public EventInput testPublish(String s, final EventInput o) {
        Cluck.publish(testify(s), o);
        return o;
    }

    public void addDriveMotors(FloatOutput leftDrive1, FloatOutput leftDrive2, FloatOutput leftDrive, FloatOutput rightDrive1, FloatOutput rightDrive2, FloatOutput rightDrive) {
        testPublish("leftDrive1", leftDrive1);
        testPublish("leftDrive2", leftDrive2);
        testPublish("rightDrive1", rightDrive1);
        testPublish("rightDrive2", rightDrive2);
        testPublish("leftDrive", leftDrive);
        testPublish("rightDrive", rightDrive);
    }
}
