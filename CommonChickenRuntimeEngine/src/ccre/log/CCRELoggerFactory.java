package ccre.log;

import org.slf4j.ILoggerFactory;

public class CCRELoggerFactory implements ILoggerFactory {

    private static final CCRELogger ccreLogger = new CCRELogger("");

    public CCRELogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    @Override
    public CCRELogger getLogger(String classname) {
        return ccreLogger;
    }

    public static CCRELogger getLogger() {
        return ccreLogger;
    }
}
