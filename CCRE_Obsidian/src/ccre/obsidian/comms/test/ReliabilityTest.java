/*
 * Copyright 2013 Vincent Miller
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
package ccre.obsidian.comms.test;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.cluck.CluckNode;
import ccre.concurrency.ReporterThread;
import ccre.ctrl.Ticker;
import ccre.channel.EventOutput;
import ccre.channel.EventLogger;
import ccre.cluck.CluckPublisher;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.obsidian.comms.ReliableCompressionCluckLink;
import java.util.Arrays;
import java.util.Random;

/**
 * Some code for testing the ReliableLink over a FakeBasicLink.
 *
 * @author skeggsc
 */
public class ReliabilityTest {

    public final FakeBasicLink fbl = new FakeBasicLink() {
        @Override
        protected void receiveAlphaEnd(byte[] bytes, int offset, int count) {
            alphaEnd.addBasicReceiveToQueue(Arrays.copyOfRange(bytes, offset, offset + count));
            //alphaEnd.basicReceiveHandler(bytes, offset, count);
        }

        @Override
        protected void receiveBetaEnd(byte[] bytes, int offset, int count) {
            betaEnd.addBasicReceiveToQueue(Arrays.copyOfRange(bytes, offset, offset + count));
            betaEnd.basicReceiveHandler(bytes, offset, count);
        }
    };
    public final CluckNode alphaNode = new CluckNode();
    public final ReliableCompressionCluckLink alphaEnd = new ReliableCompressionCluckLink(alphaNode, "beta") {
        @Override
        protected void basicStartComms() {
            Logger.info("Starting alpha: " + this);
        }

        @Override
        protected void basicTransmit(byte[] packet, int offset, int count) {
            fbl.sendAlphaEnd(packet, offset, count);
        }

        @Override
        public String toString() {
            return "[ALPHAEND]";
        }
    };
    public final CluckNode betaNode = new CluckNode();
    public final ReliableCompressionCluckLink betaEnd = new ReliableCompressionCluckLink(betaNode, "alpha") {
        @Override
        protected void basicStartComms() {
            Logger.info("Starting beta: " + this);
        }

        @Override
        protected void basicTransmit(byte[] packet, int offset, int count) {
            fbl.sendBetaEnd(packet, offset, count);
        }

        @Override
        public String toString() {
            return "[BETAEND]";
        }
    };

    public static void main(String[] args) throws InterruptedException {
        //Logger.minimumLevel = LogLevel.INFO;
        Logger.info("Starting RT systems");
        final ReliabilityTest rt = new ReliabilityTest();
        //rt.alphaNode.debugLogAll = rt.betaNode.debugLogAll = true;
        rt.startSubsystems();
        Logger.info("Delaying...");
        Thread.sleep(2000);
        Logger.info("Starting main threads");
        new ReporterThread("Alpha-Main") {
            @Override
            protected void threadBody() throws Throwable {
                //rt.alphaNode.publish("log-test", new EventLogger(LogLevel.INFO, "Log-Test on ALPHA!"));
                final FloatStatus ctr = new FloatStatus();
                new Ticker(100).send(new EventOutput() {
                    Random r = new Random();

                    @Override
                    public void event() {
                        ctr.set((float) r.nextGaussian());
                    }
                });
                ctr.send(new FloatOutput() {
                    @Override
                    public void set(float value) {
                        Logger.info("Sent new value: " + value);
                    }
                });
                CluckPublisher.publish(rt.alphaNode, "intest", (FloatInput) ctr);
                CluckPublisher.publish(rt.alphaNode, "checker", new EventOutput() {
                    long start = System.currentTimeMillis();
                    long last = start;

                    @Override
                    public void event() {
                        long now = System.currentTimeMillis();
                        Logger.info("Received at " + (now - start) / 1000.0f + " = +" + (now - last));
                        if (now - last < 5) {
                            Logger.info("Double-up!");
                        }
                        last = now;
                    }
                });
            }
        }.start();
        new ReporterThread("Beta-Main") {
            @Override
            protected void threadBody() throws Throwable {
                //new Ticker(100).addListener(rt.betaNode.subscribeEC("alpha/checker"));
                CluckPublisher.subscribeFI(rt.betaNode, "alpha/unF/intest", false).send(new FloatOutput() {
                    @Override
                    public void set(float value) {
                        Logger.info("Got new value: " + value);
                    }
                });
                //rt.betaNode.subscribeEC("alpha/beta/log-test").event();
            }
        }.start();
    }

    public void startSubsystems() {
        fbl.start();
        alphaEnd.start();
        betaEnd.start();
    }
}
