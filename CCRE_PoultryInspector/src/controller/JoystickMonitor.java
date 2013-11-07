/*
 * Copyright 2013 Colby Skeggs and Vincent Miller
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
package controller;

import ccre.chan.BooleanInput;
import ccre.chan.BooleanInputProducer;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInput;
import ccre.ctrl.Mixing;
import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;
import ccre.cluck.CluckNode;
import ccre.log.LogLevel;
import ccre.log.Logger;

/**
 * The interface to a joystick connected to the driver station. Stores the x, y,
 * z, and slider axes and buttons 1-12 on a joystick and publishes them over the
 * network.
 *
 * @author MillerV
 */
public final class JoystickMonitor {

    private FloatInput[] axes;
    private BooleanInput[] buttons;
    private int stick;
    private boolean connected;

    /**
     * Creates a new joystick monitor. It will automatically attach to the first
     * joystick it finds connected to the computer. The inputs are not
     * automatically shared over the network.
     *
     * @param stick The stick number to load.
     */
    public JoystickMonitor(int stick) {
        if (refresh(stick)) {
            Logger.log(LogLevel.INFO, "Connected to joystick.");
        } else {
            Logger.log(LogLevel.WARNING, "Could not connect to joystick.");
        }
    }

    /**
     * @return Whether a joystick was found the last time this JoystickMonitor
     * was refreshed.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Refreshes the USB connection with the joystick.
     *
     * @param stick The stick number to load.
     * @return Whether a stick was found.
     */
    public boolean refresh(int stick) {
        this.stick = stick;
        int curStick = stick - 1;
        connected = false;
        for (RobotController controller : RobotController.getControllers()) {
            if (controller.getType().equals("Stick")) {
                if (curStick == 0) {
                    initializeInputs(controller);
                    connected = true;
                    break;
                } else {
                    curStick--;
                }
            }
        }
        return connected;
    }

    /**
     * Shares the FloatInputs from the joystick axes and BooleanInputs from the
     * buttons.
     *
     * @param node The CluckNode to use for publishing the inputs.
     */
    public void share(CluckNode node) {
        for (int i = 0; i < 11; i++) {
            if (i < 7) {
                if (isConnected() && axes[i] != null) {
                    node.publish("joystick" + stick + "-axis" + (i + 1), axes[i]);
                } else {
                    node.publish("joystick" + stick + "-axis" + (i + 1), Mixing.always(0.0f));
                }
                if (isConnected() && buttons[i] != null) {
                    node.publish("joystick" + stick + "-button" + (i + 1), buttons[i]);
                } else {
                    node.publish("joystick" + stick + "-button" + (i + 1), Mixing.alwaysFalse);
                }
            }
        }
    }

    private void initializeInputs(RobotController joystick) {
        axes = new FloatInput[7];
        buttons = new BooleanInput[12];

        for (int i = 1; i < 12; i++) {
            if (i < 8) {
                try {
                    axes[i - 1] = joystick.getAxis(i);
                } catch (InputTypeException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }

            try {
                buttons[i - 1] = joystick.getButton(i);
            } catch (InputTypeException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
