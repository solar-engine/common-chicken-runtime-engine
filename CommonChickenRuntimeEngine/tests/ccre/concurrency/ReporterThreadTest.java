/*
 * Copyright 2015 Cel Skeggs.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ccre.time.Time;

@SuppressWarnings("javadoc")
public class ReporterThreadTest {

    private boolean executed;
    private ReporterThread thread;

    @Before
    public void setUp() throws Exception {
        executed = false;
        thread = new TestThread("TestRT");
    }

    @Test
    public void testNoInitialExec() throws InterruptedException {
        Time.sleep(10);
        assertFalse(executed);
    }

    @Test(expected = IllegalStateException.class)
    public void testPreventDirectRun() {
        thread.run();
    }

    @Test
    public void testThreadName() {
        assertTrue(thread.getName().startsWith("TestRT-"));
    }

    @Test
    public void testThreadNameAutoIncrement() {
        int last = Integer.parseInt(thread.getName().substring("TestRT-".length()));
        assertEquals(new ReporterThread("TestRT") {
            @Override
            protected void threadBody() throws Throwable {
            }
        }.getName(), "TestRT-" + (last + 1));
    }

    @Test
    public void testExecution() throws InterruptedException {
        thread.start();
        thread.join();
        assertTrue(executed);
    }

    private class TestThread extends ReporterThread {
        TestThread(String name) {
            super(name);
        }

        @Override
        protected void threadBody() {
            try {
                this.run();
            } catch (IllegalStateException e) {
                executed = true;
            }
        }
    }

}
