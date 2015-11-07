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

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.frc.Device;
import ccre.frc.components.BooleanTextComponent;
import ccre.frc.components.SpacingComponent;
import ccre.frc.components.TextComponent;
import ccre.timers.Ticker;

/**
 * A device allowing for input of tick-based increasing or decreasing values,
 * such as a gyro or encoder. This has a mode for position, and a mode for
 * velocity, so that both can be tested.
 *
 * @author skeggsc
 */
public class SpinDevice extends Device {

    private FloatCell ticks = new FloatCell();
    private int velocity = 0;
    private BooleanTextComponent isVelocityMode = new BooleanTextComponent("POSITION", "VELOCITY") {
        @Override
        public void onPress(int x, int y) {
            super.onPress(x, y);
            velocity = 0;
        }
    }.setEditable(true);
    private TextComponent positionView = new TextComponent("0");

    /**
     * Create a new SpinDevice with a given name and an optional event for when
     * to reset the value of the device to zero.
     *
     * @param title how to describe the device.
     * @param resetWhen when to reset the value of the device, or null if this
     * isn't needed.
     */
    public SpinDevice(String title, EventInput resetWhen) {
        add(new SpacingComponent(20));
        add(new TextComponent(title));
        if (resetWhen != null) {
            resetWhen.send(new EventOutput() {
                @Override
                public void event() {
                    setTicks(0);
                }
            });
        }
        add(isVelocityMode);
        add(new TextComponent("-") {
            @Override
            public void onPress(int x, int y) {
                pressButton(-15);
            }
        });
        add(new TextComponent("-") {
            @Override
            public void onPress(int x, int y) {
                pressButton(-5);
            }
        });
        add(new TextComponent("-") {
            @Override
            public void onPress(int x, int y) {
                pressButton(-1);
            }
        });
        add(positionView);
        add(new TextComponent("+") {
            @Override
            public void onPress(int x, int y) {
                pressButton(1);
            }
        });
        add(new TextComponent("+") {
            @Override
            public void onPress(int x, int y) {
                pressButton(5);
            }
        });
        add(new TextComponent("+") {
            @Override
            public void onPress(int x, int y) {
                pressButton(15);
            }
        });

        new Ticker(100).send(new EventOutput() {
            private int partials = 0;

            @Override
            public void event() {
                partials += velocity;
                while (partials >= 10) {
                    partials -= 10;
                    addTicks(1);
                }
                while (partials <= -10) {
                    partials += 10;
                    addTicks(-1);
                }
            }
        });
    }

    private void pressButton(int i) {
        if (isVelocityMode.get()) {
            velocity += i;
        } else {
            addTicks(i);
        }
    }

    /**
     * Provides an input that represents the current value on this SpinDevice.
     *
     * @return the FloatInput representing the value on this SpinDevice.
     */
    public FloatInput asInput() {
        return ticks;
    }

    private void addTicks(int ticks) {
        setTicks(this.ticks.get() + ticks);
    }

    private void setTicks(float ticks) {
        this.ticks.safeSet(ticks);
        positionView.setLabel(String.valueOf(ticks));
    }
}
