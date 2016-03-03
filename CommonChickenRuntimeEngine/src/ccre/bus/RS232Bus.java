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

/**
 * A standard RS232 serial port, unconfigured.
 *
 * @author skeggsc
 */
public interface RS232Bus {
    /**
     * The options for Parity bits on the port.
     */
    public static enum Parity {
        // TODO: figure out exactly what these mean and document them.
        PARITY_NONE, PARITY_ODD, PARITY_EVEN, PARITY_MARK, PARITY_SPACE
    }

    /**
     * The options for Stop bits on the port.
     */
    public static enum StopBits {
        // TODO: figure out exactly what these mean and document them.
        STOP_ONE, STOP_ONE_POINT_FIVE, STOP_TWO
    }

    /**
     * Configures the RS232 port.
     *
     * @param baudRate the baud rate.
     * @param parity the parity bit setting.
     * @param stopBits the stop bit setting.
     * @param timeout the timeout, for when a read needs more bytes than are
     * available.
     * @param dataBits the number of data bits per word. not yet tested with
     * sizes besides 8 bits.
     * @return the configured RS232 port.
     */
    public RS232IO open(int baudRate, Parity parity, StopBits stopBits, float timeout, int dataBits);

    /**
     * Configures the RS232 port, with 8 bits per byte.
     *
     * @param baudRate the baud rate.
     * @param parity the parity bit setting.
     * @param stopBits the stop bit setting.
     * @param timeout the timeout, for when a read needs more bytes than are
     * available.
     * @return the configured RS232 port.
     */
    public default RS232IO open(int baudRate, Parity parity, StopBits stopBits, float timeout) {
        return open(baudRate, parity, stopBits, timeout, 8);
    }

    /**
     * Configures the RS232 port, with 8 bits per byte and a timeout of 5
     * seconds.
     *
     * @param baudRate the baud rate.
     * @param parity the parity bit setting.
     * @param stopBits the stop bit setting.
     * @return the configured RS232 port.
     */
    public default RS232IO open(int baudRate, Parity parity, StopBits stopBits) {
        return open(baudRate, parity, stopBits, 5.0f);
    }

    /**
     * Configures the RS232 port, with 8 bits per byte, a timeout of 5
     * seconds, and one stop bit.
     *
     * @param baudRate the baud rate.
     * @param parity the parity bit setting.
     * @return the configured RS232 port.
     */
    public default RS232IO open(int baudRate, Parity parity) {
        return open(baudRate, parity, StopBits.STOP_ONE);
    }

    /**
     * Configures the RS232 port, with 8 bits per byte, a timeout of 5
     * seconds, one stop bit, and no parity bits.
     *
     * @param baudRate the baud rate.
     * @return the configured RS232 port.
     */
    public default RS232IO open(int baudRate) {
        return open(baudRate, Parity.PARITY_NONE);
    }
}
