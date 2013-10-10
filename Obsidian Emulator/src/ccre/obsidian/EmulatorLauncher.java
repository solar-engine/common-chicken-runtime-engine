/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian;

import ccre.obsidian.comms.Radio;
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
    
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean watch = false;
        if (args.length != 0) {
            if (!args[0].equals("use-watcher")) {
                ccre.launcher.Launcher.main(args);
                return;
            }
            watch = true;
        }
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
        if (watch) {
            final File watchee = new File("remote-watcher");
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (watchee.exists()) {
                        watchee.delete();
                        Logger.info("Shutting down due to watcher notification.");
                        System.exit(0);
                    }
                }
            }, 500, 1000);
        }
        try {
            core.createRobotControl();
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Exception caught at top level during initialization - robots don't quit!", thr);
        }
    }
    
    @Override
    public BooleanOutput makeGPIOOutput(int chan, boolean defaultValue) {
        return null;
    }
    
    @Override
    public BooleanInputPoll makeGPIOInput(int chan, boolean pullSetting) {
        return null;
    }
    
    @Override
    public FloatOutput makePWMOutput(String chan, float defaultValue, final float calibrateN1, final float calibrateN2, float frequency, boolean zeroPolarity) {
        return null;
    }
    
    @Override
    public void destroyPWMOutput(String chan) {
    }
    
    @Override
    public FloatInputPoll makeAnalogInput(int chan) {
        return null;
    }
    
    @Override
    public Radio makeRadio(int usb) {
        return null;
    }
}
