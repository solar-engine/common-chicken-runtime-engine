package ccre.log;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.Marker;

import ccre.util.CallerInfo;
import ccre.util.Utils;

public class CCRELogger implements Logger {

    private final String classname;
    private final CopyOnWriteArrayList<LoggingTarget> targets = new CopyOnWriteArrayList<>();

    private LogLevel lowestLevelEnabled = LogLevel.TRACE;
    private boolean includeLineNumbers = true;

    /**
     * Constructs a CCRELogger that corresponds to the class of the name
     * specified. Package scope, as this constructor should only be called by
     * {@link CCRELoggerFactory#getLogger(String)}.
     */
    CCRELogger(String clazzname) {
        classname = clazzname;
        targets.add(new StandardStreamLogger());
    }

    /**
     * Set whether or not filenames and line numbers should be prefixed to
     * logging messages, when available.
     *
     * @param shouldInclude if this debugging info should be included.
     */
    public void setShouldIncludeLineNumbers(boolean shouldInclude) {
        includeLineNumbers = shouldInclude;
    }

    /**
     * Get whether or not filenames and line numbers are prefixed to logging
     * messages, when available.
     *
     * @return shouldInclude if this debugging info is included.
     */
    public boolean getShouldIncludeLineNumbers() {
        return includeLineNumbers;
    }

    /**
     * Add the specified target to the list of targets.
     *
     * @param lt The target to add.
     */
    public synchronized void addTarget(LoggingTarget lt) {
        targets.add(lt);
    }

    /**
     * Remove the specified target from the list of targets.
     *
     * @param lt The target to remove.
     */
    public synchronized void removeTarget(LoggingTarget lt) {
        targets.remove(lt);
    }

    private String prependCallerInfo(int index, String message) {
        if (includeLineNumbers && !message.startsWith("(") && !message.startsWith("[")) {
            CallerInfo caller = Utils.getMethodCaller(index + 1);
            if (caller != null && caller.getFileName() != null) {
                if (caller.getLineNum() > 0) {
                    return "(" + caller.getFileName() + ":" + caller.getLineNum() + ") " + message;
                } else {
                    return "(" + caller.getFileName() + ") " + message;
                }
            }
        }

        return message;
    }

    private void logInternal(LogLevel level, String message, Throwable thr) {
        if (level == null || message == null) {
            throw new NullPointerException();
        }
        message = prependCallerInfo(2, message); // 2 = offset to function that
                                                 // calls logger
        for (LoggingTarget lt : targets) {
            lt.log(level, message, thr);
        }
    }

    private String format(String format, Object... args) {
        // TODO: optimize this, right now it's O(format.length * args.length),
        // should be O(format.length)
        // Warning: this implementation is /slow/ but we don't care right now
        // because it works :)

        for (Object obj : args) {
            String tempformat = format.replaceFirst("\\{\\}", obj.toString());
            if (tempformat.contentEquals(format))
                throw new IllegalArgumentException("Too many arguments: " + args.length + "!");
            format = tempformat;
        }

        if (format.length() != format.replaceFirst("\\{\\}", "").length()) {
            throw new IllegalArgumentException("Too few arguments: " + args.length + "!");
        }

        return format;
    }

    @Override
    public String getName() {
        return classname;
    }

