// Modified from WPILib version:
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2016. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.communication;

import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.hal.JNIWrapper;

@SuppressWarnings("javadoc")
public class FRCNetworkCommunicationsLibrary extends JNIWrapper {

    public static interface tTargetClass {
        public static final int kTargetClass_Unknown = 0x00;
        public static final int kTargetClass_FRC1 = 0x10;
        public static final int kTargetClass_FRC2 = 0x20;
        public static final int kTargetClass_FRC3 = 0x30;
        public static final int kTargetClass_RoboRIO = 0x40;
        public static final int kTargetClass_FamilyMask = 0xF0;
        public static final int kTargetClass_ModuleMask = 0x0F;
    };

    public static interface tResourceType {
        public static final int kResourceType_Controller = 0;
        public static final int kResourceType_Module = 1;
        public static final int kResourceType_Language = 2;
        public static final int kResourceType_CANPlugin = 3;
        public static final int kResourceType_Accelerometer = 4;
        public static final int kResourceType_ADXL345 = 5;
        public static final int kResourceType_AnalogChannel = 6;
        public static final int kResourceType_AnalogTrigger = 7;
        public static final int kResourceType_AnalogTriggerOutput = 8;
        public static final int kResourceType_CANJaguar = 9;
        public static final int kResourceType_Compressor = 10;
        public static final int kResourceType_Counter = 11;
        public static final int kResourceType_Dashboard = 12;
        public static final int kResourceType_DigitalInput = 13;
        public static final int kResourceType_DigitalOutput = 14;
        public static final int kResourceType_DriverStationCIO = 15;
        public static final int kResourceType_DriverStationEIO = 16;
        public static final int kResourceType_DriverStationLCD = 17;
        public static final int kResourceType_Encoder = 18;
        public static final int kResourceType_GearTooth = 19;
        public static final int kResourceType_Gyro = 20;
        public static final int kResourceType_I2C = 21;
        public static final int kResourceType_Framework = 22;
        public static final int kResourceType_Jaguar = 23;
        public static final int kResourceType_Joystick = 24;
        public static final int kResourceType_Kinect = 25;
        public static final int kResourceType_KinectStick = 26;
        public static final int kResourceType_PIDController = 27;
        public static final int kResourceType_Preferences = 28;
        public static final int kResourceType_PWM = 29;
        public static final int kResourceType_Relay = 30;
        public static final int kResourceType_RobotDrive = 31;
        public static final int kResourceType_SerialPort = 32;
        public static final int kResourceType_Servo = 33;
        public static final int kResourceType_Solenoid = 34;
        public static final int kResourceType_SPI = 35;
        public static final int kResourceType_Task = 36;
        public static final int kResourceType_Ultrasonic = 37;
        public static final int kResourceType_Victor = 38;
        public static final int kResourceType_Button = 39;
        public static final int kResourceType_Command = 40;
        public static final int kResourceType_AxisCamera = 41;
        public static final int kResourceType_PCVideoServer = 42;
        public static final int kResourceType_SmartDashboard = 43;
        public static final int kResourceType_Talon = 44;
        public static final int kResourceType_HiTechnicColorSensor = 45;
        public static final int kResourceType_HiTechnicAccel = 46;
        public static final int kResourceType_HiTechnicCompass = 47;
        public static final int kResourceType_SRF08 = 48;
    };

    public static interface tInstances {
        public static final int kLanguage_LabVIEW = 1;
        public static final int kLanguage_CPlusPlus = 2;
        public static final int kLanguage_Java = 3;
        public static final int kLanguage_Python = 4;
        public static final int kCANPlugin_BlackJagBridge = 1;
        public static final int kCANPlugin_2CAN = 2;
        public static final int kFramework_Iterative = 1;
        public static final int kFramework_Simple = 2;
        public static final int kRobotDrive_ArcadeStandard = 1;
        public static final int kRobotDrive_ArcadeButtonSpin = 2;
        public static final int kRobotDrive_ArcadeRatioCurve = 3;
        public static final int kRobotDrive_Tank = 4;
        public static final int kRobotDrive_MecanumPolar = 5;
        public static final int kRobotDrive_MecanumCartesian = 6;
        public static final int kDriverStationCIO_Analog = 1;
        public static final int kDriverStationCIO_DigitalIn = 2;
        public static final int kDriverStationCIO_DigitalOut = 3;
        public static final int kDriverStationEIO_Acceleration = 1;
        public static final int kDriverStationEIO_AnalogIn = 2;
        public static final int kDriverStationEIO_AnalogOut = 3;
        public static final int kDriverStationEIO_Button = 4;
        public static final int kDriverStationEIO_LED = 5;
        public static final int kDriverStationEIO_DigitalIn = 6;
        public static final int kDriverStationEIO_DigitalOut = 7;
        public static final int kDriverStationEIO_FixedDigitalOut = 8;
        public static final int kDriverStationEIO_PWM = 9;
        public static final int kDriverStationEIO_Encoder = 10;
        public static final int kDriverStationEIO_TouchSlider = 11;
        public static final int kADXL345_SPI = 1;
        public static final int kADXL345_I2C = 2;
        public static final int kCommand_Scheduler = 1;
        public static final int kSmartDashboard_Instance = 1;
    };

    public static native int FRCNetworkCommunicationUsageReportingReport(byte resource, byte instanceNumber, byte context, String feature);

    public static native void setNewDataSem(long mutexId);

    public static native void FRCNetworkCommunicationObserveUserProgramStarting();

    public static native void FRCNetworkCommunicationObserveUserProgramDisabled();

    public static native void FRCNetworkCommunicationObserveUserProgramAutonomous();

    public static native void FRCNetworkCommunicationObserveUserProgramTeleop();

    public static native void FRCNetworkCommunicationObserveUserProgramTest();

    public static native void FRCNetworkCommunicationReserve();

    public static final int HAL_ENABLED = 1 << 0, HAL_AUTONOMOUS = 1 << 1, HAL_TEST = 1 << 2, HAL_ESTOP = 1 << 3, HAL_FMS_ATTACHED = 1 << 4, HAL_DS_ATTACHED = 1 << 5;
    public static native int NativeHALGetControlWord();

    // 0 = Red1, Red2, Red3, Blue1, Blue2, Blue3, else null
    public static native int NativeHALGetAllianceStation();

    public static int kMaxJoystickAxes = 12;
    public static int kMaxJoystickPOVs = 12;

    public static native short[] HALGetJoystickAxes(byte joystickNum);

    public static native short[] HALGetJoystickPOVs(byte joystickNum);

    public static native int HALGetJoystickButtons(byte joystickNum, ByteBuffer count);

    public static native int HALSetJoystickOutputs(byte joystickNum, int outputs, short leftRumble, short rightRumble);

    public static native int HALGetJoystickIsXbox(byte joystickNum);

    public static native int HALGetJoystickType(byte joystickNum);

    public static native String HALGetJoystickName(byte joystickNum);

    public static native int HALGetJoystickAxisType(byte joystickNum, byte axis);

    public static native float HALGetMatchTime();

    public static native boolean HALGetSystemActive();

    public static native boolean HALGetBrownedOut();

    public static native int HALSetErrorData(String error);
}
