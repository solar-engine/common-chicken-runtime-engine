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
 */
package ccre.frc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.WeakHashMap;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.ctrl.AbstractJoystick;
import ccre.ctrl.IJoystick;

/**
 * Uses JInput to allow the Emulator to work with physical joysticks.
 *
 * @author skeggsc
 */
public class JoystickHandler {
    private static Logger logger = LoggerFactory.getLogger(JoystickHandler.class);

    static {
        File f;
        try {
            f = File.createTempFile("joystick-binaries", "");
            if (!f.delete()) {
                LoggerFactory.getLogger(JoystickHandler.class).warn("Could not delete JInput temporary file.");
            } else if (!f.mkdir()) {
                LoggerFactory.getLogger(JoystickHandler.class).warn("Could not create JInput temporary directory.");
            } else {
                f.deleteOnExit();
                LoggerFactory.getLogger(JoystickHandler.class).info("Putting binaries in: " + f);
                Properties props = new Properties();
                try (InputStream resource = JoystickHandler.class.getResourceAsStream("/natives.properties")) {
                    props.load(resource);
                }
                Object str = props.get("natives");
                if (!(str instanceof String)) {
                    throw new IOException("Bad type for natives field: " + str);
                }
                String[] natives = ((String) str).split(";");
                for (String s : natives) {
                    File out = new File(f, s);
                    InputStream inp = JoystickHandler.class.getResourceAsStream("/" + s);
                    if (inp == null) {
                        throw new IOException("Could not find resource: /" + s);
                    }
                    try {
                        OutputStream outp = new FileOutputStream(out);
                        try {
                            byte[] data = new byte[4096];
                            while (true) {
                                int len = inp.read(data);
                                if (len == -1) {
                                    break;
                                }
                                outp.write(data, 0, len);
                            }
                        } finally {
                            outp.close();
                        }
                    } finally {
                        inp.close();
                    }
                }
                System.setProperty("net.java.games.input.librarypath", f.getAbsolutePath());
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(JoystickHandler.class).warn("Could not unpack binaries for JInput", e);
        }
        ControllerEnvironment.getDefaultEnvironment().getControllers();
    }

    /**
     * A holder for physical Joysticks that translates the active Joystick into
     * a single interface.
     *
     * @author skeggsc
     */
    public static class ExternalJoystickHolder {

        private JoystickWrapper ctrl;

        /**
         * Check if a Joystick is currently associated.
         *
         * @return if any Joystick is associated.
         */
        public boolean hasJoystick() {
            return ctrl != null;
        }

        /**
         * Associate a new Joystick.
         *
         * @param wrapper the Joystick to associate.
         */
        public void setJoystick(JoystickWrapper wrapper) {
            this.ctrl = wrapper;
        }

        /**
         * Create an interface to allow access to the currently associated
         * Joystick.
         *
         * This method does NOT need to be called again when the associated
         * Joystick changes.
         *
         * @param check when to update the Joystick.
         * @return the Joystick interface.
         */
        public IJoystick getJoystick(EventInput check) {
            check.send(new EventOutput() {
                @Override
                public void event() {
                    if (ctrl != null) {
                        ctrl.ctrl.poll();
                    }
                }
            });
            return new AbstractJoystick(check, 12, 32) {

                @Override
                protected boolean getButton(int btn) {
                    if (ctrl == null) {
                        return false;
                    }
                    ArrayList<Component> buttons = ctrl.buttons;
                    if (btn < 1 || btn > buttons.size()) {
                        return false;
                    }
                    return buttons.get(btn - 1).getPollData() > 0.5f;
                }

                @Override
                protected float getAxis(int axis) {
                    if (ctrl == null) {
                        return 0.0f;
                    }
                    ArrayList<Component> axes = ctrl.axes;
                    if (ctrl.isXBox()) {
                        // Split axis 3 into axes 3 and 4.
                        if (axis >= 5 && axis <= 6) {
                            return axes.get(axis - 2).getPollData();
                        } else if (axis == 3) {
                            float raw = axes.get(2).getPollData();
                            return raw > 0 ? raw : 0;
                        } else if (axis == 4) {
                            float raw = axes.get(2).getPollData();
                            return raw < 0 ? -raw : 0;
                        }
                    }
                    if (axis < 1 || axis > axes.size()) {
                        return 0.0f;
                    }
                    return axes.get(axis - 1).getPollData();
                }

                @Override
                protected boolean getPOV(int direction) {
                    if (ctrl == null || ctrl.pov == null) {
                        return false;
                    }
                    int angle = (int) ((ctrl.pov.getPollData() * 360 + 270) % 360);
                    return direction == angle;
                }
            };
        }

    }

    /**
     * A wrapped physical Joystick that has organized buttons and axes.
     *
     * @author skeggsc
     */
    public static class JoystickWrapper {

        private final Controller ctrl;

        private final ArrayList<Component> buttons = new ArrayList<Component>();
        private final ArrayList<Component> axes = new ArrayList<Component>();
        private Component pov;

        private JoystickWrapper(Controller ctrl) {
            this.ctrl = ctrl;
        }

        private boolean isXBox() {
            return ctrl.getName().contains("XBOX 360 For Windows") && axes.size() == 5;
        }

        @Override
        public String toString() {
            return ctrl + " on " + ctrl.getPortType() + ":" + ctrl.getPortNumber();
        }

        private void start() {
            logger.info("Started: " + ctrl + ": " + ctrl.getType());
            axes.clear();
            buttons.clear();
            axes.add(null);
            axes.add(null);
            axes.add(null);
            axes.add(null);
            axes.add(null);
            for (Component comp : ctrl.getComponents()) {
                logger.info("Component: " + comp);
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    buttons.add(comp);
                } else if (comp.getIdentifier() instanceof Component.Identifier.Axis) {
                    if (comp.getIdentifier() == Component.Identifier.Axis.X) {
                        axes.set(0, comp);
                    } else if (comp.getIdentifier() == Component.Identifier.Axis.Y) {
                        axes.set(1, comp);
                    } else if (comp.getIdentifier() == Component.Identifier.Axis.Z) {
                        axes.set(2, comp);
                    } else if (comp.getIdentifier() == Component.Identifier.Axis.RX) {
                        axes.set(3, comp);
                    } else if (comp.getIdentifier() == Component.Identifier.Axis.RY) {
                        axes.set(4, comp);
                    } else if (comp.getIdentifier() == Component.Identifier.Axis.POV) {
                        pov = comp;
                    } else {
                        axes.add(comp);
                    }
                }
            }
            while (axes.contains(null)) {
                axes.remove(null);
            }
            logger.info("B/A/P: " + buttons + "/" + axes + "/" + pov);
            if (isXBox()) {
                logger.info("This is a 5-axis XBOX controller, which means that it's not going to show up the same as on the real robot.");
                logger.info("To resolve this, the emulator will remap the trigger axis into the two separate axes - but it won't work exactly the same.");
                logger.info("Notably, unlike the real robot, we don't know the difference between pressing both axes and pressing neither axes.");
                logger.info("So, it will think that neither are pressed in this scenario.");
            }
        }
    }

    private final WeakHashMap<Controller, JoystickWrapper> wrappings = new WeakHashMap<Controller, JoystickWrapper>();

    private boolean isIgnored(Controller ctrl) {
        Controller.Type t = ctrl.getType();
        return t == Controller.Type.MOUSE || t == Controller.Type.KEYBOARD || t == Controller.Type.UNKNOWN;
    }

    /**
     * Get a Joystick that currently has a button held down.
     *
     * @return the detected Joystick, or null if none are found.
     */
    public JoystickWrapper getActivelyPressedJoystick() {
        for (Controller ctrl : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            if (isIgnored(ctrl)) {
                continue;
            }
            ctrl.poll();
            for (Component comp : ctrl.getComponents()) {
                if (comp.getIdentifier() instanceof Component.Identifier.Button) {
                    if (comp.getPollData() > 0.5f) {
                        return getOrWrap(ctrl);
                    }
                }
            }
        }
        return null;
    }

    private JoystickWrapper getOrWrap(Controller ctrl) {
        JoystickWrapper w = wrappings.get(ctrl);
        if (w == null) {
            w = new JoystickWrapper(ctrl);
            wrappings.put(ctrl, w);
            w.start();
        }
        return w;
    }
}
