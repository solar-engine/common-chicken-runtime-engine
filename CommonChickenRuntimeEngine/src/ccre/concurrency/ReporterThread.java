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
package ccre.concurrency;

import ccre.log.LogLevel;
import ccre.log.Logger;
import java.io.InterruptedIOException;

/**
 * A nice wrapper for the builtin Thread. Provides a system to prevent execution
 * of the run outside of the target thread, provides an easy way to name the
 * thread, provides a builtin handler for throwables, and provides an abstract
 * method for the body of the thread.
 *
 * @author skeggsc
 */
public abstract class ReporterThread extends Thread {

    /**
     * The next ID to use for a ReporterThread.
     */
    private static int id = 0;
    /**
     * Has the run method already been called?
     */
    private boolean started = false;

    /**
     * Create a new ReporterThread. The passed name will have a unique ID
     * appended to it.
     *
     * @param name the name of this type of thread.
     */
    public ReporterThread(String name) {
        super(name + "-" + id++);
    }

    @Override
    public final void run() {
        if (this != Thread.currentThread()) {
            Logger.severe("Run function of Thread " + getName() + " called directly!");
            return;
        }
        if (started) {
            Logger.severe("Run function of Thread " + getName() + " recalled!");
            return;
        }
        started = true;
        try {
            threadBody();
        } catch (OutOfMemoryError oom) {
            Logger.severe("OOM");
            throw oom; // The out-of-memory error will crash the system.
        } catch (InterruptedIOException ex) {
            Logger.log(LogLevel.WARNING, "Interruption (during IO) of Thread " + this.getName(), ex);
        } catch (InterruptedException ex) {
            Logger.log(LogLevel.WARNING, "Interruption of Thread " + this.getName(), ex);
        } catch (Throwable thr) {
            Logger.log(LogLevel.SEVERE, "Abrupt termination of Thread " + this.getName(), thr);
        }
    }

    /**
     * The body of the thread. This will be called when the thread starts. This
     * is guarenteed to be called precisely once by the ReporterThread.
     *
     * @throws Throwable if something goes wrong. this will be caught by the
     * ReporterThread automatically.
     */
    protected abstract void threadBody() throws Throwable;
}
