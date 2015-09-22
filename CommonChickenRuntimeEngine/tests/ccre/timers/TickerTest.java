/*
 * Copyright 2015 Colby Skeggs
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventOutput;
import ccre.time.FakeTime;
import ccre.time.Time;

@RunWith(Parameterized.class)
public class TickerTest {
    private static Logger logger = LoggerFactory.getLogger(TickerTest.class);

    private static Time oldProvider;
    private static FakeTime fake;
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

    @BeforeClass
    public static void setUpClass() {
        assertNull(oldProvider);
        oldProvider = Time.getTimeProvider();
        fake = new FakeTime();
        Time.setTimeProvider(fake);
    }

    @Before
    public void setUp() {
        ticker = new Ticker(period, fixedRate);
        counter = 0;
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
        ticker.terminate();
        ticker = null;
    }

    @AfterClass
    public static void tearDownClass() {
        assertNotNull(oldProvider);
        Time.setTimeProvider(oldProvider);
        oldProvider = null;
        fake = null;
    }

    private synchronized void check(int ctr) throws InterruptedException {
        if (ctr != counter) {
            fail("expected <" + ctr + "> but got <" + counter + "> in " + period);
        }
        // assertEquals(ctr, counter);
    }

    @Test
    public void testCounting() throws InterruptedException {
        start(cb);
        for (int i = 0; i < 4; i++) {
            fake.forward(period);
        }
        check(4);
    }

    private EventOutput start(EventOutput eo) throws InterruptedException {
        EventOutput unbind = ticker.sendR(eo);
        Thread.sleep(5);
        return unbind;
    }

    @Test
    public void testSlowCounting() throws InterruptedException {
        if (period > 1) {
            start(cb);
            for (int i = 0; i < 15; i++) {
                fake.forward(period / 2);
                fake.forward(period - (period / 2));
            }
            check(15);
        }
    }

    @Test
    public void testFastCounting() throws InterruptedException {
        start(cb);
        fake.forward(period * 20);
        // skipping time that fast doesn't work well with this version of Ticker
        // - that's what the option is for!
        check(fixedRate ? 20 : 1);
    }

    @Test
    public void testUnbind() throws InterruptedException {
        EventOutput unbind = start(cb);
        for (int i = 0; i < 20; i++) {
            fake.forward(period);
        }
        check(20);

        unbind.event();

        fake.forward(period * 10);

        check(20);// nothing more

        for (int i = 0; i < 5; i++) {
            fake.forward(period);
        }

        check(20);// nothing more
    }

    @Test
    public void testRebind() throws InterruptedException {
        start(cb).event();// bind and unbind
        start(cb);// rebind
        for (int i = 0; i < 5; i++) {
            fake.forward(period);
        }
        check(5);
    }

    @Test
    public void testLoopTooManyErrors() throws InterruptedException {
        logger.info("The following ticker main loop errors are purposeful.");
        start(() -> {
            synchronized (TickerTest.this) {
                counter++;
            }
            throw new RuntimeException("Ticker purposeful failure.");
        });
        for (int i = 0; i < 20; i++) {
            fake.forward(period);
        }
        check(6);// only the first six would have occurred: after the exception
                 // gets thrown on that last time, it gets detached.
        for (int i = 0; i < 10; i++) {
            fake.forward(period);
        }
        check(6);// nothing more, preferably
    }

    @Test
    public void testLoopAFewErrors() throws InterruptedException {
        logger.info("The following ticker main loop errors are purposeful.");
        start(() -> {
            synchronized (TickerTest.this) {
                counter++;
                if (counter < 6) {
                    throw new RuntimeException("Ticker purposeful failure.");
                }
            }
        });
        // here, unlike above, only five failures occur - so nothing gets
        // detached, and it keeps working.
        for (int i = 0; i < 20; i++) {
            fake.forward(period);
        }
        check(20);
    }

    @Test
    public void testLoopScatteredErrors() throws InterruptedException {
        logger.info("The following ticker main loop errors are purposeful.");
        start(() -> {
            synchronized (TickerTest.this) {
                counter++;
                if (counter % 10 == 0) {
                    throw new RuntimeException("Ticker purposeful failure.");
                }
            }
        });
        // here, unlike above, the failures happen infrequently enough that
        // nothing actually gets detached, and it keeps working
        for (int i = 0; i < 70; i++) {
            fake.forward(period);
        }
        check(70);
    }
}
