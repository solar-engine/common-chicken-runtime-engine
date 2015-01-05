package java.lang;

/**
 * This a substitute for java.lang.SuppressWarnings for Squawk. Does nothing
 * useful whatsoever except stuff can work.
 *
 * @see java.lang.Override
 * @author skeggsc
 */
public @interface SuppressWarnings {

    @SuppressWarnings("javadoc")
    String[] value();

}
