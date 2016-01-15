/*
 * Copyright 2015-2016 Colby Skeggs
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

import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.log.LogLevel;
import ccre.log.VerifyingLogger;
import ccre.testing.CountingBooleanOutput;
import ccre.testing.CountingEventOutput;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class BooleanInputTest {

    private static final String ERROR_STRING = "Purposeful failure.";

    private CountingEventOutput expected, expected2;

    private EventCell es = null;
    private BooleanInput bi = null;
    private boolean result;// temporary variable

    @Before
    public void setUp() throws Exception {
        expected = new CountingEventOutput();
        expected2 = new CountingEventOutput();
        es = new EventCell();
        bi = new BooleanInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                return es.onUpdate(notify);
            }

            @Override
            public boolean get() {
                return result;
            }
        };
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() throws Exception {
        VerifyingLogger.checkAndEnd();
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
        CancelOutput cex = expected::event;
        bi = new BooleanInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
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
                return cex;
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
            assertEquals(cex, bi.send(cbo));
            assertTrue(gotProperly);
            gotProperly = false;
            cbo.check(); // the real check is in onUpdate above
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSendNull() {
        bi.send(null);
    }

    @Test
    public void testNotRule1() {
        bi = new BooleanInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                fail();
                return null;
            }

            @Override
            public boolean get() {
                return result;
            }
        };
        BooleanInput bi2 = bi.not();
        // no update fired because not() is required to work without that.
        result = false;
        assertTrue(bi2.get());
        result = true;
        assertFalse(bi2.get());
    }

    @Test
    public void testNotRule2() {
        CancelOutput cex2 = expected2::event;
        bi = new BooleanInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                assertFalse(gotProperly);
                assertEquals(expected, notify);
                gotProperly = true;
                return cex2;
            }

            @Override
            public boolean get() {
                fail();
                return false;
            }
        };
        BooleanInput bi2 = bi.not();
        gotProperly = false;
        bi2.onUpdate(expected);
        assertTrue(gotProperly);
        gotProperly = false;
        assertEquals(cex2, bi2.onUpdate(expected));
        assertTrue(gotProperly);
    }

    @Test
    public void testNotNot() {
        bi = new BooleanInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                assertFalse(gotProperly);
                assertEquals(expected, notify);
                gotProperly = true;
                return expected2::event;
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
        BooleanCell alpha = new BooleanCell(), beta = new BooleanCell();
        BooleanCell abi = new BooleanCell(), bai = new BooleanCell();
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
        BooleanCell alpha = new BooleanCell(), beta = new BooleanCell();
        BooleanCell abi = new BooleanCell(), bai = new BooleanCell();
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
        BooleanCell alpha = new BooleanCell(), beta = new BooleanCell();
        BooleanCell abi = new BooleanCell(), bai = new BooleanCell();
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
        BooleanCell alpha = new BooleanCell(), beta = new BooleanCell();
        BooleanCell abi = new BooleanCell(), bai = new BooleanCell();
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
        BooleanCell alpha = new BooleanCell(), beta = new BooleanCell();
        BooleanCell abi = new BooleanCell(), bai = new BooleanCell();
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
            BooleanCell a = new BooleanCell(b);
            CountingEventOutput ceo = new CountingEventOutput();
            a.onPress(ceo);
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
        bi.onPress(ceo);

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
            BooleanCell a = new BooleanCell(b);
            CountingEventOutput ceo = new CountingEventOutput();
            a.onRelease(ceo);
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
        bi.onRelease(ceo);

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
        bi.onChange(ceo);

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
            BooleanCell a = new BooleanCell(), allowDeny = new BooleanCell(true);
            BooleanCell out = new BooleanCell();
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
        BooleanCell bs = new BooleanCell();
        FloatCell out = new FloatCell();
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
        BooleanCell bs = new BooleanCell();
        FloatCell out = new FloatCell(), tru = new FloatCell();
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
        BooleanCell bs = new BooleanCell();
        FloatCell out = new FloatCell(), fals = new FloatCell(6.4f);
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
        BooleanCell bs = new BooleanCell();
        FloatCell out = new FloatCell(), tru = new FloatCell(), fals = new FloatCell(6.4f);
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
            FloatCell var = i % 2 == 0 ? tru : fals;
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
    public void testSelectBooleanBoolean() {
        for (boolean def1 : Values.interestingBooleans) {
            for (boolean def2 : Values.interestingBooleans) {
                BooleanCell bs = new BooleanCell();
                BooleanCell out = new BooleanCell();
                BooleanInput bi = bs.select(def2, def1);
                bi.send(out);
                assertEquals(def2, bi.get());
                assertEquals(def2, out.get());
                for (int i = 0; i < 30; i++) {
                    bs.toggle();
                    assertEquals(bs.get() ? def1 : def2, bi.get());
                    assertEquals(bs.get() ? def1 : def2, out.get());
                }
            }
        }
    }

    @Test
    public void testSelectBooleanBooleanInput() {
        for (boolean def : Values.interestingBooleans) {
            BooleanCell bs = new BooleanCell();
            BooleanCell out = new BooleanCell(), tru = new BooleanCell();
            BooleanInput bi = bs.select(def, tru);
            bi.send(out);
            assertEquals(def, bi.get());
            assertEquals(def, out.get());
            for (int i = 0; i < 30; i++) {
                tru.set(true);
                bs.toggle();
                assertEquals(bs.get() ? true : def, bi.get());
                assertEquals(bs.get() ? true : def, out.get());
                for (boolean b : Values.interestingBooleans) {
                    tru.set(b);
                    assertEquals(bs.get() ? b : def, bi.get());
                    assertEquals(bs.get() ? b : def, out.get());
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSelectBooleanBooleanInputNull() {
        bi.select(false, null);
    }

    @Test
    public void testSelectBooleanInputBoolean() {
        for (boolean def : Values.interestingBooleans) {
            BooleanCell bs = new BooleanCell();
            BooleanCell out = new BooleanCell(), fals = new BooleanCell(false);
            BooleanInput bi = bs.select(fals, def);
            bi.send(out);
            assertEquals(false, bi.get());
            assertEquals(false, out.get());
            for (int i = 0; i < 30; i++) {
                fals.set(false);
                bs.toggle();
                assertEquals(bs.get() ? def : false, bi.get());
                assertEquals(bs.get() ? def : false, out.get());
                for (boolean b : Values.interestingBooleans) {
                    fals.set(b);
                    assertEquals(bs.get() ? def : b, bi.get());
                    assertEquals(bs.get() ? def : b, out.get());
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSelectBooleanInputBooleanNull() {
        bi.select(null, true);
    }

    @Test
    public void testSelectBooleanInputBooleanInput() {
        BooleanCell bs = new BooleanCell();
        BooleanCell out = new BooleanCell(), tru = new BooleanCell(), fals = new BooleanCell(false);
        BooleanInput bi = bs.select(fals, tru);
        bi.send(out);
        assertEquals(false, bi.get());
        assertEquals(false, out.get());
        for (int i = 0; i < 30; i++) {
            fals.set(true);
            tru.set(false);
            bs.toggle();
            assertEquals(bs.get() ? tru.get() : fals.get(), bi.get());
            assertEquals(bs.get() ? tru.get() : fals.get(), out.get());
            BooleanCell var = i % 2 == 0 ? tru : fals;
            for (boolean b : Values.interestingBooleans) {
                var.set(b);// switch which one we vary each time outside
                assertEquals(bs.get() ? tru.get() : fals.get(), bi.get());
                assertEquals(bs.get() ? tru.get() : fals.get(), out.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSelectBooleanInputBooleanInputNull() {
        bi.select(null, null);
    }

    @Test
    public void testOnUpdate() {
        CountingEventOutput ceo = new CountingEventOutput();
        bi = new BooleanInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
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

    @Test
    public void testSendError() {
        bi = new BooleanInput() {
            @Override
            public CancelOutput onUpdate(EventOutput notify) {
                expected.event();
                return CancelOutput.nothing;
            }

            @Override
            public boolean get() {
                expected2.event();
                return true;
            }
        };
        CountingBooleanOutput cbo = new CountingBooleanOutput();
        BooleanOutput evil = new BooleanOutput() {
            @Override
            public void set(boolean value) {
                cbo.set(value);
                // TODO: check logging.
                throw new NoSuchElementException(ERROR_STRING);
            }
        };
        expected.ifExpected = expected2.ifExpected = cbo.ifExpected = true;
        cbo.valueExpected = true;
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        bi.send(evil);
        VerifyingLogger.check();
        expected.check();
        expected2.check();
        cbo.check();
    }
}
