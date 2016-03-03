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
 * A standard SPI port, unconfigured.
 *
 * @author skeggsc
 */
public interface SPIBus {
    /**
     * Configures the SPI port and provides an open connection.
     *
     * @param hertz the baud rate of the connection.
     * @param isMSB if the most significant bit (MSB) should be first.
     * @param dataOnFalling if bits are sent and received on the falling edge.
     * @param clockActiveLow if the clock is active when low.
     * @param chipSelectActiveLow if the chip select pin is active when low.
     * @return the configured SPI connection.
     */
    public SPIIO configure(int hertz, boolean isMSB, boolean dataOnFalling, boolean clockActiveLow, boolean chipSelectActiveLow);
}
