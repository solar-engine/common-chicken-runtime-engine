package org.team1540.geminiapollo;

import ccre.cluck.Cluck;
import ccre.channel.*;

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
