package ccre.igneous;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.WeakHashMap;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.AbstractJoystickWithPOV;
import ccre.ctrl.BooleanMixing;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.IJoystickWithPOV;
import ccre.igneous.JoystickHandler.JoystickWrapper;
import ccre.log.Logger;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

public class JoystickHandler {
    static {
        File f;
        try {
            f = File.createTempFile("joystick-binaries", "");
            if (!f.delete()) {
                Logger.warning("Could not delete JInput temporary file.");
            } else if (!f.mkdir()) {
                Logger.warning("Could not create JInput temporary directory.");
            } else {
                f.deleteOnExit();
                Logger.info("Putting binaries in: " + f);
                Properties props = new Properties();
                props.load(JoystickHandler.class.getResourceAsStream("/natives.properties"));
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
            Logger.warning("Could not unpack binaries for JInput", e);
        }
        ControllerEnvironment.getDefaultEnvironment().getControllers();
    }

    public static class ExternalJoystickHolder {

        private JoystickWrapper ctrl;

        public boolean hasJoystick() {
            return ctrl != null;
        }

        public void setJoystick(JoystickWrapper wrapper) {
            this.ctrl = wrapper;
        }

        public IJoystickWithPOV getJoystick(EventInput check) {
            check.send(new EventOutput() {
                public void event() {
                    if (ctrl != null) {
                        ctrl.ctrl.poll();
                    }
                }
            });
            return new AbstractJoystickWithPOV(check) {

                public BooleanInputPoll getButtonChannel(final int button) {
                    return new BooleanInputPoll() {
                        public boolean get() {
                            if (ctrl == null) {
                                return false;
                            }
                            ArrayList<Component> buttons = ctrl.buttons;
                            if (button < 1 || button > buttons.size()) {
                                return false;
                            }
                            return buttons.get(button - 1).getPollData() > 0.5f;
                        }
                    };
                }

                public FloatInputPoll getAxisChannel(final int axis) {
                    return new FloatInputPoll() {
                        public float get() {
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
                    };
                }

                public BooleanInputPoll isPOVPressed(final int id) {
                    return new BooleanInputPoll() {
                        public boolean get() {
                            if (id != 1 || ctrl == null || ctrl.pov == null) {
                                return false;
                            }
                            return ctrl.pov.getPollData() > 0.0f;
                        }
                    };
                }

                public FloatInputPoll getPOVAngle(final int id) {
                    return new FloatInputPoll() {
                        public float get() {
                            if (id != 1 || ctrl == null || ctrl.pov == null) {
                                return 0.0f;
                            }
                            return (ctrl.pov.getPollData() * 360 + 270) % 360;
                        }
                    };
                }
            };
        }

    }

    public static class JoystickWrapper {

        public final Controller ctrl;

        public final ArrayList<Component> buttons = new ArrayList<Component>();
        public final ArrayList<Component> axes = new ArrayList<Component>();
        Component pov;

        public JoystickWrapper(Controller ctrl) {
            this.ctrl = ctrl;
        }

        public boolean isXBox() {
            return "XBOX 360 For Windows (Controller)".equals(ctrl.getName()) && axes.size() == 5;
        }

        public String toString() {
            return ctrl + " on " + ctrl.getPortType() + ":" + ctrl.getPortNumber();
        }

        public void start() {
            Logger.info("Started: " + ctrl + ": " + ctrl.getType());
            axes.clear();
            buttons.clear();
            axes.add(null);
            axes.add(null);
            axes.add(null);
            axes.add(null);
            axes.add(null);
            for (Component comp : ctrl.getComponents()) {
                Logger.info("Component: " + comp);
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
            Logger.info("B/A/P: " + buttons + "/" + axes + "/" + pov);
            if (isXBox()) {
                Logger.info("This is a 5-axis XBOX controller, which means that it's not going to show up the same as on the real robot.");
                Logger.info("To resolve this, the emulator will remap the trigger axis into the two separate axes - but it won't work exactly the same.");
                Logger.info("Notably, unlike the real robot, we don't know the difference between pressing both axes and pressing neither axes.");
                Logger.info("So, it will think that neither are pressed in this scenario.");
            }
        }
    }

    private final WeakHashMap<Controller, JoystickWrapper> wrappings = new WeakHashMap<Controller, JoystickWrapper>();

    private boolean isIgnored(Controller ctrl) {
        Controller.Type t = ctrl.getType();
        return t == Controller.Type.MOUSE || t == Controller.Type.KEYBOARD || t == Controller.Type.UNKNOWN;
    }

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
