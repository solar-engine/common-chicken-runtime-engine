/*
 * Copyright 2013 Colby Skeggs
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
package ccre.obsidian;

import ccre.event.EventConsumer;

/**
 *
 * @author MillerV
 */
public class EmulatorWorld implements EventConsumer {

    private double x;
    private double y;
    private double angle;
    private double linearVelocity;
    private double rotationalVelocity;

    @Override
    public void eventFired() {
        angle += rotationalVelocity;
        x += Math.sin(Math.toRadians(angle)) * linearVelocity;
        y += Math.cos(Math.toRadians(angle)) * linearVelocity;
    }

    public GPSLocation getLocation() {
        return new GPSLocation(x, y, angle);
    }

    public void updateVelocity(double leftMotor, double rightMotor) {
        linearVelocity = (rightMotor + leftMotor) / 2;
        rotationalVelocity = (rightMotor - leftMotor) / 2;
    }

    public class GPSLocation {

        public final double x;
        public final double y;
        public final double angle;

        public GPSLocation(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }
}
