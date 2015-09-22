package org.slf4j.impl;

import org.slf4j.ILoggerFactory;

import ccre.log.CCRELoggerFactory;

/**
 * Implementation of StaticLoggerBinder
 */
public class StaticLoggerBinder {

    /**
     * The unique instance of this class.
     */
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    /**
     * The unique instance of CCRELoggerFactory.
     */
    private static final CCRELoggerFactory CCRE_FACTORY = new CCRELoggerFactory();

    /**
     * Return the singleton of this class.
     * 
     * @return the StaticLoggerBinder singleton
     */
    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.7.12"; // !final

    private StaticLoggerBinder() {

    }

    /**
     * @return An immutable instance of {@link CCRELoggerFactory}
     */
    public ILoggerFactory getLoggerFactory() {
        return CCRE_FACTORY;
    }

    /**
     * @return The class name of {@link CCRELoggerFactory}
     */
    public String getLoggerFactoryClassStr() {
        return CCRE_FACTORY.getClass().getName();
    }
}
