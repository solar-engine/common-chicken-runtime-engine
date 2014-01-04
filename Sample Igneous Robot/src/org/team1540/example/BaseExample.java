package org.team1540.example;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;

public class BaseExample extends IterativeRobot {
    
    Joystick leftJoystick;
    Joystick rightJoystick;
    Talon leftTalon;
    Talon rightTalon;
    double rampLeft;
    double rampRight;

    public void robotInit() {
        leftJoystick = new Joystick(1);
        rightJoystick = new Joystick(2);
        leftTalon = new Talon(1);
        rightTalon = new Talon(2);
    }
    
    public void teleopPeriodic() {
        double inputLeft = leftJoystick.getY();
        double inputRight = rightJoystick.getY();
        if (inputLeft < rampLeft - 0.1) {
            rampLeft = rampLeft - 0.1;
        } else if (inputLeft > rampLeft + 0.1) {
            rampLeft = rampLeft + 0.1;
        } else {
            rampLeft = inputLeft;
        }
        if (inputRight < rampRight - 0.1) {
            rampRight = rampRight - 0.1;
        } else if (inputRight > rampRight + 0.1) {
            rampRight = rampRight + 0.1;
        } else {
            rampRight = inputRight;
        }
        leftTalon.set(rampLeft);
        rightTalon.set(rampRight);
    }
}
