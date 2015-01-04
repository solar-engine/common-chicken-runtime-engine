package java.lang;

public class NoSuchFieldError extends Error { // Should really be IncompatibleClassChangeError
    // Do nothing. This really is only used for one minimal internal usage, which is allowing enum switch statement compilation.
    private NoSuchFieldError() {
    }
}
