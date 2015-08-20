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
package ccre.igneous;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.DIOJNI;
import edu.wpi.first.wpilibj.hal.JNIWrapper;
import edu.wpi.first.wpilibj.hal.PWMJNI;

class DirectPWM {
    public static final int PWM_NUM = 20;

    public static final int TYPE_TALON = 0;
    public static final int TYPE_JAGUAR = 1;
    public static final int TYPE_VICTOR = 2;
    public static final int TYPE_SERVO = 3;
    public static final int TYPE_NUM = 4;
    private static final ByteBuffer[] pwms = new ByteBuffer[PWM_NUM];
    private static final int[] types = new int[PWM_NUM];
    private static final int[] tmax = new int[TYPE_NUM],
            tdbMax = new int[TYPE_NUM], tctr = new int[TYPE_NUM],
            tdbMin = new int[TYPE_NUM], tmin = new int[TYPE_NUM];
    private static boolean isConfigInit = false;
    private static final int kSystemClockTicksPerMicrosecond = 40;
    private static final double kDefaultPwmCenter = 1.5;
    private static final int kDefaultPwmStepsDown = 1000;
    private static final double[] cmax = new double[] { 2.037, 2.31, 2.027, 2.6 },
            cdbMax = new double[] { 1.539, 1.55, 1.525, 0 },
            cctr = new double[] { 1.513, 1.507, 1.507, 0 },
            cdbMin = new double[] { 1.487, 1.454, 1.49, 0 },
            cmin = new double[] { 0.989, 0.697, 1.026, 0.6 };

    private static synchronized void initConfig(IntBuffer status) {
        double loopTime = DIOJNI.getLoopTiming(status) / (kSystemClockTicksPerMicrosecond * 1e3);
        Common.check(status);

        for (int i = 0; i < TYPE_NUM; i++) {
            tmax[i] = (int) ((cmax[i] - kDefaultPwmCenter) / loopTime + kDefaultPwmStepsDown - 1);
            tdbMax[i] = (int) ((cdbMax[i] - kDefaultPwmCenter) / loopTime + kDefaultPwmStepsDown - 1);
            tctr[i] = (int) ((cctr[i] - kDefaultPwmCenter) / loopTime + kDefaultPwmStepsDown - 1);
            tdbMin[i] = (int) ((cdbMin[i] - kDefaultPwmCenter) / loopTime + kDefaultPwmStepsDown - 1);
            tmin[i] = (int) ((cmin[i] - kDefaultPwmCenter) / loopTime + kDefaultPwmStepsDown - 1);
        }
        isConfigInit = true;
    }

    public static synchronized void init(int channel, int type) {
        if (channel < 0 || channel >= PWM_NUM) {
            throw new RuntimeException("PWM port out of range: " + channel);
        }
        if (type < 0 || type >= TYPE_NUM) {
            throw new RuntimeException("Invalid PWM type: " + type);
        }

        if (pwms[channel] == null) {
            IntBuffer status = Common.getCheckBuffer();

            ByteBuffer port = DIOJNI.initializeDigitalPort(JNIWrapper.getPort((byte) channel), status);
            Common.check(status);

            if (!PWMJNI.allocatePWMChannel(port, status)) {
                throw new RuntimeException("PWM channel " + channel + " is already allocated");
            }
            Common.check(status);

            PWMJNI.setPWM(port, (short) 0, status);
            Common.check(status);

            if (!isConfigInit) {
                initConfig(status);
            }

            configureScaling(port, type == TYPE_SERVO ? 4 : type == TYPE_VICTOR ? 2 : 1, status);

            if (type != TYPE_SERVO) {
                PWMJNI.latchPWMZero(port, status);

                Common.check(status);
            }

            pwms[channel] = port;
            types[channel] = type;
        } else if (types[channel] != type) {
            throw new RuntimeException("Cannot allocate PWM as multiple types: " + channel);
        }
    }

    private static void configureScaling(ByteBuffer port, int num, IntBuffer status) {
        switch (num) {
        case 4: // more squelching
            PWMJNI.setPWMPeriodScale(port, 3, status);
            break;
        case 2: // less squelching
            PWMJNI.setPWMPeriodScale(port, 1, status);
            break;
        case 1: // no squelching
            PWMJNI.setPWMPeriodScale(port, 0, status);
            break;
        default:
            throw new RuntimeException("Invalid scaling to configureScaling.");
        }

        Common.check(status);
    }

    public static synchronized void freePWM(int channel) {
        if (channel < 0 || channel >= PWM_NUM) {
            throw new RuntimeException("PWM port out of range: " + channel);
        }
        ByteBuffer port = pwms[channel];
        if (port == null) {
            return; // already freed
        }
        pwms[channel] = null;

        IntBuffer status = Common.getCheckBuffer();

        PWMJNI.setPWM(port, (short) 0, status);
        Common.check(status);

        PWMJNI.freePWMChannel(port, status);
        Common.check(status);

        DIOJNI.freeDIO(port, status);
        Common.check(status);
    }

    public static void set(int channel, float value) {
        if (channel < 0 || channel >= PWM_NUM) {
            throw new RuntimeException("PWM port out of range: " + channel);
        }
        ByteBuffer port = pwms[channel];
        if (port == null) {
            throw new RuntimeException("PWM port unallocated: " + channel);
        }
        int rawValue;
        int type = types[channel];
        if (type == TYPE_SERVO) {
            rawValue = (int) ((value * (double) (tmax[type] - tmin[type])) + tmin[type]);
        } else {
            if (value == 0.0) {
                rawValue = tctr[type];
            } else if (value > 0.0) {
                rawValue = (int) (value * ((double) (tmax[type] - tdbMax[type])) + tdbMax[type] + 0.5);
            } else {
                rawValue = (int) (value * ((double) (tdbMin[type] - tmin[type])) + tdbMin[type] + 0.5);
            }
        }
        IntBuffer status = Common.getCheckBuffer();
        PWMJNI.setPWM(port, (short) rawValue, status); // just FPGA errors
        Common.check(status);
    }
}
