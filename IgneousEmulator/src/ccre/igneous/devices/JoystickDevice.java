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
package ccre.igneous.devices;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.CombinationJoystickWithPOV;
import ccre.ctrl.IJoystickWithPOV;
import ccre.igneous.Device;
import ccre.igneous.DeviceGroup;
import ccre.igneous.DeviceListPanel;
import ccre.igneous.JoystickHandler;
import ccre.igneous.JoystickHandler.ExternalJoystickHolder;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;
import ccre.log.Logger;

/**
 * A device representing a Joystick. This will have buttons and axes added
 * dynamically as needed.
 *
 * @author skeggsc
 */
public class JoystickDevice extends DeviceGroup {

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
                            Logger.info("Attaching joystick: " + joy);
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
    private final FloatControlDevice[] povs = new FloatControlDevice[10]; // 10 is arbitrary.
    private final BooleanControlDevice[] povPresses = new BooleanControlDevice[povs.length];

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

    private FloatControlDevice getAxis(int id) {
        if (id < 1 || id > axes.length) {
            throw new IllegalArgumentException("Invalid axis number: " + id);
        }
        if (axes[id - 1] == null) {
            axes[id - 1] = new FloatControlDevice("Axis " + id);
            add(axes[id - 1]);
            addToMaster();
        }
        return axes[id - 1];
    }

    /**
     * Get the IJoystickWithPOV to access this Joystick.
     *
     * @param check when to update the Joystick's sources.
     * @return the Joystick.
     */
    public IJoystickWithPOV getJoystick(EventInput check) {
        return new CombinationJoystickWithPOV(joystickHolder.getJoystick(check), new IJoystickWithPOV() {
            public EventInput getButtonSource(int id) {
                if (id < 1 || id > buttons.length) {
                    throw new IllegalArgumentException("Invalid button number: " + id);
                }
                if (buttons[id - 1] == null) {
                    buttons[id - 1] = new BooleanControlDevice("Button " + id);
                    add(buttons[id - 1]);
                    addToMaster();
                }
                return buttons[id - 1].whenPressed();
            }

            public FloatInput getAxisSource(int axis) {
                return getAxis(axis);
            }

            public FloatInputPoll getAxisChannel(int axis) {
                return getAxis(axis);
            }

            public BooleanInputPoll getButtonChannel(int id) {
                if (id < 1 || id > buttons.length) {
                    throw new IllegalArgumentException("Invalid button number: " + id);
                }
                if (buttons[id - 1] == null) {
                    buttons[id - 1] = new BooleanControlDevice("Button " + id);
                    add(buttons[id - 1]);
                    addToMaster();
                }
                return buttons[id - 1];
            }

            public FloatInputPoll getXChannel() {
                return getAxisChannel(1);
            }

            public FloatInputPoll getYChannel() {
                return getAxisChannel(2);
            }

            public FloatInput getXAxisSource() {
                return getAxisSource(1);
            }

            public FloatInput getYAxisSource() {
                return getAxisSource(2);
            }

            public BooleanInputPoll isPOVPressed(int id) {
                return isPOVPressedSource(id);
            }

            public FloatInputPoll getPOVAngle(int id) {
                return getPOVAngleSource(id);
            }

            public BooleanInput isPOVPressedSource(int id) {
                if (!isRoboRIO) {
                    throw new RuntimeException("POVs can only be accessed from a roboRIO!");
                }
                if (id < 1 || id > povPresses.length) {
                    throw new IllegalArgumentException("Invalid POV number: " + id);
                }
                if (povPresses[id - 1] == null) {
                    povPresses[id - 1] = new BooleanControlDevice("POV " + id);
                    add(povPresses[id - 1]);
                    addToMaster();
                }
                return povPresses[id - 1];
            }

            public FloatInput getPOVAngleSource(int id) {
                if (!isRoboRIO) {
                    throw new RuntimeException("POVs can only be accessed from a roboRIO!");
                }
                if (id < 1 || id > povs.length) {
                    throw new IllegalArgumentException("Invalid POV number: " + id);
                }
                if (povs[id - 1] == null) {
                    povs[id - 1] = new FloatControlDevice("POV " + id, 0, 360, 0, 0);
                    add(povs[id - 1]);
                    addToMaster();
                }
                return povs[id - 1];
            }
        });
    }
}
