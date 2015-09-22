// Modified from WPILib version
// No license was specified, but the standard FIRST BSD was implied.

package edu.wpi.first.wpilibj.hal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * base class for all JNI wrappers
 */
public class JNIWrapper {
    static File jniLibrary = null;

    static {
        try {
            jniLibrary = File.createTempFile("libwpilibJavaJNI", ".so");
            jniLibrary.deleteOnExit();

            byte[] buffer = new byte[4096];
            int readBytes;

            // Note: the source for this library is available in WPILib, of course.
            InputStream is = JNIWrapper.class.getResourceAsStream("/edu/wpi/first/wpilibj/binaries/libwpilibJavaJNI.so");
            if (is == null) {
                throw new RuntimeException("Could not initialize JNIWrapper: missing shared object in Jar.");
            }
            OutputStream os = new FileOutputStream(jniLibrary);
            try {
                while ((readBytes = is.read(buffer)) != -1) {
                    os.write(buffer, 0, readBytes);
                }
            } finally {
                os.close();
                is.close();
            }

            System.load(jniLibrary.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static native ByteBuffer getPort(byte pin);
}
