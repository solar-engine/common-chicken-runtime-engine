/*
 * Copyright 2013 Colby Skeggs
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
package ccre.examples.concurrency;

import ccre.concurrency.ReporterThread;
import ccre.log.Logger;

/**
 * An example of how a ReporterThread can be used, in comparison to a standard
 * Java Thread.
 *
 * @author skeggsc
 */
public class ReporterThreadExample {

    /**
     * Run the same hello-world program with a ReporterThread and a normal
     * Thread, to see the difference.
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        /*
         * The primary advantage of ReporterThread is that it's harder to break.
         * 
         * It requires a thread name always, which can make debugging easier,
         * and autoadds a unique ID so that the threads can be distinguished.
         * 
         * If you call the run() method yourself, the thread will throw an
         * exception and not actually run the contents. This means that misuse
         * of a ReporterThread won't cause the code to be ran multiple times.
         * 
         * The threadBody() method throws a Throwable, which means that you
         * don't need to write your own handling for all errors.
         * 
         * The threadBody() method is also protected, meaning that no one else
         * will be able to call the body, unlike the run() method which must be
         * public by definition.
         */
        ReporterThread mt = new ReporterThread("MyThread") {
            @Override
            protected void threadBody() throws Throwable {
                Logger.info("Hello world from a ReporterThread!");
                throw new Exception("This is how a ReporterThread exception looks.");
            }
        };
        mt.start();
        mt.join();
        new Thread() {
            @Override
            public void run() {
                Logger.info("Hello world from a Thread!");
                // Must be a RuntimeException or an Error since normal exceptions cannot be thrown from run().
                throw new RuntimeException("This is how a Java Thread exception looks.");
            }
        }.start();
    }
}
