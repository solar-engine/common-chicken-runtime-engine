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
package ccre.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.util.Values;

public class BooleanInputTest {

    private CountingEventOutput expected, expected2;

    private EventStatus es = null;
    private BooleanInput bi = null;
    private boolean result;// temporary variable

    @Before
    public void setUp() throws Exception {
        expected = new CountingEventOutput();
        expected2 = new CountingEventOutput();
        es = new EventStatus();
        bi = new BooleanInput() {
            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                return es.onUpdateR(notify);
            }

            @Override
            public boolean get() {
                return result;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        bi = null;
        es = null;
        expected = null;
        expected2 = null;
    }

    @Test
    public void testAlwaysBoolean() {
        assertFalse(BooleanInput.alwaysFalse.get());
        assertTrue(BooleanInput.alwaysTrue.get());
        CountingBooleanOutput cbo = new CountingBooleanOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = false;
        BooleanInput.alwaysFalse.send(cbo);
        cbo.check();
        cbo = new CountingBooleanOutput();
        cbo.ifExpected = true;
        cbo.valueExpected = true;
        BooleanInput.alwaysTrue.send(cbo);
        cbo.check();
    }

    @Test
    public void testAlways() {
        assertEquals(BooleanInput.alwaysFalse, BooleanInput.always(false));
        assertEquals(BooleanInput.alwaysTrue, BooleanInput.always(true));
    }

    private boolean gotProperly;

    @Test
    public void testSend() {
        final CountingBooleanOutput cbo = new CountingBooleanOutput();
        gotProperly = false;
        bi = new BooleanInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                assertFalse(gotProperly);
                cbo.check();// the earlier setup from outside this
                for (boolean b : new boolean[] { false, true }) {
                    result = b;
                    cbo.ifExpected = true;
                    cbo.valueExpected = b;
                    notify.event();
                    cbo.check();
                }
                gotProperly = true;
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                fail();
                return null;
            }

            @Override
            public boolean get() {
                return result;
            }
        };
        for (boolean b : new boolean[] { false, true }) {
            result = b;
            assertEquals(b, bi.get());
        }
        for (boolean initial : new boolean[] { false, true }) {
            cbo.ifExpected = true;
            cbo.valueExpected = result = initial;
            assertFalse(gotProperly);
            bi.send(cbo);
            assertTrue(gotProperly);
            gotProperly = false;
            cbo.check();// the real check is in onUpdateR above
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSendNull() {
        bi.send(null);
    }

    @Test
    public void testSendR() {
        final CountingBooleanOutput cbo = new CountingBooleanOutput();
        gotProperly = false;
        bi = new BooleanInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                fail();
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                assertFalse(gotProperly);
                cbo.check();// the earlier setup from outside this
                for (boolean b : new boolean[] { false, true }) {
                    result = b;
                    cbo.ifExpected = true;
                    cbo.valueExpected = b;
                    notify.event();
                    cbo.check();
                }
                gotProperly = true;
                return expected;
            }

            @Override
            public boolean get() {
                return result;
            }
        };
        for (boolean b : new boolean[] { false, true }) {
            result = b;
            assertEquals(b, bi.get());
        }
        for (boolean initial : new boolean[] { false, true }) {
            cbo.ifExpected = true;
            cbo.valueExpected = result = initial;
            assertFalse(gotProperly);
            assertEquals(expected, bi.sendR(cbo));
            assertTrue(gotProperly);
            gotProperly = false;
            cbo.check();// the real check is in onUpdateR above
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSendRNull() {
        bi.sendR(null);
    }

    @Test
    public void testNotRule1() {
        bi = new BooleanInput() {
            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                fail();
                return null;
            }

            @Override
            public boolean get() {
                return result;
            }
        };
        BooleanInput bi2 = bi.not();
        result = false;// no update fired because not() is required to work
                       // without that.
        assertTrue(bi2.get());
        result = true;
        assertFalse(bi2.get());
    }

