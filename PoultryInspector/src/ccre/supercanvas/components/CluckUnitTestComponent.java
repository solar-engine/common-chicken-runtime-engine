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
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.Cluck;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.log.LogLevel;
import ccre.log.LoggingTarget;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;
import ccre.util.LineCollectorOutputStream;

/**
 * A SuperCanvas-based component to allow unit-testing the Cluck implementation.
 * Works in conjunction with UnitTestBot.
 *
 * @author skeggsc
 */
public class CluckUnitTestComponent extends DraggableBoxComponent {
    private static Logger logger = LoggerFactory.getLogger(CluckUnitTestComponent.class);

    private static final long serialVersionUID = 4921789619177479709L;

    private final class CluckUnitTesterWorker extends CollapsingWorkerThread {
        public static final int MAX_CTR = 19;
        BooleanOutput bo = Cluck.subscribeBO("robot/utest-bo0");
        BooleanInput bi = Cluck.subscribeBI("robot/utest-bi0", false);
        EventOutput eo = Cluck.subscribeEO("robot/utest-eo0");
        EventInput ei = Cluck.subscribeEI("robot/utest-ei0");
        FloatOutput fo = Cluck.subscribeFO("robot/utest-fo0");
        FloatInput fi = Cluck.subscribeFI("robot/utest-fi0", false);
        LoggingTarget lt = Cluck.subscribeLT("robot/utest-lt0", LogLevel.TRACE);
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
            Cluck.publish("utest-lt1", new LoggingTarget() {
                public void log(LogLevel level, String message, String extended) {
                    // TODO: Do more with other possible logging messages (with throwables or extended)
                    if (level == LogLevel.TRACE && message.equals(testMessage0) && extended == null) {
                        ctr++;
                    } else {
                        logger.warn("Wrong log data sent!");
                        success = false;
                    }
                }

                public void log(LogLevel level, String message, Throwable throwable) {
                    logger.warn("Wrong log event called!");
                    success = false;
                }
            });
            Cluck.publish("utest-os1", new LineCollectorOutputStream() {
                @Override
                protected void collect(String param) {
                    if (param.equals("THIS IS THE CCRE TALKING.\0 send properly. DO IT.")) {
                        ctr++;
                    } else {
                        logger.warn("Wrong test data sent!");
                        success = false;
                    }
                }
            });
        }
        private static final String testMessage0 = "{}{}{}[][][]()()() TESTING 1234567890____!";
        private boolean success = false;

        private CluckUnitTesterWorker(String name, boolean shouldIgnoreWhileRunning) {
            super(name, shouldIgnoreWhileRunning);
        }

        private void checkSendLog() throws InterruptedException {
            int start = ctr;
            lt.log(LogLevel.TRACE, testMessage0, (Throwable) null);
            this.wait(200);
            if (ctr != start + 1) {
                logger.warn("Unit testing failed - send log failed.");
                success = false;
            }
        }

        private void checkSendData() throws InterruptedException, IOException {
            int start = ctr;
            os.write("THIS IS THE CCRE TALKING.\0 send properly. DO IT.\n".getBytes());
            this.wait(200);
            if (ctr != start + 1) {
                logger.warn("Unit testing failed - send data failed.");
                success = false;
            }
        }

        private void checkSetBoolean(boolean b, boolean expect) throws InterruptedException {
            int start = ctr;
            bo.set(b);
            this.wait(200);
            if (ctr != start + (expect ? 1 : 0)) {
                logger.warn("Unit testing failed - boolean event count wrong.");
                success = false;
            }
            if (bi.get() != b) {
                logger.warn("Unit testing failed - boolean value wrong.");
                success = false;
            }
        }

        private void checkSetFloat(float f, boolean expect) throws InterruptedException {
            int start = ctr;
            fo.set(f);
            this.wait(200);
            if (ctr != start + (expect ? 1 : 0)) {
                logger.warn("Unit testing failed - float event count wrong.");
                success = false;
            }
            if (fi.get() != f) {
                logger.warn("Unit testing failed - float value wrong.");
                success = false;
            }
        }

        private void checkSetEvent() throws InterruptedException {
            int start = ctr;
            eo.event();
            this.wait(200);
            if (ctr != start + 1) {
                logger.warn("Unit testing failed - event count wrong.");
                success = false;
            }
        }

        @Override
        protected synchronized void doWork() throws Throwable {
            logger.info("Starting test...");
            bo.set(false);
            fo.set(0.3f);
            this.wait(200);
            ctr = 0;
            success = true;
            checkSetBoolean(false, false);
            checkSetBoolean(false, false);
            checkSetBoolean(true, true);
            checkSetBoolean(true, false);
            checkSetFloat(0.0f, true);
            checkSetFloat(0.0f, false);
            checkSetEvent();
            checkSetFloat(1.0f, true);
            checkSetFloat(2.3f, true);
            checkSendData();
            checkSetFloat(1.0f, true);
            checkSetBoolean(false, true);
            checkSetEvent();
            checkSetBoolean(true, true);
            checkSetBoolean(false, true);
            checkSendLog();
            checkSetBoolean(true, true);
            checkSetFloat(0.0f, true);
            //checkSetFloat(Float.NaN, true);
            checkSetEvent();
            checkSetFloat(7.2f, true);
            checkSetFloat(Float.NEGATIVE_INFINITY, true);
            checkSetFloat(Float.NEGATIVE_INFINITY, false);
            checkSetFloat(0.0f, true);
            checkSetFloat(-6.2f, true);
            if (ctr != MAX_CTR) {
                success = false;
                logger.warn("Unit testing failed: wrong number of overall message events!");
            }
            if (success) {
                logger.info("Unit tests completed successfully!");
            } else {
                logger.warn("Unit tests failed.");
            }
        }
    }

    private static final Color bodyColor = new Color(202, 4, 120);

    private transient CluckUnitTesterWorker tester;

    /**
     * Create a new CluckUnitTestComponent at the given location.
     *
     * @param cx the X position.
     * @param cy the Y position.
     */
    public CluckUnitTestComponent(int cx, int cy) {
        super(cx, cy);
        halfWidth = 80;
        halfHeight = 20;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        Rendering.drawBody(bodyColor, g, this);
        g.setColor(Color.BLACK);
        int baseY = centerY - halfHeight / 2 + fontMetrics.getAscent();
        String str = tester != null && tester.isDoingWork() ? "Testing: " + tester.ctr + "/" + CluckUnitTesterWorker.MAX_CTR : "Ready";
        g.drawString(str, centerX - g.getFontMetrics().stringWidth(str) / 2, baseY);
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (tester == null) {
            tester = new CluckUnitTesterWorker("Cluck-Unit-Tester", true);
        }
        tester.trigger();
        return true;
    }

    @Override
    public String toString() {
        return "Cluck Unit Tester";
    }
}
