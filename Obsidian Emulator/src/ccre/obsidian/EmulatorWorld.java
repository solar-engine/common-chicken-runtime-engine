/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
