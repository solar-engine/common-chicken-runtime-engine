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
package poultryinspector.controller;

import ccre.chan.BooleanInput;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatInput;
import ccre.chan.FloatOutput;
import ccre.ctrl.Ticker;
import ccre.event.EventConsumer;
import ccre.util.CLinkedList;
import ccre.util.CList;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier.Axis;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 * An input device such as a joystick or gamepad. Since the constructor is
 * private, use getControllers to get all connected devices. This class is a
 * wrapper for JInput.
 *
 * @author MillerV
 */
public class RobotController {

    private Controller ctrl;
    private static final Axis[] AxisIDS = new Axis[]{
        Axis.X, Axis.Y, Axis.Z, Axis.SLIDER, Axis.RX, Axis.RY, Axis.RZ
    };
    private static final Button[] ButtonIDS = new Button[]{
        Button._0, Button._1, Button._2, Button._3, Button._4, Button._5, Button._6, Button._7, Button._8, Button._9,
        Button._10, Button._11
    };

    /**
     * Gets an array of all compatible devices connected to the computer. Since
     * all devices must be looped through, it is recommended that this method is
     * called only once.
     *
     * @return An array of all connected controllers.
     */
    public static RobotController[] getControllers() {
        Controller[] ctrls = ControllerEnvironment.getDefaultEnvironment().getControllers();
        RobotController[] controllers = new RobotController[ctrls.length];

        for (int i = 0; i < ctrls.length; i++) {
            controllers[i] = new RobotController(ctrls[i]);
        }
        return controllers;
    }

    private RobotController(Controller ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * @return The type of this controller.
     * @see Controller.Type
     */
    public String getType() {
        return ctrl.getType().toString();
    }

    /**
     * @return The name of this controller, as specified by the manufacturer.
     */
    public String getName() {
        return ctrl.getName();
    }

    /**
     * Gets an axis component of this controller, as a FloatInputPoll.
     *
     * @param id The Identifier for the component.
     * @return The component with that Identifier.
     * @throws InputTypeException
     * @see getComponents()
     */
    public FloatInput getAxis(int axis) throws InputTypeException {
        Component comp = ctrl.getComponent(AxisIDS[axis - 1]);
        if (comp == null) {
            return null;
        }
        if (!comp.isAnalog()) {
            throw new InputTypeException("Error: expected analog input.");
        }
        return new AxisInput(comp, ctrl);
    }

    /**
     * Gets a button component of this controller, as a BooleanInputPoll.
     *
     * @param id The Identifier for the component.
     * @return The component with that Identifier.
     * @throws InputTypeException
     * @see getComponents()
     */
    public BooleanInput getButton(int button) throws InputTypeException {
        Component comp = ctrl.getComponent(ButtonIDS[button - 1]);
        if (comp == null) {
            return null;
        }
        if (comp.isAnalog()) {
            throw new InputTypeException("Error: expected digital input.");
        }
        return new ButtonInput(comp, ctrl);
    }

    private class AxisInput implements FloatInput {

        private Component component;
        private Controller controller;
        private CList<FloatOutput> subscribers;
        private float lastValue;

        private AxisInput(Component component, Controller controller) {
            this.component = component;
            this.controller = controller;
            this.subscribers = new CLinkedList<FloatOutput>();
            this.lastValue = 0.0f;

            Ticker t = new Ticker(10);

            t.addListener(new EventConsumer() {
                @Override
                public void eventFired() {
                    float value = readValue();
                    if (value != lastValue) {
                        for (FloatOutput output : subscribers) {
                            output.writeValue(value);
                        }
                        lastValue = value;
                    }
                }
            });
        }

        @Override
        public float readValue() {
            controller.poll();
            return component.getPollData();
        }

        @Override
        public void addTarget(FloatOutput output) {
            subscribers.add(output);
        }

        @Override
        public boolean removeTarget(FloatOutput output) {
            return subscribers.remove(output);
        }
    }

    private final class ButtonInput implements BooleanInput {

        private Component component;
        private Controller controller;
        private CList<BooleanOutput> subscribers;
        private boolean lastValue;

        private ButtonInput(Component component, Controller controller) {
            this.component = component;
            this.controller = controller;
            this.subscribers = new CLinkedList<BooleanOutput>();
            this.lastValue = false;

            Ticker t = new Ticker(10);

            t.addListener(new EventConsumer() {
                @Override
                public void eventFired() {
                    boolean value = readValue();
                    if (value != lastValue) {
                        for (BooleanOutput output : subscribers) {
                            output.writeValue(value);
                        }
                        lastValue = value;
                    }
                }
            });
        }

        @Override
        public boolean readValue() {
            controller.poll();
            return component.getPollData() == 1.0f;
        }

        @Override
        public void addTarget(BooleanOutput output) {
            subscribers.add(output);
        }

        @Override
        public boolean removeTarget(BooleanOutput output) {
            return subscribers.remove(output);
        }
    }
}
