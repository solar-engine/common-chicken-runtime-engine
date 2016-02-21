/*
 * Copyright 2016 Cel Skeggs
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
 */
package ccre.bus;

public interface RS232Bus {
    public static enum Parity {
        PARITY_NONE, PARITY_ODD, PARITY_EVEN, PARITY_MARK, PARITY_SPACE
    }

    public static enum StopBits {
        STOP_ONE, STOP_ONE_POINT_FIVE, STOP_TWO
    }

    public RS232IO open(int baudRate, Parity parity, StopBits stopBits, float timeout, int dataBits);

    public default RS232IO open(int baudRate, Parity parity, StopBits stopBits, float timeout) {
        return open(baudRate, parity, stopBits, timeout, 8);
    }

    public default RS232IO open(int baudRate, Parity parity, StopBits stopBits) {
        return open(baudRate, parity, stopBits, 5.0f);
    }

    public default RS232IO open(int baudRate, Parity parity) {
        return open(baudRate, parity, StopBits.STOP_ONE);
    }

    public default RS232IO open(int baudRate) {
        return open(baudRate, Parity.PARITY_NONE);
    }
}
