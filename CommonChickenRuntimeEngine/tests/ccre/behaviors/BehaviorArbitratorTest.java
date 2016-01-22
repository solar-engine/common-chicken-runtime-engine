/*
 * Copyright 2016 Colby Skeggs
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
package ccre.behaviors;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.FloatCell;
import ccre.channel.FloatInput;
import ccre.rconf.RConf;
import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.testing.CountingFloatOutput;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class BehaviorArbitratorTest {

    private static final String NAME = "Example Arbitrator";
    private static final String NAME_BEHAVIOR = "Example Behavior";
    private static final String NAME_BEHAVIOR_ALT = "Example Behavior Alternate";
    private static final String NAME_BEHAVIOR_ALT_2 = "Example Behavior Alternate 2";
    private BehaviorArbitrator arbitrator;

    @Before
    public void setUp() throws Exception {
        this.arbitrator = new BehaviorArbitrator(NAME);
    }

    @After
    public void tearDown() throws Exception {
        arbitrator = null;
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, arbitrator.getName());
    }

    @Test
    public void testToString() {
        assertEquals("[BehaviorArbitrator " + NAME + "]", arbitrator.toString());
    }

    @Test
    public void testOneBehavior() {
        BooleanCell request = new BooleanCell();
        Behavior behavior = arbitrator.addBehavior(NAME_BEHAVIOR, request);
        CountingBooleanOutput inactive = new CountingBooleanOutput();
        CountingBooleanOutput active = new CountingBooleanOutput();

        inactive.ifExpected = true;
        inactive.valueExpected = true;
        arbitrator.getIsInactive().send(inactive);
        inactive.check();

        active.ifExpected = true;
        active.valueExpected = false;
        arbitrator.getIsActive(behavior).send(active);
        active.check();

        for (int i = 0; i < 10; i++) {
            inactive.ifExpected = active.ifExpected = true;
            inactive.valueExpected = !(active.valueExpected = !request.get());
            request.toggle();
            active.check();
            inactive.check();
        }
    }

    @Test
    public void testTwoBehaviors() {
        BooleanCell requestL = new BooleanCell(), requestH = new BooleanCell();
        Behavior behaviorL = arbitrator.addBehavior(NAME_BEHAVIOR, requestL);
        Behavior behaviorH = arbitrator.addBehavior(NAME_BEHAVIOR_ALT, requestH);
        CountingBooleanOutput cL = new CountingBooleanOutput(), cH = new CountingBooleanOutput(), cI = new CountingBooleanOutput();
        cL.ifExpected = true;
        cL.valueExpected = false;
        arbitrator.getIsActive(behaviorL).send(cL);
        cL.check();
        cH.ifExpected = true;
        cH.valueExpected = false;
        arbitrator.getIsActive(behaviorH).send(cH);
        cH.check();
        cI.ifExpected = true;
        cI.valueExpected = true;
        arbitrator.getIsInactive().send(cI);
        cI.check();

        for (int i = 0; i < 100; i++) {
            if (Values.getRandomBoolean()) {
                if (!requestH.get()) { // if H is true, nothing should change
                    cI.ifExpected = cL.ifExpected = true;
                    cL.valueExpected = !(cI.valueExpected = requestL.get());
                }
                requestL.toggle();
            } else {
                if (!requestH.get()) {
                    cH.ifExpected = true;
                    cH.valueExpected = true;
                    if (requestL.get()) {
                        cL.ifExpected = true;
                        cL.valueExpected = false;
                    } else {
                        cI.ifExpected = true;
                        cI.valueExpected = false;
                    }
                } else {
                    cH.ifExpected = true;
                    cH.valueExpected = false;
                    if (requestL.get()) {
                        cL.ifExpected = true;
                        cL.valueExpected = true;
                    } else {
                        cI.ifExpected = true;
                        cI.valueExpected = true;
                    }
                }
                requestH.toggle();
            }
            cH.check();
            cI.check();
            cL.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAddBehaviorNullA() {
        arbitrator.addBehavior(null, BooleanInput.alwaysTrue);
    }

    @Test(expected = NullPointerException.class)
    public void testAddBehaviorNullB() {
        arbitrator.addBehavior(NAME_BEHAVIOR, null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddGetIsActiveNull() {
        arbitrator.getIsActive(null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddEventNull() {
        arbitrator.addEvent(null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddBooleanNull() {
        arbitrator.addBoolean(null);
    }

    @Test(expected = NullPointerException.class)
    public void testAddFloatNull() {
        arbitrator.addFloat(null);
    }

    @Test
    public void testAddEventIBasic() {
        EventCell base = new EventCell();
        ArbitratedEvent event = arbitrator.addEvent(base);
        CountingEventOutput ceo = new CountingEventOutput();
        event.send(ceo);
        for (int i = 0; i < 10; i++) {
            ceo.ifExpected = true;
            base.event();
            ceo.check();
        }
    }

    @Test
    public void testAddEventIAdvanced() {
        BooleanCell rqL = new BooleanCell(), rqM = new BooleanCell(), rqH = new BooleanCell();
        Behavior behaviorL = arbitrator.addBehavior(NAME_BEHAVIOR, rqL);
        /* Behavior behaviorM = */ arbitrator.addBehavior(NAME_BEHAVIOR_ALT, rqM);
        Behavior behaviorH = arbitrator.addBehavior(NAME_BEHAVIOR_ALT_2, rqH);

        EventCell base = new EventCell(), extraL = new EventCell(), extraH = new EventCell();
        ArbitratedEvent arb = arbitrator.addEvent(base);
        arb.attach(behaviorL, extraL);
        // and nothing on behaviorM
        arb.attach(behaviorH, extraH);
        CountingEventOutput ceo = new CountingEventOutput();
        arb.send(ceo);

        for (int i = 0; i < 100; i++) {
            boolean l = Values.getRandomBoolean(), m = Values.getRandomBoolean(), h = Values.getRandomBoolean();
            rqL.set(l);
            rqM.set(m);
            rqH.set(h);
            for (int j = 0; j < 10; j++) {
                if (h) {
                    base.event();
                    extraL.event();
                    ceo.ifExpected = true;
                    extraH.event();
                    ceo.check();
                } else if (m) {
                    // same as the inactive case, since we didn't specify
                    // anything
                    extraL.event();
                    extraH.event();
                    ceo.ifExpected = true;
                    base.event();
                    ceo.check();
                } else if (l) {
                    base.event();
                    extraH.event();
                    ceo.ifExpected = true;
                    extraL.event();
                    ceo.check();
                } else {
                    extraL.event();
                    extraH.event();
                    ceo.ifExpected = true;
                    base.event();
                    ceo.check();
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testArbitratedEventAttachNullA() {
        arbitrator.addEvent().attach(null, EventInput.never);
    }

    @Test(expected = NullPointerException.class)
    public void testArbitratedEventAttachNullB() {
        arbitrator.addEvent().attach(arbitrator.addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArbitratedEventAttachWrongChain() {
        arbitrator.addEvent().attach(new BehaviorArbitrator(NAME).addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse), EventInput.never);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArbitratedEventAttachDouble() {
        Behavior behavior = arbitrator.addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse);
        ArbitratedEvent event = arbitrator.addEvent();
        event.attach(behavior, new EventCell());
        event.attach(behavior, new EventCell());
    }

    @Test
    public void testAddBooleanIBasic() {
        for (boolean b0 : new boolean[] { false, true }) {
            BooleanCell base = new BooleanCell(b0);
            ArbitratedBoolean bool = arbitrator.addBoolean(base);
            CountingBooleanOutput cbo = new CountingBooleanOutput();
            cbo.ifExpected = true;
            cbo.valueExpected = b0;
            bool.send(cbo);
            cbo.check();
            for (int i = 0; i < 10; i++) {
                cbo.valueExpected = !cbo.valueExpected;
                cbo.ifExpected = true;
                base.set(cbo.valueExpected);
                cbo.check();
            }
        }
    }

    @Test
    public void testAddBooleanIAdvanced() {
        BooleanCell rqL = new BooleanCell(), rqM = new BooleanCell(), rqH = new BooleanCell();
        Behavior behaviorL = arbitrator.addBehavior(NAME_BEHAVIOR, rqL);
        /* Behavior behaviorM = */ arbitrator.addBehavior(NAME_BEHAVIOR_ALT, rqM);
        Behavior behaviorH = arbitrator.addBehavior(NAME_BEHAVIOR_ALT_2, rqH);

        BooleanCell base = new BooleanCell(), extraL = new BooleanCell(), extraH = new BooleanCell();
        ArbitratedBoolean arb = arbitrator.addBoolean(base);
        arb.attach(behaviorL, extraL);
        // and nothing on behaviorM
        arb.attach(behaviorH, extraH);
        BooleanCell out = new BooleanCell();
        arb.send(out);

        for (int i = 0; i < 100; i++) {
            boolean l = Values.getRandomBoolean(), m = Values.getRandomBoolean(), h = Values.getRandomBoolean();
            rqL.set(l);
            rqM.set(m);
            rqH.set(h);
            for (int j = 0; j < 30; j++) {
                base.set(Values.getRandomBoolean());
                extraL.set(Values.getRandomBoolean());
                extraH.set(Values.getRandomBoolean());
                if (h) {
                    assertEquals(extraH.get(), out.get());
                } else if (m) { // nothing attached on behaviorM, so fall back
                                // to base
                    assertEquals(base.get(), out.get());
                } else if (l) {
                    assertEquals(extraL.get(), out.get());
                } else {
                    assertEquals(base.get(), out.get());
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testArbitratedBooleanAttachNullA() {
        arbitrator.addBoolean().attach(null, BooleanInput.alwaysFalse);
    }

    @Test(expected = NullPointerException.class)
    public void testArbitratedBooleanAttachNullB() {
        arbitrator.addBoolean().attach(arbitrator.addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArbitratedBooleanAttachWrongChain() {
        arbitrator.addBoolean().attach(new BehaviorArbitrator(NAME).addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse), BooleanInput.alwaysFalse);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArbitratedBooleanAttachDouble() {
        Behavior behavior = arbitrator.addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse);
        ArbitratedBoolean event = arbitrator.addBoolean();
        event.attach(behavior, new BooleanCell());
        event.attach(behavior, new BooleanCell());
    }

    @Test
    public void testAddFloatIBasic() {
        for (float f0 : Values.interestingFloats) {
            FloatCell base = new FloatCell(f0);
            ArbitratedFloat flo = arbitrator.addFloat(base);
            CountingFloatOutput cfo = new CountingFloatOutput();
            cfo.ifExpected = true;
            cfo.valueExpected = f0;
            flo.send(cfo);
            cfo.check();
            for (float f1 : Values.interestingFloats) {
                if (f1 == f0) {
                    continue;
                }
                cfo.valueExpected = f1;
                cfo.ifExpected = true;
                base.set(cfo.valueExpected);
                cfo.check();
            }
        }
    }

    @Test
    public void testAddFloatIAdvanced() {
        BooleanCell rqL = new BooleanCell(), rqM = new BooleanCell(), rqH = new BooleanCell();
        Behavior behaviorL = arbitrator.addBehavior(NAME_BEHAVIOR, rqL);
        /* Behavior behaviorM = */ arbitrator.addBehavior(NAME_BEHAVIOR_ALT, rqM);
        Behavior behaviorH = arbitrator.addBehavior(NAME_BEHAVIOR_ALT_2, rqH);

        FloatCell base = new FloatCell(), extraL = new FloatCell(), extraH = new FloatCell();
        ArbitratedFloat arb = arbitrator.addFloat(base);
        arb.attach(behaviorL, extraL);
        // and nothing on behaviorM
        arb.attach(behaviorH, extraH);
        FloatCell out = new FloatCell();
        arb.send(out);

        for (int i = 0; i < 100; i++) {
            boolean l = Values.getRandomBoolean(), m = Values.getRandomBoolean(), h = Values.getRandomBoolean();
            rqL.set(l);
            rqM.set(m);
            rqH.set(h);
            for (int j = 0; j < 30; j++) {
                base.set(Values.getRandomFloat());
                extraL.set(Values.getRandomFloat());
                extraH.set(Values.getRandomFloat());
                if (h) {
                    assertEquals(Float.floatToIntBits(extraH.get()), Float.floatToIntBits(out.get()));
                } else if (m) { // nothing attached on behaviorM, so fall back
                                // to base
                    assertEquals(Float.floatToIntBits(base.get()), Float.floatToIntBits(out.get()));
                } else if (l) {
                    assertEquals(Float.floatToIntBits(extraL.get()), Float.floatToIntBits(out.get()));
                } else {
                    assertEquals(Float.floatToIntBits(base.get()), Float.floatToIntBits(out.get()));
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testArbitratedFloatAttachNullA() {
        arbitrator.addFloat().attach(null, FloatInput.zero);
    }

    @Test(expected = NullPointerException.class)
    public void testArbitratedFloatAttachNullB() {
        arbitrator.addFloat().attach(arbitrator.addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArbitratedFloatAttachWrongChain() {
        arbitrator.addFloat().attach(new BehaviorArbitrator(NAME).addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse), FloatInput.zero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArbitratedFloatAttachDouble() {
        Behavior behavior = arbitrator.addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysFalse);
        ArbitratedFloat event = arbitrator.addFloat();
        event.attach(behavior, new FloatCell());
        event.attach(behavior, new FloatCell());
    }

    @Test
    public void testSignalRConf() {
        arbitrator.addBehavior(NAME_BEHAVIOR, BooleanInput.alwaysTrue);
        arbitrator.addBehavior(NAME_BEHAVIOR_ALT, BooleanInput.alwaysTrue);
        arbitrator.addBehavior(NAME_BEHAVIOR_ALT_2, BooleanInput.alwaysTrue);
        int n = arbitrator.queryRConf().length;
        for (int i = 0; i < n; i++) {
            // this will have actual testing once we have SOMETHING being
            // available over signalRConf
            assertFalse(arbitrator.signalRConf(i, new byte[0]));
        }
    }

    @Test
    public void testQueryRConf() {
        for (int length = 0; length < 8; length++) {
            BehaviorArbitrator arbitrator = new BehaviorArbitrator(NAME);
            BooleanCell[] cells = new BooleanCell[length];
            for (int i = 0; i < length; i++) {
                cells[i] = new BooleanCell();
                arbitrator.addBehavior(NAME_BEHAVIOR + i, cells[i]);
            }
            for (int j = 0; j < 30; j++) {
                int active_id = -1;
                for (int i = 0; i < length; i++) {
                    boolean b = Values.getRandomBoolean();
                    cells[i].set(b);
                    if (b) {
                        active_id = i;
                    }
                }
                RConf.Entry[] entries = arbitrator.queryRConf();
                assertEquals(3 + length, entries.length);
                assertEquals(RConf.F_TITLE, entries[0].type);
                assertEquals("Behaviors for " + NAME, entries[0].parseTextual());
                for (int enti = 1; enti < length + 1; enti++) {
                    int i = enti - 1;
                    assertEquals(RConf.F_STRING, entries[enti].type);
                    String name = entries[enti].parseTextual();
                    String[] parts = name.split(": ", 2);
                    assertEquals(8, parts[0].length());
                    String prefix = parts[0].trim();
                    if (!cells[i].get()) {
                        assertEquals("Inactive", prefix);
                    } else if (i == active_id) {
                        assertEquals("Active", prefix);
                    } else {
                        assertEquals("Standby", prefix);
                    }
                    assertEquals(NAME_BEHAVIOR + i, parts[1]);
                }
                assertEquals(RConf.F_STRING, entries[length + 1].type);
                assertEquals(active_id == -1 ? "No active behavior" : "Active behavior", entries[length + 1].parseTextual());
                assertEquals(RConf.F_AUTO_REFRESH, entries[length + 2].type);
                assertEquals(1000, entries[length + 2].parseInteger().intValue());
            }
        }
    }
}
