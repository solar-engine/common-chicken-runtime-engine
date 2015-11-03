/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008-2012. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.can;

public class CANExceptionFactory {
    // From NIRioStatus
    public static final int kRioStatusOffset = -63000;

    public static final int kRioStatusSuccess = 0;
    public static final int kRIOStatusBufferInvalidSize = kRioStatusOffset - 80;
    public static final int kRIOStatusOperationTimedOut = -52007;
    public static final int kRIOStatusFeatureNotSupported = kRioStatusOffset - 193;
    public static final int kRIOStatusResourceNotInitialized = -52010;
    // FRC Error codes
    static final int ERR_CANSessionMux_InvalidBuffer = -44086;
    static final int ERR_CANSessionMux_MessageNotFound = -44087;
    static final int ERR_CANSessionMux_NotAllowed = -44088;
    static final int ERR_CANSessionMux_NotInitialized = -44089;

    public static void checkStatus(int status, int messageID) {
        switch (status) {
        case kRioStatusSuccess:
            // Everything is ok... don't throw.
            break;
        case ERR_CANSessionMux_InvalidBuffer:
        case kRIOStatusBufferInvalidSize:
            throw new RuntimeException("Invalid CAN buffer! Typically, this is due to a buffer being too small to include the needed safety token.");
        case ERR_CANSessionMux_MessageNotFound:
        case kRIOStatusOperationTimedOut:
            throw new CANMessageNotFoundException();
        case ERR_CANSessionMux_NotAllowed:
        case kRIOStatusFeatureNotSupported:
            throw new RuntimeException("CAN Message not allowed: MessageID = " + messageID);
        case ERR_CANSessionMux_NotInitialized:
        case kRIOStatusResourceNotInitialized:
            throw new RuntimeException("CAN not initialized! This is equivalent to a CANNotInitializedException.");
        default:
            throw new RuntimeException("Fatal CAN status code detected: " + status);
        }
    }
}
