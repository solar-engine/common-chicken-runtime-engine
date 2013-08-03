package ccre.testing;

import ccre.event.Event;
import ccre.event.EventConsumer;

/**
 * A test that tests some parts of the Event class.
 *
 * @author skeggsc
 */
public final class TestEvent extends BaseTest {

    @Override
    public String getName() {
        return "Event Testing";
    }
    private int eventCalled = -42;

    @Override
    protected void runTest() throws TestingException {
        Event event = new Event();
        eventCalled = 0;
        EventConsumer evt = new EventConsumer() {
            public void eventFired() {
                eventCalled++;
            }
        };
        assertTrue(event.addListener(evt), "Event did not add properly!");
        assertEqual(eventCalled, 0, "Event fired too soon!");
        event.produce();
        assertEqual(eventCalled, 1, "Event did not fire properly!");
        event.removeListener(evt);
        event.produce();
        assertEqual(eventCalled, 1, "Event did not remove properly!");
    }
}
