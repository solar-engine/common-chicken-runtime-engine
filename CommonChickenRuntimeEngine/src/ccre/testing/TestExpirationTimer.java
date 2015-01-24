package ccre.testing;

import ccre.channel.BooleanStatus;
import ccre.channel.FloatStatus;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.FloatMixing;
import ccre.util.Utils;

public class TestExpirationTimer extends BaseTest {
    // Warning: there IS some amount of dependency in these tests on timing, and this may have issues with Java implementations.
    // Sometimes, they'll probably fail randomly.

    @Override
    public String getName() {
        return "ExpirationTimer tests";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        testAlarmClock();
        testLightSequence();
        testTunableClock();
    }

    private void testAlarmClock() throws TestingException, InterruptedException {
        ExpirationTimer alarmClock = new ExpirationTimer();
        try {
            FloatStatus time = new FloatStatus();
            alarmClock.schedule(99).send(FloatMixing.pumpEvent(Utils.currentTimeSeconds, time));
            float expected = Utils.getCurrentTimeSeconds() + 0.099f;
            assertFalse(alarmClock.isRunning(), "Alarm clock should not be running!");
            alarmClock.start();
            assertTrue(alarmClock.isRunning(), "Alarm clock should be running!");

            Thread.sleep(150);
            assertTrue(alarmClock.isRunning(), "Alarm clock should still be running!");
            assertTrue(Math.abs(time.get() - expected) < 0.01f, "Alarm clock did not ring properly!");

            Thread.sleep(150);
            assertTrue(alarmClock.isRunning(), "Alarm clock should still be running!");
            assertTrue(Math.abs(time.get() - expected) < 0.01f, "Alarm clock did not ring properly!");

            expected = Utils.getCurrentTimeSeconds() + 0.099f;
            alarmClock.feed();
            assertTrue(alarmClock.isRunning(), "Alarm clock should still be running!");

            Thread.sleep(150);
            assertTrue(alarmClock.isRunning(), "Alarm clock should still be running!");
            assertTrue(Math.abs(time.get() - expected) < 0.01f, "Alarm clock did not ring properly!");

            alarmClock.stop();
            assertFalse(alarmClock.isRunning(), "Alarm clock should have stopped!");
        } finally {
            alarmClock.terminate();
        }
    }

    private void testLightSequence() throws TestingException, InterruptedException {
        ExpirationTimer sequencer = new ExpirationTimer();
        try {
            BooleanStatus light = new BooleanStatus();
            sequencer.scheduleToggleSequence(light, true, 49, 99, 149, 199, 249, 299);
            BooleanStatus light2 = new BooleanStatus();
            sequencer.scheduleBooleanPeriod(99, 199, light2, true);

            Thread.sleep(25);

            assertFalse(light.get(), "Light should be off!");
            assertFalse(light2.get(), "Light 2 should be off!");
            sequencer.start();

            Thread.sleep(50);
            assertTrue(light.get(), "Light should be on!");
            assertFalse(light2.get(), "Light 2 should be off!");
            Thread.sleep(50);
            assertFalse(light.get(), "Light should be off!");
            assertTrue(light2.get(), "Light 2 should be on!");
            Thread.sleep(50);
            assertTrue(light.get(), "Light should be on!");
            assertTrue(light2.get(), "Light 2 should be on!");
            Thread.sleep(50);
            assertFalse(light.get(), "Light should be off!");
            assertFalse(light2.get(), "Light 2 should be off!");
            Thread.sleep(50);
            assertTrue(light.get(), "Light should be on!");
            assertFalse(light2.get(), "Light 2 should be off!");
            Thread.sleep(50);
            assertFalse(light.get(), "Light should be off!");
            assertFalse(light2.get(), "Light 2 should be off!");
            Thread.sleep(75);
            assertFalse(light.get(), "Light should still be off!");
            assertFalse(light2.get(), "Light 2 should still be off!");
        } finally {
            sequencer.terminate();
        }
    }

    private void testTunableClock() throws TestingException, InterruptedException {
        ExpirationTimer alarmClock = new ExpirationTimer();
        try {
            BooleanStatus ringingA = new BooleanStatus();
            BooleanStatus ringingB = new BooleanStatus();
            FloatStatus delay1 = new FloatStatus();
            FloatStatus delay2 = new FloatStatus();
            ringingA.setTrueWhen(alarmClock.schedule(delay1));
            ringingB.setTrueWhen(alarmClock.schedule(delay2));

            delay1.set(0.1f);
            delay2.set(0.2f);

            alarmClock.startOrFeed();

            ringingA.set(false);
            ringingB.set(false);

            Thread.sleep(10);

            delay1.set(0.2f);
            delay2.set(0.1f);

            Thread.sleep(40);
            assertFalse(ringingA.get(), "Alarm A should not be ringing.");
            assertFalse(ringingB.get(), "Alarm B should not be ringing.");
            Thread.sleep(100);
            assertTrue(ringingA.get(), "Alarm A should be ringing.");
            assertFalse(ringingB.get(), "Alarm B should not be ringing.");
            Thread.sleep(100);
            assertTrue(ringingA.get(), "Alarm A should be ringing.");
            assertTrue(ringingB.get(), "Alarm B should be ringing.");

            alarmClock.startOrFeed();

            ringingA.set(false);
            ringingB.set(false);
            Thread.sleep(50);
            assertFalse(ringingA.get(), "Alarm A should not be ringing.");
            assertFalse(ringingB.get(), "Alarm B should not be ringing.");
            Thread.sleep(100);
            assertFalse(ringingA.get(), "Alarm A should not be ringing.");
            assertTrue(ringingB.get(), "Alarm B should be ringing.");
            Thread.sleep(100);
            assertTrue(ringingA.get(), "Alarm A should be ringing.");
            assertTrue(ringingB.get(), "Alarm B should be ringing.");

            alarmClock.startOrFeed();

            ringingA.set(false);
            ringingB.set(false);
            assertFalse(ringingA.get(), "Alarm A should not be ringing.");
            assertFalse(ringingB.get(), "Alarm B should not be ringing.");

            alarmClock.stop();

            Thread.sleep(50);

            alarmClock.start();

            Thread.sleep(50);

            alarmClock.stop();

            Thread.sleep(200);

            assertFalse(ringingA.get(), "Alarm A should not be ringing.");
            assertFalse(ringingB.get(), "Alarm B should not be ringing.");
        } finally {
            alarmClock.terminate();
        }
    }
}
