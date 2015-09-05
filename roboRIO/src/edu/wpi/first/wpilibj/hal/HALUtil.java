package edu.wpi.first.wpilibj.hal;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class HALUtil extends JNIWrapper {
    public static final int NULL_PARAMETER = -1005;
    public static final int SAMPLE_RATE_TOO_HIGH = 1001;
    public static final int VOLTAGE_OUT_OF_RANGE = 1002;
    public static final int LOOP_TIMING_ERROR = 1004;
    public static final int INCOMPATIBLE_STATE = 1015;
    public static final int ANALOG_TRIGGER_PULSE_OUTPUT_ERROR = -1011;
    public static final int NO_AVAILABLE_RESOURCES = -104;
    public static final int PARAMETER_OUT_OF_RANGE = -1028;

    public static native ByteBuffer initializeMutexNormal();

    public static native void deleteMutex(ByteBuffer sem);

    public static native byte takeMutex(ByteBuffer sem);

    public static native ByteBuffer initializeMultiWait();

    public static native void deleteMultiWait(ByteBuffer sem);

    public static native byte takeMultiWait(ByteBuffer sem, ByteBuffer m, int timeOut);

    public static native short getFPGAVersion(IntBuffer status);

    public static native int getFPGARevision(IntBuffer status);

    public static native long getFPGATime(IntBuffer status);

    public static native boolean getFPGAButton(IntBuffer status);

    public static native String getHALErrorMessage(int code);

    public static native int getHALErrno();

    public static native String getHALstrerror(int errno);

    public static String getHALstrerror() {
        return getHALstrerror(getHALErrno());
    }

}
