/*
 * Copyright 2013 Colby Skeggs and Vincent Miller
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
package ccre.obsidian;

import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanOutput;
import ccre.event.Event;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.cluck.CluckGlobals;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.log.MultiTargetLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Vincent Miller
 */
public class EmulatorLauncher implements ObsidianLauncher {

    /**
     * The settings loaded during the launch process.
     */
    public static Properties settings;
    private String leftMotorPin = "P8_14";
    private String rightMotorPin = "P8_16";
    private FloatOutput leftMotor;
    private FloatOutput rightMotor;

    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        new EmulatorLauncher();
    }

    public EmulatorLauncher() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        leftMotor = new FloatOutput() {
            @Override
            public void writeValue(float value) {
                System.out.println("Left motor: " + value);
            }
        };
        rightMotor = new FloatOutput() {
            @Override
            public void writeValue(float value) {
                System.out.println("Right motor: " + value);
            }
        };
        CluckGlobals.ensureInitializedCore();
        Logger.target = new MultiTargetLogger(new LoggingTarget[]{Logger.target, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger")});
        Properties p = new Properties();
        InputStream inst = ObsidianLauncherImpl.class.getResourceAsStream("/obsidian-conf.properties");
        if (inst == null) {
            throw new IOException("Could not find configuration file!");
        }
        p.load(inst);
        settings = p;
        String name = p.getProperty("Obsidian-Main");
        if (name == null) {
            throw new IOException("Could not find configuration-specified launchee!");
        }
        ObsidianCore core = (ObsidianCore) Class.forName(name).newInstance();
        core.properties = p;
        core.launcher = new EmulatorLauncher();
        CluckGlobals.initializeServer(80);
        final Event prd = new Event();
        core.periodic = prd;
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    prd.produce();
                } catch (Throwable thr) {
                    Logger.log(LogLevel.SEVERE, "Exception caught in execution loop - robots don't quit!", thr);
                }
            }
        }, 10, 20);
        try {
            core.createRobotControl();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Exception caught at top level during initialization - robots don't quit!", thr);
        }
    }

    @Override
    public BooleanOutput makeGPIOOutput(int chan, boolean defaultValue) {
        throw new UnsupportedOperationException("GPIO not supported in emulator.");
    }

    @Override
    public BooleanInputPoll makeGPIOInput(int chan, boolean pullSetting) {
        throw new UnsupportedOperationException("GPIO not supported in emulator.");
    }

    @Override
    public FloatOutput makePWMOutput(String chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity) {
        if (chan.equals(leftMotorPin)) {
            return leftMotor;
        } else if (chan.equals(rightMotorPin)) {
            return rightMotor;
        } else {
            return null;
        }
    }

    @Override
    public void destroyPWMOutput(String chan) {
    }

    @Override
    public FloatInputPoll makeAnalogInput(int chan) {
        throw new UnsupportedOperationException("Analog not supported in emulator.");
    }
}
