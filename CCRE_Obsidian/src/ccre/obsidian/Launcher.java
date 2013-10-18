/*
 * Copyright 2013 Colby Skeggs
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

import ccre.cluck.CluckGlobals;
import ccre.event.Event;
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
 * The Obsidian launcher. This is the class that is ran by the Java virtual
 * machine.
 *
 * @author skeggsc
 */
public class Launcher {

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
        Logger.warning("Could not start logger!");
        //Logger.target = new MultiTargetLogger(new LoggingTarget[]{Logger.target, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, "general-logger")});
        Properties p = new Properties();
        InputStream inst = Launcher.class.getResourceAsStream("/obsidian-conf.properties");
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
        CluckGlobals.setupServer();
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
}