    @Override
    public boolean isTraceEnabled() {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.TRACE) <= 0;
    }

    @Override
    public void trace(String msg) {
        if (!isTraceEnabled())
            return;
        logInternal(LogLevel.TRACE, msg, null);
    }

    @Override
    public void trace(String format, Object arg) {
        if (!isTraceEnabled())
            return;
        logInternal(LogLevel.TRACE, format(format, arg), null);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (!isTraceEnabled())
            return;
        logInternal(LogLevel.TRACE, format(format, arg1, arg2), null);
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (!isTraceEnabled())
            return;
        logInternal(LogLevel.TRACE, format(format, arguments), null);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (!isTraceEnabled())
            return;
        logInternal(LogLevel.TRACE, msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.TRACE) <= 0;
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (!isTraceEnabled(marker))
            return;
        logInternal(LogLevel.TRACE, msg, null);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (!isTraceEnabled(marker))
            return;
        logInternal(LogLevel.TRACE, format(format, arg), null);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (!isTraceEnabled(marker))
            return;
        logInternal(LogLevel.TRACE, format(format, arg1, arg2), null);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (!isTraceEnabled(marker))
            return;
        logInternal(LogLevel.TRACE, format(format, argArray), null);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (!isTraceEnabled(marker))
            return;
        logInternal(LogLevel.TRACE, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.DEBUG) <= 0;
    }

    @Override
    public void debug(String msg) {
        if (!isDebugEnabled())
            return;
        logInternal(LogLevel.DEBUG, msg, null);
    }

    @Override
    public void debug(String format, Object arg) {
        if (!isDebugEnabled())
            return;
        logInternal(LogLevel.DEBUG, format(format, arg), null);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (!isDebugEnabled())
            return;
        logInternal(LogLevel.DEBUG, format(format, arg1, arg2), null);
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (!isDebugEnabled())
            return;
        logInternal(LogLevel.DEBUG, format(format, arguments), null);
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (!isDebugEnabled())
            return;
        logInternal(LogLevel.DEBUG, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.DEBUG) <= 0;
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (!isDebugEnabled(marker))
            return;
        logInternal(LogLevel.DEBUG, msg, null);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (!isDebugEnabled(marker))
            return;
        logInternal(LogLevel.DEBUG, format(format, arg), null);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (!isDebugEnabled(marker))
            return;
        logInternal(LogLevel.DEBUG, format(format, arg1, arg2), null);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        if (!isDebugEnabled(marker))
            return;
        logInternal(LogLevel.DEBUG, format(format, arguments), null);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (!isDebugEnabled(marker))
            return;
        logInternal(LogLevel.DEBUG, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.INFO) <= 0;
    }

    @Override
    public void info(String msg) {
        if (!isInfoEnabled())
            return;
        logInternal(LogLevel.INFO, msg, null);
    }

    @Override
    public void info(String format, Object arg) {
        if (!isInfoEnabled())
            return;
        logInternal(LogLevel.INFO, format(format, arg), null);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (!isInfoEnabled())
            return;
        logInternal(LogLevel.INFO, format(format, arg1, arg2), null);
    }

    @Override
    public void info(String format, Object... arguments) {
        if (!isInfoEnabled())
            return;
        logInternal(LogLevel.INFO, format(format, arguments), null);
    }

    @Override
    public void info(String msg, Throwable t) {
        if (!isInfoEnabled())
            return;
        logInternal(LogLevel.INFO, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.INFO) <= 0;
    }

    @Override
    public void info(Marker marker, String msg) {
        if (!isInfoEnabled(marker))
            return;
        logInternal(LogLevel.INFO, msg, null);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (!isInfoEnabled(marker))
            return;
        logInternal(LogLevel.INFO, format(format, arg), null);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (!isInfoEnabled(marker))
            return;
        logInternal(LogLevel.INFO, format(format, arg1, arg2), null);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        if (!isInfoEnabled(marker))
            return;
        logInternal(LogLevel.INFO, format(format, arguments), null);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (!isInfoEnabled(marker))
            return;
        logInternal(LogLevel.INFO, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.WARN) <= 0;
    }

    @Override
    public void warn(String msg) {
        if (!isWarnEnabled())
            return;
        logInternal(LogLevel.WARN, msg, null);
    }

    @Override
    public void warn(String format, Object arg) {
        if (!isWarnEnabled())
            return;
        logInternal(LogLevel.WARN, format(format, arg), null);
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (!isWarnEnabled())
            return;
        logInternal(LogLevel.WARN, format(format, arguments), null);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (!isWarnEnabled())
            return;
        logInternal(LogLevel.WARN, format(format, arg1, arg2), null);
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (!isWarnEnabled())
            return;
        logInternal(LogLevel.WARN, msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.WARN) <= 0;
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (!isWarnEnabled(marker))
            return;
        logInternal(LogLevel.WARN, msg, null);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (!isWarnEnabled(marker))
            return;
        logInternal(LogLevel.WARN, format(format, arg), null);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (!isWarnEnabled(marker))
            return;
        logInternal(LogLevel.WARN, format(format, arg1, arg2), null);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        if (!isWarnEnabled(marker))
            return;
        logInternal(LogLevel.WARN, format(format, arguments), null);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (!isWarnEnabled(marker))
            return;
        logInternal(LogLevel.WARN, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.ERROR) <= 0;
    }

    @Override
    public void error(String msg) {
        if (!isErrorEnabled())
            return;
        logInternal(LogLevel.ERROR, msg, null);

    }

    @Override
    public void error(String format, Object arg) {
        if (!isErrorEnabled())
            return;
        logInternal(LogLevel.ERROR, format(format, arg), null);

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (!isErrorEnabled())
            return;
        logInternal(LogLevel.ERROR, format(format, arg1, arg2), null);

    }

    @Override
    public void error(String format, Object... arguments) {
        if (!isErrorEnabled())
            return;
        logInternal(LogLevel.ERROR, format(format, arguments), null);

    }

    @Override
    public void error(String msg, Throwable t) {
        if (!isErrorEnabled())
            return;
        logInternal(LogLevel.ERROR, msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        // if lowestLevelEnabled is less than or equal to this
        return lowestLevelEnabled.compareTo(LogLevel.ERROR) <= 0;
    }

    @Override
    public void error(Marker marker, String msg) {
        if (!isErrorEnabled(marker))
            return;
        logInternal(LogLevel.ERROR, msg, null);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (!isErrorEnabled(marker))
            return;
        logInternal(LogLevel.ERROR, format(format, arg), null);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (!isErrorEnabled(marker))
            return;
        logInternal(LogLevel.ERROR, format(format, arg1, arg2), null);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        if (!isErrorEnabled(marker))
            return;
        logInternal(LogLevel.ERROR, format(format, arguments), null);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (!isErrorEnabled(marker))
            return;
        logInternal(LogLevel.ERROR, msg, t);
    }

    public static void logByLevel(Logger logger, LogLevel level, String message, Throwable throwable) {
        switch (level.id) {
        case LogLevel.FATAL_ID:
            logger.error(message, throwable);
            break;
        case LogLevel.ERROR_ID:
            logger.error(message, throwable);
            break;
        case LogLevel.WARN_ID:
            logger.warn(message, throwable);
            break;
        case LogLevel.INFO_ID:
            logger.info(message, throwable);
            break;
        case LogLevel.DEBUG_ID:
            logger.debug(message, throwable);
            break;
        case LogLevel.TRACE_ID:
            logger.trace(message, throwable);
            break;
        }
    }

    public static void logByLevel(Logger logger, LogLevel level, String message, String extended) {
        switch (level.id) {
        case LogLevel.FATAL_ID:
            logger.error(message, extended);
            break;
        case LogLevel.ERROR_ID:
            logger.error(message, extended);
            break;
        case LogLevel.WARN_ID:
            logger.warn(message, extended);
            break;
        case LogLevel.INFO_ID:
            logger.info(message, extended);
            break;
        case LogLevel.DEBUG_ID:
            logger.debug(message, extended);
            break;
        case LogLevel.TRACE_ID:
            logger.trace(message, extended);
            break;
        }
    }

    public static void logByLevel(Logger logger, LogLevel level, String message) {
        switch (level.id) {
        case LogLevel.FATAL_ID:
            logger.error(message);
            break;
        case LogLevel.ERROR_ID:
            logger.error(message);
            break;
        case LogLevel.WARN_ID:
            logger.warn(message);
            break;
        case LogLevel.INFO_ID:
            logger.info(message);
            break;
        case LogLevel.DEBUG_ID:
            logger.debug(message);
            break;
        case LogLevel.TRACE_ID:
            logger.trace(message);
            break;
        }
    }
}
