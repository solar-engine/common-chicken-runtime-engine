/*
 * Copyright 2014-2015 Colby Skeggs.
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
package ccre.supercanvas.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.OutputStream;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;

/**
 * A SuperCanvas-based component to allow unit-testing the Cluck implementation.
 * Works in conjunction with UnitTestBot.
 * 
 * @author skeggsc
 */
public class CluckUnitTestComponent extends DraggableBoxComponent {

    private static final Color bodyColor = new Color(102, 2, 60);
    
    private final CollapsingWorkerThread tester = new CollapsingWorkerThread("Cluck-Unit-Tester", true) {
        
        BooleanOutput bo = Cluck.subscribeBO("robot/utest-bo0");
        BooleanInput bi = Cluck.subscribeBI("robot/utest-bi0", false);
        EventOutput eo = Cluck.subscribeEO("robot/utest-eo0");
        EventInput ei = Cluck.subscribeEI("robot/utest-ei0");
        FloatOutput fo = Cluck.subscribeFO("robot/utest-fo0");
        FloatInput fi = Cluck.subscribeFI("robot/utest-fi0", false);
        LoggingTarget lt = Cluck.subscribeLT("robot/utest-lt0", LogLevel.FINEST);
        OutputStream os = Cluck.subscribeOS("robot/utest-os0");
        
        private int ctr;
        
        {
            bi.send(new BooleanOutput() {
                public void set(boolean b) {
                    ctr++;
                }
            });
            ei.send(new EventOutput() {
                public void event() {
                    ctr++;
                }
            });
            fi.send(new FloatOutput() {
                public void set(float f) {
                    ctr++;
                }
            });
        }
        
        private void checkSetBoolean(boolean b) throws InterruptedException {
            int start = ctr;
            bo.set(b);
            this.wait(2000);
            if (ctr != start + 1) {
                
            }
        }
        
        @Override
        protected void doWork() throws Throwable {
            Logger.info("Starting test...");
            ctr = 0;
        }
    };

    /**
     * Create a new CluckUnitTestComponent at the given location.
     * 
     * @param cx the X position.
     * @param cy the Y position.
     */
    public CluckUnitTestComponent(int cx, int cy) {
        super(cx, cy);
        halfWidth = 80;
        halfHeight = 80;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        Rendering.drawBody(bodyColor, g, this);
        g.setColor(Color.BLACK);
        int baseY = centerY - halfHeight + fontMetrics.getAscent();
        String str = tester.isDoingWork() ? "Testing..." : "Ready";
        g.drawString(str, centerX - g.getFontMetrics().stringWidth(str) / 2, baseY);
    }

    @Override
    public boolean onInteract(int x, int y) {
        tester.trigger();
        return true;
    }

    @Override
    public String toString() {
        return "Cluck Unit Tester";
    }
}
