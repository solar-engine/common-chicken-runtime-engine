package ccre.testing;

import ccre.channel.BooleanStatus;
import ccre.channel.EventStatus;
import ccre.channel.FloatStatus;
import ccre.ctrl.EventMixing;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.FloatMixing;
import ccre.util.Utils;

/**
 * Tests the ExpirationTimer class.
 * 
 * @author skeggsc
 */
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
        testWarningLight();
        testBadUsage();
        testSelfStopper();
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
            BooleanStatus light3 = new BooleanStatus(true);
            sequencer.scheduleBooleanPeriod(99, 199, light2, true);
            sequencer.scheduleBooleanPeriod(99, 199, light3, false);

            Thread.sleep(25);

            assertFalse(light.get(), "Light should be off!");
            assertFalse(light2.get(), "Light 2 should be off!");
            assertTrue(light3.get(), "Light 3 should be on!");
            sequencer.start();

            Thread.sleep(50);
            assertTrue(light.get(), "Light should be on!");
            assertFalse(light2.get(), "Light 2 should be off!");
            assertTrue(light3.get(), "Light 3 should be on!");
            Thread.sleep(50);
            assertFalse(light.get(), "Light should be off!");
            assertTrue(light2.get(), "Light 2 should be on!");
            assertFalse(light3.get(), "Light 3 should be off!");
            Thread.sleep(50);
            assertTrue(light.get(), "Light should be on!");
            assertTrue(light2.get(), "Light 2 should be on!");
            assertFalse(light3.get(), "Light 3 should be off!");
            Thread.sleep(50);
            assertFalse(light.get(), "Light should be off!");
            assertFalse(light2.get(), "Light 2 should be off!");
            assertTrue(light3.get(), "Light 3 should be on!");
            Thread.sleep(50);
            assertTrue(light.get(), "Light should be on!");
            assertFalse(light2.get(), "Light 2 should be off!");
            assertTrue(light3.get(), "Light 3 should be on!");
            Thread.sleep(50);
            assertFalse(light.get(), "Light should be off!");
            assertFalse(light2.get(), "Light 2 should be off!");
            assertTrue(light3.get(), "Light 3 should be on!");
            Thread.sleep(75);
            assertFalse(light.get(), "Light should still be off!");
            assertFalse(light2.get(), "Light 2 should still be off!");
            assertTrue(light3.get(), "Light 3 should still be on!");

            assertTrue(sequencer.isRunning(), "Should have continued running.");

            EventStatus status = new EventStatus();
            sequencer.stopWhen(status);

            assertTrue(sequencer.isRunning(), "Should have continued running.");

            status.produce();

            assertFalse(sequencer.isRunning(), "Should have stopped running.");
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

            alarmClock.getStartOrFeedEvent().event();

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

            EventStatus when = new EventStatus();
            alarmClock.startOrFeedWhen(when);
            when.event();

            ringingA.set(false);
            ringingB.set(false);
            assertFalse(ringingA.get(), "Alarm A should not be ringing.");
            assertFalse(ringingB.get(), "Alarm B should not be ringing.");

            alarmClock.getStopEvent().event();

            Thread.sleep(50);

            EventStatus status = new EventStatus();
            alarmClock.startWhen(status);
            status.produce();

            Thread.sleep(50);

            alarmClock.getStopEvent().event();

            Thread.sleep(200);

            assertFalse(ringingA.get(), "Alarm A should not be ringing.");
            assertFalse(ringingB.get(), "Alarm B should not be ringing.");
        } finally {
            alarmClock.terminate();
        }
    }

    private void testWarningLight() throws TestingException, InterruptedException {
        ExpirationTimer warningLight = new ExpirationTimer();
        try {
            BooleanStatus light = new BooleanStatus();
            warningLight.scheduleEnable(100, light);
            warningLight.scheduleDisable(200, light);

            warningLight.start();
            Thread.sleep(50);
            assertFalse(light.get(), "Light should be off!");
            Thread.sleep(100);
            assertTrue(light.get(), "Light should be on!");
            Thread.sleep(100);
            assertFalse(light.get(), "Light should be off!");
        } finally {
            warningLight.terminate();
        }
    }

    private void testBadUsage() throws TestingException {
        ExpirationTimer bad = new ExpirationTimer();
        try {
            EventStatus feeder = new EventStatus();
            bad.feedWhen(feeder);
            try {
                bad.stop();
                assertFail("Expected an exception from stop!");
            } catch (IllegalStateException ex) {
                // Correct!
            }
            bad.start();
            try {
                bad.start();
                assertFail("Expected an exception from start!");
            } catch (IllegalStateException ex) {
                // Correct!
            }
            // Can't schedule while already running!
            try {
                bad.schedule(100, EventMixing.ignored);
                assertFail("Expected an exception from schedule!");
            } catch (IllegalStateException ex) {
                // Correct!
            }
            try {
                bad.schedule(FloatMixing.always(1), EventMixing.ignored);
                assertFail("Expected an exception from schedule!");
            } catch (IllegalStateException ex) {
                // Correct!
            }
            bad.stop();
            try {
                bad.feed();
                assertFail("Expected an exception from feed!");
            } catch (IllegalStateException ex) {
                // Correct!
            }
            bad.startOrFeed();
            bad.feed();
            feeder.event();
            bad.startOrFeed();
            bad.stop();
            try {
                bad.stop();
                assertFail("Expected an exception from stop!");
            } catch (IllegalStateException ex) {
                // Correct!
            }
            bad.getStopEvent().event();
            assertFalse(bad.isRunning(), "Should still not be running!");
            bad.getFeedEvent().event();
            assertFalse(bad.isRunning(), "Should still not be running!");
            bad.getStartEvent().event();
            try {
                bad.start();
                assertFail("Expected an exception from start!");
            } catch (IllegalStateException ex) {
                // Correct!
            }
            assertTrue(bad.isRunning(), "Should still be running!");
            bad.getStartEvent().event();
            assertTrue(bad.isRunning(), "Should still be running!");
            bad.getFeedEvent().event();
            assertTrue(bad.isRunning(), "Should still be running!");
        } finally {
            bad.terminate();
        }
    }

    private void testSelfStopper() throws TestingException, InterruptedException {
        ExpirationTimer selfStopper = new ExpirationTimer();
        try {
            selfStopper.scheduleDisable(75, selfStopper.getRunningControl());
            selfStopper.getRunningControl().set(true);
            assertTrue(selfStopper.isRunning(), "Should be running!");
            assertObjectEqual(selfStopper.isRunning(), selfStopper.getRunningStatus().get(), "Expected equal running results.");
            Thread.sleep(50);
            assertTrue(selfStopper.isRunning(), "Should still be running!");
            assertObjectEqual(selfStopper.isRunning(), selfStopper.getRunningStatus().get(), "Expected equal running results.");
            selfStopper.getRunningControl().set(true);
            assertTrue(selfStopper.isRunning(), "Should still be running!");
            assertObjectEqual(selfStopper.isRunning(), selfStopper.getRunningStatus().get(), "Expected equal running results.");
            Thread.sleep(50);
            assertFalse(selfStopper.isRunning(), "Should have stopped running!");
            assertObjectEqual(selfStopper.isRunning(), selfStopper.getRunningStatus().get(), "Expected equal running results.");
            selfStopper.getRunningControl().set(false);
            assertFalse(selfStopper.isRunning(), "Should still not be running!");
            assertObjectEqual(selfStopper.isRunning(), selfStopper.getRunningStatus().get(), "Expected equal running results.");
        } finally {
            selfStopper.terminate();
        }
    }
}
