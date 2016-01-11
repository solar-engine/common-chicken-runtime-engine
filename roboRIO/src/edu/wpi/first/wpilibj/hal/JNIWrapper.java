// Certain modifications are Copyright 2016 Colby Skeggs
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2016. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.hal;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * base class for all JNI wrappers
 */
@SuppressWarnings("javadoc")
public class JNIWrapper {
    static File jniLibrary = null;

    static {
        try {
            jniLibrary = File.createTempFile("libwpilibJavaJNI", ".so");
            jniLibrary.deleteOnExit();

            // Note: the source for this library is available in WPILib, of
            // course.
            try (InputStream is = JNIWrapper.class.getResourceAsStream("/edu/wpi/first/wpilibj/binaries/libwpilibJavaJNI.so")) {
                if (is == null) {
                    throw new RuntimeException("Could not initialize JNIWrapper: missing shared object in Jar.");
                }
                Files.copy(is, jniLibrary.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            System.load(jniLibrary.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static native long getPortWithModule(byte module, byte pin);

    public static native long getPort(byte pin);

    public static native void freePort(long port_pointer);
}