    private boolean gotProperlyB;

    @Test
    public void testNotRule2() {
        bi = new BooleanInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                assertFalse(gotProperly);
                assertEquals(expected2, notify);
                gotProperly = true;
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                assertFalse(gotProperlyB);
                assertEquals(expected, notify);
                gotProperlyB = true;
                return expected2;
            }

            @Override
            public boolean get() {
                fail();
                return false;
            }
        };
        BooleanInput bi2 = bi.not();
        gotProperly = gotProperlyB = false;
        bi2.onUpdate(expected2);
        assertTrue(gotProperly);
        assertFalse(gotProperlyB);
        gotProperly = false;
        assertEquals(expected2, bi2.onUpdateR(expected));
        assertFalse(gotProperly);
        assertTrue(gotProperlyB);
    }

    @Test
    public void testNotNot() {
        bi = new BooleanInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                assertFalse(gotProperly);
                assertEquals(expected2, notify);
                gotProperly = true;
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                assertFalse(gotProperlyB);
                assertEquals(expected, notify);
                gotProperlyB = true;
                return expected2;
            }

            @Override
            public boolean get() {
                fail();
                return false;
            }
        };
        assertEquals(bi, bi.not().not());
    }

    @Test
    public void testAnd() {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus();
        BooleanStatus abi = new BooleanStatus(), bai = new BooleanStatus();
        BooleanInput ab = alpha.and(beta), ba = beta.and(alpha);
        ab.send(abi);
        ba.send(bai);
        for (boolean a : Values.interestingBooleans) {
            for (boolean b : Values.interestingBooleans) {
                alpha.set(a);
                beta.set(b);
                assertEquals(a && b, ab.get());
                assertEquals(a && b, ba.get());
                assertEquals(a && b, abi.get());
                assertEquals(a && b, bai.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAndNull() {
        bi.and(null);
    }

    @Test
    public void testAndNot() {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus();
        BooleanStatus abi = new BooleanStatus(), bai = new BooleanStatus();
        BooleanInput ab = alpha.andNot(beta), ba = beta.andNot(alpha);
        ab.send(abi);
        ba.send(bai);
        for (boolean a : Values.interestingBooleans) {
            for (boolean b : Values.interestingBooleans) {
                alpha.set(a);
                beta.set(b);
                assertEquals(a && !b, ab.get());
                assertEquals(!a && b, ba.get());
                assertEquals(a && !b, abi.get());
                assertEquals(!a && b, bai.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAndNotNull() {
        bi.andNot(null);
    }

    @Test
    public void testXor() {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus();
        BooleanStatus abi = new BooleanStatus(), bai = new BooleanStatus();
        BooleanInput ab = alpha.xor(beta), ba = beta.xor(alpha);
        ab.send(abi);
        ba.send(bai);
        for (boolean a : Values.interestingBooleans) {
            for (boolean b : Values.interestingBooleans) {
                alpha.set(a);
                beta.set(b);
                assertEquals(a ^ b, ab.get());
                assertEquals(a ^ b, ba.get());
                assertEquals(a ^ b, abi.get());
                assertEquals(a ^ b, bai.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testXorNull() {
        bi.xor(null);
    }

    @Test
    public void testOr() {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus();
        BooleanStatus abi = new BooleanStatus(), bai = new BooleanStatus();
        BooleanInput ab = alpha.or(beta), ba = beta.or(alpha);
        ab.send(abi);
        ba.send(bai);
        for (boolean a : Values.interestingBooleans) {
            for (boolean b : Values.interestingBooleans) {
                alpha.set(a);
                beta.set(b);
                assertEquals(a || b, ab.get());
                assertEquals(a || b, ba.get());
                assertEquals(a || b, abi.get());
                assertEquals(a || b, bai.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOrNull() {
        bi.or(null);
    }

    @Test
    public void testOrNot() {
        BooleanStatus alpha = new BooleanStatus(), beta = new BooleanStatus();
        BooleanStatus abi = new BooleanStatus(), bai = new BooleanStatus();
        BooleanInput ab = alpha.orNot(beta), ba = beta.orNot(alpha);
        ab.send(abi);
        ba.send(bai);
        for (boolean a : Values.interestingBooleans) {
            for (boolean b : Values.interestingBooleans) {
                alpha.set(a);
                beta.set(b);
                assertEquals(a || !b, ab.get());
                assertEquals(!a || b, ba.get());
                assertEquals(a || !b, abi.get());
                assertEquals(!a || b, bai.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOrNotNull() {
        bi.orNot(null);
    }

    @Test
    public void testOnPress() {
        for (boolean b : new boolean[] { false, true }) {
            BooleanStatus a = new BooleanStatus(b);
            CountingEventOutput ceo = new CountingEventOutput();
            a.onPress().send(ceo);
            for (int i = 0; i < 5; i++) {
                a.set(false);
                ceo.ifExpected = true;
                a.set(true);
                ceo.check();
            }
        }
    }

    @Test
    public void testOnPressNoRepeat() {
        CountingEventOutput ceo = new CountingEventOutput();
        bi.onPress().send(ceo);

        for (int i = 0; i < 10; i++) {
            result = true;

            ceo.ifExpected = true;
            es.event();
            ceo.check();

            result = false;
            ceo.check();

            result = true;

            ceo.ifExpected = true;
            es.event();
            ceo.check();

            result = false;
            es.event();
            ceo.check();
        }
    }

    @Test
    public void testOnRelease() {
        for (boolean b : new boolean[] { false, true }) {
            BooleanStatus a = new BooleanStatus(b);
            CountingEventOutput ceo = new CountingEventOutput();
            a.onRelease().send(ceo);
            for (int i = 0; i < 5; i++) {
                a.set(true);
                ceo.ifExpected = true;
                a.set(false);
                ceo.check();
            }
        }
    }

    @Test
    public void testOnReleaseNoRepeat() {
        CountingEventOutput ceo = new CountingEventOutput();
        bi.onRelease().send(ceo);

        for (int i = 0; i < 10; i++) {
            result = false;

            ceo.ifExpected = true;
            es.event();
            ceo.check();

            result = true;
            ceo.check();

            result = false;

            ceo.ifExpected = true;
            es.event();
            ceo.check();

            result = true;
            es.event();
            ceo.check();
        }
    }

    @Test
    public void testOnChange() {
        CountingEventOutput ceo = new CountingEventOutput();
        bi.onChange().send(ceo);

        for (int i = 0; i < 10; i++) {
            result = true;

            ceo.ifExpected = true;
            es.event();
            ceo.check();

            result = false;

            ceo.ifExpected = true;
            es.event();
            ceo.check();
        }
    }

    @Test
    public void testFilterUpdates() {
        for (boolean not : new boolean[] { false, true }) {
            BooleanStatus a = new BooleanStatus(), allowDeny = new BooleanStatus(true);
            BooleanStatus out = new BooleanStatus();
            bi = not ? a.filterUpdatesNot(allowDeny) : a.filterUpdates(allowDeny);
            bi.send(out);
            boolean expect = false;
            for (int i = 0; i < 110; i++) {
                allowDeny.set(((i % 19) < 10) ^ not);
                a.toggle();
                if (allowDeny.get() ^ not) {
                    expect = a.get();
                }
                assertEquals(expect, bi.get());
                assertEquals(expect, out.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFilterUpdatesNull() {
        bi.filterUpdates(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFilterUpdatesNotNull() {
        bi.filterUpdatesNot(null);
    }

    @Test
    public void testToFloatFloatFloat() {
        BooleanStatus bs = new BooleanStatus();
        FloatStatus out = new FloatStatus();
        FloatInput fi = bs.toFloat(6.4f, 3.2f);
        fi.send(out);
        assertEquals(6.4f, fi.get(), 0);
        assertEquals(6.4f, out.get(), 0);
        for (int i = 0; i < 30; i++) {
            bs.toggle();
            assertEquals(bs.get() ? 3.2f : 6.4f, fi.get(), 0);
            assertEquals(bs.get() ? 3.2f : 6.4f, out.get(), 0);
        }
    }

    @Test
    public void testToFloatFloatFloatInput() {
        BooleanStatus bs = new BooleanStatus();
        FloatStatus out = new FloatStatus(), tru = new FloatStatus();
        FloatInput fi = bs.toFloat(6.4f, tru);
        fi.send(out);
        assertEquals(6.4f, fi.get(), 0);
        assertEquals(6.4f, out.get(), 0);
        for (int i = 0; i < 30; i++) {
            tru.set(17.3f);
            bs.toggle();
            assertEquals(bs.get() ? 17.3f : 6.4f, fi.get(), 0);
            assertEquals(bs.get() ? 17.3f : 6.4f, out.get(), 0);
            for (float f : Values.interestingFloats) {
                tru.set(f);
                assertEquals(bs.get() ? f : 6.4f, fi.get(), 0);
                assertEquals(bs.get() ? f : 6.4f, out.get(), 0);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToFloatFloatFloatInputNull() {
        bi.toFloat(0, null);
    }

    @Test
    public void testToFloatFloatInputFloat() {
        BooleanStatus bs = new BooleanStatus();
        FloatStatus out = new FloatStatus(), fals = new FloatStatus(6.4f);
        FloatInput fi = bs.toFloat(fals, 3.2f);
        fi.send(out);
        assertEquals(6.4f, fi.get(), 0);
        assertEquals(6.4f, out.get(), 0);
        for (int i = 0; i < 30; i++) {
            fals.set(-17.3f);
            bs.toggle();
            assertEquals(bs.get() ? 3.2f : -17.3f, fi.get(), 0);
            assertEquals(bs.get() ? 3.2f : -17.3f, out.get(), 0);
            for (float f : Values.interestingFloats) {
                fals.set(f);
                assertEquals(bs.get() ? 3.2f : f, fi.get(), 0);
                assertEquals(bs.get() ? 3.2f : f, out.get(), 0);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToFloatFloatInputFloatNull() {
        bi.toFloat(null, 0);
    }

    @Test
    public void testToFloatFloatInputFloatInput() {
        BooleanStatus bs = new BooleanStatus();
        FloatStatus out = new FloatStatus(), tru = new FloatStatus(), fals = new FloatStatus(6.4f);
        FloatInput fi = bs.toFloat(fals, tru);
        fi.send(out);
        assertEquals(6.4f, fi.get(), 0);
        assertEquals(6.4f, out.get(), 0);
        for (int i = 0; i < 30; i++) {
            fals.set(13.2f);
            tru.set(-13.2f);
            bs.toggle();
            assertEquals(bs.get() ? tru.get() : fals.get(), fi.get(), 0);
            assertEquals(bs.get() ? tru.get() : fals.get(), out.get(), 0);
            FloatStatus var = i % 2 == 0 ? tru : fals;
            for (float f : Values.interestingFloats) {
                var.set(f);// switch which one we vary each time outside
                assertEquals(bs.get() ? tru.get() : fals.get(), fi.get(), 0);
                assertEquals(bs.get() ? tru.get() : fals.get(), out.get(), 0);
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testToFloatFloatInputFloatInputNull() {
        bi.toFloat(null, null);
    }

    @Test
    public void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        bi = new BooleanInput() {
            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                assertFalse(gotProperly);
                assertEquals(notify, ceo);
                gotProperly = true;
                return null;
            }

            @Override
            public boolean get() {
                fail();
                return false;
            }
        };
        gotProperly = false;
        bi.onUpdate(ceo);
        assertTrue(gotProperly);
    }
}
