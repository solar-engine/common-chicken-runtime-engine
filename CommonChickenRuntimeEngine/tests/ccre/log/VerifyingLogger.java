/*
 * Copyright 2016 Colby Skeggs
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
package ccre.log;

import java.util.Arrays;

public class VerifyingLogger {

    private static VerifyingLoggingTarget vlt;
    private static LoggingTarget[] targets;
    private static boolean oldLineNumbers;

    public static VerifyingLoggingTarget get() {
        return vlt;
    }

    public static void configure(LogLevel level, String message, Throwable thr) {
        vlt.configureThrowable(level, message, thr);
    }

    public static void configureExt(LogLevel level, String message, String str) {
        vlt.configureString(level, message, str);
    }
    
    public static void check() {
        vlt.check();
    }

    public static synchronized void begin() {
        if (vlt != null) {
            throw new IllegalStateException();
        }
        vlt = new VerifyingLoggingTarget();
        targets = Logger.targets.toArray(new LoggingTarget[Logger.targets.size()]);
        // race condition but it's fine because this is only testing
        Logger.targets.clear();
        Logger.addTarget(vlt);

        oldLineNumbers = Logger.getShouldIncludeLineNumbers();
        Logger.setShouldIncludeLineNumbers(false);
    }

    public static synchronized void end() {
        if (vlt == null) {
            throw new IllegalStateException();
        }
        Logger.setShouldIncludeLineNumbers(oldLineNumbers);
        
        Logger.removeTarget(vlt);
        Logger.targets.addAll(Arrays.asList(targets));
        vlt = null;
    }
}
