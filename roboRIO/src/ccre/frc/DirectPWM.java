/*
 * Copyright 2015-2016 Cel Skeggs
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
package ccre.frc;

import edu.wpi.first.wpilibj.hal.DIOJNI;
import edu.wpi.first.wpilibj.hal.JNIWrapper;
import edu.wpi.first.wpilibj.hal.PWMJNI;

class DirectPWM {
    public static final int PWM_NUM = 20;

    public static final int TYPE_TALON = 0;
    public static final int TYPE_JAGUAR = 1;
    public static final int TYPE_VICTOR = 2;
    public static final int TYPE_SERVO = 3;
    public static final int TYPE_VICTORSP = 4;
    public static final int TYPE_SPARK = 5;
    public static final int TYPE_SD540 = 6;
    public static final int TYPE_TALONSRX = 7;
    public static final int TYPE_NUM = 8;
    private static final long[] pwms = new long[PWM_NUM];
    private static final int[] types = new int[PWM_NUM];
    private static final int[] tmax = new int[TYPE_NUM],
            tdbMax = new int[TYPE_NUM], tctr = new int[TYPE_NUM],
            tdbMin = new int[TYPE_NUM], tmin = new int[TYPE_NUM];
    private static boolean isConfigInit = false;
    private static final int kSystemClockTicksPerMicrosecond = 40;
    private static final double kDefaultPwmCenter = 1.5;
    private static final int kDefaultPwmStepsDown = 1000;
    private static final double[] cmax = new double[] { 2.037, 2.31, 2.027, 2.6, 2.004, 2.003, 2.05, 2.004 },
            cdbMax = new double[] { 1.539, 1.55, 1.525, 0, 1.52, 1.55, 1.55, 1.52 },
            cctr = new double[] { 1.513, 1.507, 1.507, 0, 1.50, 1.50, 1.50, 1.50 },
            cdbMin = new double[] { 1.487, 1.454, 1.49, 0, 1.48, 1.46, 1.44, 1.48 },
            cmin = new double[] { 0.989, 0.697, 1.026, 0.6, 0.997, 0.999, 0.94, 0.997 };
    private static final int[] scaling = new int[] { 1, 1, 2, 4, 1, 1, 1, 1 };

    private static synchronized void initConfig() {
        double loopTime = DIOJNI.getLoopTiming() / (kSystemClockTicksPerMicrosecond * 1e3);

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

        if (pwms[channel] == 0) {
            long port = DIOJNI.initializeDigitalPort(JNIWrapper.getPort((byte) channel));

            if (!PWMJNI.allocatePWMChannel(port)) {
                throw new RuntimeException("PWM channel " + channel + " is already allocated");
            }

            PWMJNI.setPWM(port, (short) 0);

            if (!isConfigInit) {
                initConfig();
            }

            configureScaling(port, scaling[type]);

            if (type != TYPE_SERVO) {
                PWMJNI.latchPWMZero(port);
            }

            pwms[channel] = port;
            types[channel] = type;
        } else if (types[channel] != type) {
            throw new RuntimeException("Cannot allocate PWM as multiple types: " + channel);
        }
    }

    private static void configureScaling(long port, int num) {
        switch (num) {
        case 4: // more squelching
            PWMJNI.setPWMPeriodScale(port, 3);
            break;
        case 2: // less squelching
            PWMJNI.setPWMPeriodScale(port, 1);
            break;
        case 1: // no squelching
            PWMJNI.setPWMPeriodScale(port, 0);
            break;
        default:
            throw new RuntimeException("Invalid scaling to configureScaling.");
        }
    }

    public static synchronized void freePWM(int channel) {
        if (channel < 0 || channel >= PWM_NUM) {
            throw new RuntimeException("PWM port out of range: " + channel);
        }
        long port = pwms[channel];
        if (port == 0) {
            return; // already freed
        }
        pwms[channel] = 0;
        PWMJNI.setPWM(port, (short) 0);
        PWMJNI.freePWMChannel(port);
        DIOJNI.freeDIO(port);
    }

    public static void set(int channel, float value) {
        if (channel < 0 || channel >= PWM_NUM) {
            throw new RuntimeException("PWM port out of range: " + channel);
        }
        long port = pwms[channel];
        if (port == 0) {
            throw new RuntimeException("PWM port unallocated: " + channel);
        }
        int rawValue;
        int type = types[channel];
        if (type == TYPE_SERVO) {
            rawValue = (int) ((value * (double) (tmax[type] - tmin[type])) + tmin[type]);
        } else {
            if (value == 0.0) {
                rawValue = tctr[type];
            } else if (value == Float.POSITIVE_INFINITY) {
                rawValue = (int) (tmax[type] + 0.5);
            } else if (value == Float.NEGATIVE_INFINITY) {
                rawValue = (int) (tmin[type] + 0.5);
            } else if (value > 0.0) {
                rawValue = (int) (value * ((double) (tmax[type] - tdbMax[type])) + tdbMax[type] + 0.5);
            } else {
                // NaN will cause rawValue to be zero, which means disabled.
                rawValue = (int) (value * ((double) (tdbMin[type] - tmin[type])) + tdbMin[type] + 0.5);
            }
        }

        PWMJNI.setPWM(port, (short) rawValue);
    }
}
