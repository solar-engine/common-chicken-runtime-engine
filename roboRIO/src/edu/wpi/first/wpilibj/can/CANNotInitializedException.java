// Certain modifications are Copyright 2016 Colby Skeggs
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008-2016. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.can;

/**
 * Exception indicating that the CAN driver layer has not been initialized. This
 * happens when an entry-point is called before a CAN driver plugin has been
 * installed.
 */
@SuppressWarnings({ "javadoc", "serial" })
public class CANNotInitializedException extends RuntimeException {
    public CANNotInitializedException() {
        super();
    }
}
