/*
 * Copyright 2015 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.igneous.direct;

import java.nio.ByteBuffer;

import ccre.concurrency.ReporterThread;
import edu.wpi.first.wpilibj.communication.FRCNetworkCommunicationsLibrary;
import edu.wpi.first.wpilibj.hal.HALUtil;

class DirectDriverStation {
    public static final int JOYSTICK_NUM = 6;
    public static final int AXIS_NUM = FRCNetworkCommunicationsLibrary.kMaxJoystickAxes;
    public static final int POV_NUM = FRCNetworkCommunicationsLibrary.kMaxJoystickPOVs;

    public static final int[] buttons = new int[JOYSTICK_NUM];
    public static final int[] buttonCounts = new int[JOYSTICK_NUM];
    public static final short[][] axes = new short[JOYSTICK_NUM][];
    public static final short[][] povs = new short[JOYSTICK_NUM][];

    public static void init() {
        ReporterThread comm = new ReporterThread("DriverStationCommunication") {
            @Override
            protected void threadBody() throws Throwable {
                mainloop();
            }
        };
        comm.setPriority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2);
        comm.start();
    }

    private static boolean dataUpdated = false;
    private static final Object updateLock = new Object();

    private static void mainloop() {
        ByteBuffer dataMutex = HALUtil.initializeMutexNormal();
        ByteBuffer dataSemaphore = HALUtil.initializeMultiWait();
        FRCNetworkCommunicationsLibrary.setNewDataSem(dataSemaphore);
        while (true) {
            HALUtil.takeMultiWait(dataSemaphore, dataMutex, 0);

            for (int stick = 0; stick < JOYSTICK_NUM; stick++) {
                axes[stick] = FRCNetworkCommunicationsLibrary.HALGetJoystickAxes((byte) stick);
                povs[stick] = FRCNetworkCommunicationsLibrary.HALGetJoystickPOVs((byte) stick);
                ByteBuffer countBuffer = ByteBuffer.allocateDirect(1);
                buttons[stick] = FRCNetworkCommunicationsLibrary.HALGetJoystickButtons((byte) stick, countBuffer);
                buttonCounts[stick] = countBuffer.get();
            }

            synchronized (updateLock) {
                dataUpdated = true;
                updateLock.notifyAll();
            }
        }
    }

    public static boolean isNewControlData() {
        if (dataUpdated) {
            dataUpdated = false;
            return true;
        } else {
            return false;
        }
    }

    public static void waitForData() throws InterruptedException {
        synchronized (updateLock) {
            updateLock.wait();
        }
    }

    public static float getStickAxis(int joy, int axis) {
        if (joy < 0 || joy >= JOYSTICK_NUM) {
            throw new RuntimeException("Invalid joystick port: " + joy);
        }
        if (axis < 0 || axis >= AXIS_NUM) {
            throw new RuntimeException("Invalid joystick axis: " + axis);
        }
        short[] jaxes = axes[joy];
        if (jaxes == null || axis >= jaxes.length) {
            return 0;
        }
        short value = jaxes[axis];
        if (value < 0) {
            return value / 128f;
        } else {
            return value / 127f;
        }
    }

    public static boolean getStickButton(int joy, int button) {
        if (joy < 0 || joy >= JOYSTICK_NUM) {
            throw new RuntimeException("Invalid joystick port: " + joy);
        }
        if (button < 0) {
            throw new RuntimeException("Invalid joystick button ID: " + button);
        }
        if (button >= buttonCounts[joy]) {
            return false;
        } else {
            return (buttons[joy] & (1 << button)) != 0;
        }
    }

    public static int getStickPOV(int joy, int pov) {
        if (joy < 0 || joy >= JOYSTICK_NUM) {
            throw new RuntimeException("Invalid joystick port: " + joy);
        }
        if (pov < 0 || pov >= POV_NUM) {
            throw new RuntimeException("Invalid joystick POV: " + pov);
        }
        short[] jpovs = povs[joy];
        if (jpovs == null || pov >= jpovs.length) {
            return 0;
        }
        return jpovs[pov];
    }

    public static void verifyPortNumber(int joy) {
        if (joy < 0 || joy >= JOYSTICK_NUM) {
            throw new RuntimeException("Invalid joystick port: " + joy);
        }
    }
}
