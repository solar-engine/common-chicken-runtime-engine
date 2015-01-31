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
import java.nio.IntBuffer;

import edu.wpi.first.wpilibj.hal.DIOJNI;
import edu.wpi.first.wpilibj.hal.JNIWrapper;
import edu.wpi.first.wpilibj.hal.RelayJNI;

class DirectRelay {
    public static final int RELAY_NUM = 4;
    private static final ByteBuffer[] relays = new ByteBuffer[RELAY_NUM];

    public static ByteBuffer init(int channel) {
        if (channel < 0 || channel >= RELAY_NUM) {
            throw new RuntimeException("Invalid relay port number: " + channel);
        }
        if (relays[channel] == null) {
            IntBuffer status = Common.allocateInt();
            ByteBuffer port = DIOJNI.initializeDigitalPort(JNIWrapper.getPort((byte) channel), status);
            Common.check(status);
            relays[channel] = port;
        }
        return relays[channel];
    }

    public static void setForward(ByteBuffer port, boolean active) {
        IntBuffer status = Common.allocateInt();
        RelayJNI.setRelayForward(port, (byte) (active ? 1 : 0), status);
        Common.check(status);
    }

    public static void setReverse(ByteBuffer port, boolean active) {
        IntBuffer status = Common.allocateInt();
        RelayJNI.setRelayReverse(port, (byte) (active ? 1 : 0), status);
        Common.check(status);
    }
}
