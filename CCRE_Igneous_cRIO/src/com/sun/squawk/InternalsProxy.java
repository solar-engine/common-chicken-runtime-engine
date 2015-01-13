package com.sun.squawk;

/**
 * Provides access for the CCRE to some of the package-private Squawk internals.
 * 
 * @author skeggsc
 */
public class InternalsProxy {
    /**
     * Get the Method object behind the specified ExecutionPoint, if it exists.
     * 
     * This is provided because com.sun.squawk.ExecutionPoint.getMethod is
     * broken during the build process. (No, I'm not quite sure why.)
     * 
     * @param ep the ExecutionPoint to access.
     * @return the Method object.
     * @see com.sun.squawk.ExecutionPoint#getMethod()
     */
    public static Method getMethod(ExecutionPoint ep) {
        return ep.getKlass().findMethod(ep.mp);
    }
}
