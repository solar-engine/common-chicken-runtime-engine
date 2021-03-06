/*
 * Copyright 2015-2016 Cel Skeggs
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
package ccre.timers;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ccre.channel.CancelOutput;
import ccre.channel.EventOutput;
import ccre.log.LogLevel;
import ccre.log.VerifyingLogger;
import ccre.scheduler.VirtualTime;

@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class TickerTest {

    private static final String ERROR_STRING = "Ticker purposeful failure.";

    private Ticker ticker;
    private final int period;
    private final boolean fixedRate;
    private int counter;

    private final EventOutput cb = () -> {
        synchronized (TickerTest.this) {
            counter++;
        }
    };

    public TickerTest(int period, boolean fixedRate) {
        this.period = period;
        this.fixedRate = fixedRate;
    }

    @Before
    public void setUp() {
        VirtualTime.startFakeTime();
        ticker = new Ticker(period, fixedRate);
        counter = 0;
        VerifyingLogger.begin();
    }

    @Parameters
    public static Collection<Object[]> generateData() {
        ArrayList<Object[]> out = new ArrayList<>();
        // the different timings here aren't really relevant - this just tests
        // more birds with two stones.
        out.add(new Object[] { 770, false });
        out.add(new Object[] { 760, true });
        return out;
    }

    @After
    public void tearDown() {
        VerifyingLogger.checkAndEnd();
        ticker.terminate();
        ticker = null;
        VirtualTime.endFakeTime();
    }

    private synchronized void check(int ctr) throws InterruptedException {
        if (ctr != counter) {
            fail("expected <" + ctr + "> but got <" + counter + "> in " + period);
        }
    }

    @Test
    public void testCounting() throws InterruptedException {
        start(cb);
        for (int i = 0; i < 4; i++) {
            VirtualTime.forward(period);
        }
        check(4);
    }

    private CancelOutput start(EventOutput eo) throws InterruptedException {
        return ticker.send(eo);
    }

    @Test
    public void testSlowCounting() throws InterruptedException {
        if (period > 1) {
            start(cb);
            for (int i = 0; i < 15; i++) {
                VirtualTime.forward(period / 2);
                VirtualTime.forward(period - (period / 2));
            }
            check(15);
        }
    }

    @Test
    public void testFastCounting() throws InterruptedException {
        start(cb);
        VirtualTime.forward(period * 20);
        // skipping time that fast doesn't work well with this version of Ticker
        // - that's what the option is for!
        check(fixedRate ? 20 : 1); // flaky
    }

    @Test
    public void testUnbind() throws InterruptedException {
        CancelOutput unbind = start(cb);
        for (int i = 0; i < 20; i++) {
            VirtualTime.forward(period);
        }
        check(20);

        unbind.cancel();

        VirtualTime.forward(period * 10);

        check(20);// nothing more

        for (int i = 0; i < 5; i++) {
            VirtualTime.forward(period);
        }

        check(20);// nothing more
    }

    @Test
    public void testRebind() throws InterruptedException {
        start(cb).cancel();// bind and unbind
        start(cb);// rebind
        for (int i = 0; i < 5; i++) {
            VirtualTime.forward(period);
        }
        check(5);
    }

    @Test
    public void testLoopAFewErrors() throws InterruptedException {
        start(() -> {
            synchronized (TickerTest.this) {
                counter++;
                if (counter < 6) {
                    throw new RuntimeException(ERROR_STRING);
                }
            }
        });
        for (int i = 0; i < 20; i++) {
            if (i < 5) {
                VerifyingLogger.configure(LogLevel.SEVERE, "Top-level failure in scheduled event", (t) -> t.getClass() == RuntimeException.class && ERROR_STRING.equals(t.getMessage()));
            }
            VirtualTime.forward(period);
            VerifyingLogger.check();
        }
        check(20);
    }

    @Test
    public void testLoopScatteredErrors() throws InterruptedException {
        start(() -> {
            synchronized (TickerTest.this) {
                counter++;
                if (counter % 10 == 0) {
                    throw new RuntimeException(ERROR_STRING);
                }
            }
        });
        for (int i = 0; i < 70; i++) {
            if (i % 10 == 9) {
                VerifyingLogger.configure(LogLevel.SEVERE, "Top-level failure in scheduled event", (t) -> t.getClass() == RuntimeException.class && ERROR_STRING.equals(t.getMessage()));
            }
            VirtualTime.forward(period);
            VerifyingLogger.check();
        }
        check(70);
    }
}
