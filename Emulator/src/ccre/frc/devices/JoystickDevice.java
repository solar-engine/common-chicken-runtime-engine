/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.frc.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.ctrl.CombinationJoystickWithPOV;
import ccre.ctrl.IJoystick;
import ccre.frc.Device;
import ccre.frc.DeviceGroup;
import ccre.frc.DeviceListPanel;
import ccre.frc.JoystickHandler;
import ccre.frc.JoystickHandler.ExternalJoystickHolder;
import ccre.frc.components.SpacingComponent;
import ccre.frc.components.TextComponent;

/**
 * A device representing a Joystick. This will have buttons and axes added
 * dynamically as needed.
 *
 * @author skeggsc
 */
public class JoystickDevice extends DeviceGroup {
    private static Logger logger = LoggerFactory.getLogger(JoystickDevice.class);

    class ExternalJoystickAttachDevice extends Device {
        private final TextComponent status;

        ExternalJoystickAttachDevice(final JoystickHandler handler) {
            add(new SpacingComponent(40));
            add(new TextComponent("External Joystick:"));
            add(status = new TextComponent("[UNATTACHED]", new String[] { "[UNATTACHED]", "[HOLD BUTTON FIRST]", "[ATTACHED]" }) {
                @Override
                protected void onPress(int x, int y) {
                    if (joystickHolder.hasJoystick()) {
                        joystickHolder.setJoystick(null);
                        status.setLabel("[UNATTACHED]");
                    } else {
                        JoystickHandler.JoystickWrapper joy = handler.getActivelyPressedJoystick();
                        if (joy == null) {
                            status.setLabel("[UNATTACHED] Hold a Joystick button before clicking.");
                        } else {
                        	logger.info("Attaching joystick: " + joy);
                            joystickHolder.setJoystick(joy);
                            status.setLabel("[ATTACHED]: " + joy);
                        }
                    }
                }
            });
        }
    }

    private final FloatControlDevice[] axes = new FloatControlDevice[6];
    private final BooleanControlDevice[] buttons = new BooleanControlDevice[14];
    private final BooleanControlDevice[] povAngles = new BooleanControlDevice[360];

    private boolean wasAddedToMaster = false;
    private final DeviceListPanel master;
    private boolean isRoboRIO;
    private final ExternalJoystickHolder joystickHolder;

    /**
     * Create a new JoystickDevice with a name and a panel to contain this
     * Joystick.
     *
     * Make sure to call addToMaster instead of calling add directly.
     *
     * @param name the name of this device.
     * @param isRoboRIO if this is a Joystick on a roboRIO.
     * @param master the panel that will contain this device.
     * @param handler the JoystickHandler to use for connecting external
     * Joysticks.
     * @see #addToMaster()
     */
    public JoystickDevice(String name, boolean isRoboRIO, DeviceListPanel master, JoystickHandler handler) {
        this.isRoboRIO = isRoboRIO;
        add(new HeadingDevice(name));
        add(new ExternalJoystickAttachDevice(handler));
        this.master = master;
        joystickHolder = new ExternalJoystickHolder();
    }

    /**
     * Create a new JoystickDevice with a Joystick port number and a panel to
     * contain this Joystick.
     *
     * Make sure to call addToMaster instead of calling add directly.
     *
     * @param id the port number of this device.
     * @param isRoboRIO if this is a Joystick on a roboRIO.
     * @param master the panel that will contain this device.
     * @param handler the JoystickHandler to use for connecting external
     * Joysticks.
     * @see #addToMaster()
     */
    public JoystickDevice(int id, boolean isRoboRIO, DeviceListPanel master, JoystickHandler handler) {
        this("Joystick " + id, isRoboRIO, master, handler);
    }

    /**
     * Add this Joystick to the device panel, if it hasn't been already added.
     *
     * @return this device, for method chaining.
     */
    public synchronized JoystickDevice addToMaster() {
        if (!wasAddedToMaster) {
            wasAddedToMaster = true;
            master.add(this);
        }
        return this;
    }

    /**
     * Get the IJoystickWithPOV to access this Joystick.
     *
     * @param check when to update the Joystick's sources.
     * @return the Joystick.
     */
    public IJoystick getJoystick(EventInput check) {
        return new CombinationJoystickWithPOV(joystickHolder.getJoystick(check), new IJoystick() {
            public BooleanInput button(int id) {
                if (id < 1 || id > buttons.length) {
                    throw new IllegalArgumentException("Invalid button number: " + id);
                }
                if (buttons[id - 1] == null) {
                    buttons[id - 1] = new BooleanControlDevice("Button " + id);
                    add(buttons[id - 1]);
                    addToMaster();
                }
                return buttons[id - 1].asInput();
            }

            public FloatInput axis(int id) {
                if (id < 1 || id > axes.length) {
                    throw new IllegalArgumentException("Invalid axis number: " + id);
                }
                if (axes[id - 1] == null) {
                    axes[id - 1] = new FloatControlDevice("Axis " + id);
                    add(axes[id - 1]);
                    addToMaster();
                }
                return axes[id - 1].asInput();
            }

            public BooleanInput isPOV(int direction) {
                if (!isRoboRIO) {
                    throw new RuntimeException("POVs can only be accessed from a roboRIO!");
                }
                if (direction < 0 || direction >= 360) {
                    throw new IllegalArgumentException("POV directions must be in range 0 ... 359!");
                }
                if (povAngles[direction] == null) {
                    povAngles[direction] = new BooleanControlDevice("POV dir " + direction);
                    add(povAngles[direction]);
                    addToMaster();
                }
                return povAngles[direction].asInput();
            }
        });
    }
}
