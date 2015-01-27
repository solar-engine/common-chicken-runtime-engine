package org.team1540.example.deep;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.EventMixing;
import ccre.igneous.Igneous;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConf;
import ccre.rconf.RConfable;

public class DriveCode implements RConfable {

    private final FloatInputPoll leftAxis = Igneous.joystick1.getAxisChannel(2);
    private final FloatInputPoll rightAxis = Igneous.joystick1.getAxisChannel(5);
    private final FloatOutput leftOut = Igneous.makeTalonMotor(2, Igneous.MOTOR_FORWARD, 0.1f);
    private final FloatOutput rightOut = Igneous.makeTalonMotor(1, Igneous.MOTOR_REVERSE, 0.1f);
    private final BooleanStatus allowToRun = new BooleanStatus(), forceEnabled = new BooleanStatus();
    private final BooleanInputPoll shouldBeRunning = BooleanMixing.andBooleans(allowToRun, BooleanMixing.orBooleans(Igneous.getIsTeleop(), forceEnabled));
    
    public DriveCode() {
        DriverImpls.createSynchTankDriver(EventMixing.filterEvent(shouldBeRunning, true, Igneous.globalPeriodic),
                leftAxis, rightAxis, leftOut, rightOut);
    }

    public Entry[] queryRConf() throws InterruptedException {
        return new Entry[] {RConf.title("Drive Code"),
                RConf.string("Axes:"), RConf.fieldFloat(leftAxis), RConf.fieldFloat(rightAxis),
                RConf.string("Allow To Run:"), RConf.fieldBoolean(allowToRun),
                RConf.string("Force To Run:"), RConf.fieldBoolean(forceEnabled)};
    }

    public void signalRConf(int field, byte[] data) throws InterruptedException {
        switch (field) {
        case 2:
            if (data.length >= 4) {
                leftOut.set(RConf.bytesToFloat(data));
            }
            break;
        case 3:
            if (data.length >= 4) {
                rightOut.set(RConf.bytesToFloat(data));
            }
            break;
        case 5:
            allowToRun.set(data.length > 0 && data[0] != 0);
            break;
        case 7:
            forceEnabled.set(data.length > 0 && data[0] != 0);
            break;
        }
    }
}
