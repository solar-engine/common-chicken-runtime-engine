/*
 * Copyright 2014 Colby Skeggs
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

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInputPoll;
import ccre.ctrl.Ticker;
import ccre.igneous.Device;
import ccre.igneous.components.BooleanTextComponent;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

public class SpinDevice extends Device implements FloatInputPoll {
    
    private int ticks = 0;
    private int velocity = 0;
    private BooleanTextComponent isVelocityMode = new BooleanTextComponent("POSITION", "VELOCITY") {
        public void onPress(int x, int y) {
            super.onPress(x, y);
            velocity = 0;
        }
    }.setEditable(true);
    private TextComponent positionView = new TextComponent("0");

    public SpinDevice(String title, EventInput resetWhen) {
        add(new SpacingComponent(20));
        add(new TextComponent(title));
        if (resetWhen != null) {
            resetWhen.send(new EventOutput() {
                public void event() {
                    setTicks(0);
                }
            });
        }
        add(isVelocityMode);
        add(new TextComponent("-") {
            public void onPress(int x, int y) {
                pressButton(-10);
            }
        });
        add(new TextComponent("-") {
            public void onPress(int x, int y) {
                pressButton(-1);
            }
        });
        add(positionView);
        add(new TextComponent("+") {
            public void onPress(int x, int y) {
                pressButton(1);
            }
        });
        add(new TextComponent("+") {
            public void onPress(int x, int y) {
                pressButton(5);
            }
        });
        
        new Ticker(100).send(new EventOutput() {
            private int partials = 0;
            public void event() {
                partials += velocity;
                while (partials >= 10) {
                    partials -= 10;
                    setTicks(ticks + 1);
                }
                while (partials <= -10) {
                    partials += 10;
                    setTicks(ticks - 1);
                }
            }
        });
    }

    private void pressButton(int i) {
        if (isVelocityMode.get()) {
            velocity += i;
        } else {
            setTicks(ticks + i);
        }
    }

    public float get() {
        return ticks;
    }

    private void setTicks(int ticks) {
        this.ticks = ticks;
        positionView.setLabel(String.valueOf(ticks));
    }
}
