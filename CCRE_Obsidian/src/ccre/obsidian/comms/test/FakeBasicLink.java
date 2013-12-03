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

import ccre.concurrency.ReporterThread;
import ccre.log.Logger;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A basic link mockup for use when a real link isn't available for testing link
 * code.
 *
 * @author skeggsc
 */
public abstract class FakeBasicLink {

    private BlockingQueue<byte[]> transToAlpha = new ArrayBlockingQueue<byte[]>(10);
    private BlockingQueue<byte[]> transToBeta = new ArrayBlockingQueue<byte[]>(10);
    private ReporterThread alphaReciver = new ReporterThread("Alpha-Recv") {
        @Override
        protected void threadBody() throws InterruptedException {
            Logger.finest("Starting Alpha-Recv");
            Random r = new Random();
            while (true) {
                byte[] cur = transToAlpha.take();
                int blen = (10 + r.nextInt(30) + cur.length * 5) / 2;
                Logger.finer("Alpha-Recv receiving: " + Arrays.toString(cur) + " with base delay " + blen + " ms");
                if (r.nextInt(100) < 10) {
                    long delay = (long) (blen * r.nextFloat());
                    Logger.finest("Alpha-Recv Failed transmit! Will lose " + delay + " ms");
                    // Message lost in transit!
                    Thread.sleep(delay);
                } else {
                    Logger.finest("Alpha-Recv Working transmit!");
                    Thread.sleep(blen);
                    Logger.finest("Alpha-Recv Succeeded transmit!");
                    long start = System.currentTimeMillis();
                    receiveAlphaEnd(cur, 0, cur.length);
                    long len = System.currentTimeMillis() - start;
                    Logger.finer("Length of Alpha send: " + len);
                }
            }
        }
    };
    private ReporterThread betaReciver = new ReporterThread("Beta-Recv") {
        @Override
        protected void threadBody() throws InterruptedException {
            Logger.finest("Starting Beta-Recv");
            Random r = new Random();
            while (true) {
                byte[] cur = transToBeta.take();
                int blen = (10 + r.nextInt(30) + cur.length * 5) / 2;
                Logger.finest("Beta-Recv receiving: " + Arrays.toString(cur) + " with base delay " + blen + " ms");
                if (r.nextInt(100) < 10) {
                    long delay = (long) (blen * r.nextFloat());
                    Logger.finest("Beta-Recv Failed transmit! Will lose " + delay + " ms");
                    // Message lost in transit!
                    Thread.sleep(delay);
                } else {
                    Logger.finest("Beta-Recv Working transmit!");
                    Thread.sleep(blen);
                    Logger.finest("Beta-Recv Succeeded transmit!");
                    long start = System.currentTimeMillis();
                    receiveBetaEnd(cur, 0, cur.length);
                    long len = System.currentTimeMillis() - start;
                    Logger.finer("Length of Beta send: " + len);
                }
            }
        }
    };
    
    public void start() {
        alphaReciver.start();
        betaReciver.start();
    }

    protected abstract void receiveAlphaEnd(byte[] bytes, int offset, int count);

    protected abstract void receiveBetaEnd(byte[] bytes, int offset, int count);

    public void sendAlphaEnd(byte[] bytes, int offset, int count) {
        if (!transToBeta.offer(Arrays.copyOfRange(bytes, offset, offset + count))) {
            Logger.fine("Lost packet to Beta: " + Arrays.toString(Arrays.copyOfRange(bytes, offset, offset + count)));
            transToBeta.poll(); // Cause the queue to rotate more.
        }
    }

    public void sendBetaEnd(byte[] bytes, int offset, int count) {
        if (!transToAlpha.offer(Arrays.copyOfRange(bytes, offset, offset + count))) {
            Logger.fine("Lost packet to Alpha: " + Arrays.toString(Arrays.copyOfRange(bytes, offset, offset + count)));
            transToAlpha.poll(); // Cause the queue to rotate more.
        }
    }
}
