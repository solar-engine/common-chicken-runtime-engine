package ccre.timers;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.time.FakeTime;
import ccre.time.Time;

public class ElapsedTimerTest {
    private static Time oldProvider;
    private static FakeTime fake;
    private ElapsedTimer timer;

    @BeforeClass
    public static void setUpClass() throws Exception {
        assertNull(oldProvider);
        oldProvider = Time.getTimeProvider();
        fake = new FakeTime();
        Time.setTimeProvider(fake);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        assertNotNull(oldProvider);
        Time.setTimeProvider(oldProvider);
        oldProvider = null;
        fake = null;
    }

    @Before
    public void setUp() throws Exception {
        timer = new ElapsedTimer();
    }

    @After
    public void tearDown() throws Exception {
        timer.reset();
        timer = null;
    }

    @Test
    public void testElapsedTimer() throws InterruptedException {
        Random rand = new Random();
        timer.start();
        assertTrue(timer.isRunning.get());
        long currentTime = timer.read();
        for (int i = 0; i < 100; i++) {
            System.out.println("fuck" + i);
            int delay = rand.nextInt(10000);
            fake.forward(delay);
            currentTime += delay;
            assertEquals(timer.read(), currentTime);
        }
    }
    
    @Test(expected = IllegalStateException.class)
    public void testReadUnstartedTimer() {
        timer.read();
    }
    
    @Test(expected = IllegalStateException.class)
    public void testStartingRunningTimer() {
        timer.start();
        timer.start();
    }
}
