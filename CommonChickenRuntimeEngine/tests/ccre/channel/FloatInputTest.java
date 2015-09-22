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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ccre.testing.CountingEventOutput;
import ccre.testing.CountingFloatOutput;
import ccre.time.FakeTime;
import ccre.time.Time;
import ccre.util.Values;

public class FloatInputTest {

    private CountingFloatOutput cfo;
    private FloatInput fi;
    private FloatStatus fs, fs2;

    private static Time oldProvider;
    private static FakeTime fake;

    // These two are for derivative testing.
    @BeforeClass
    public static void setUpClass() {
        assertNull(oldProvider);
        oldProvider = Time.getTimeProvider();
        fake = new FakeTime();
        Time.setTimeProvider(fake);
    }

    @AfterClass
    public static void tearDownClass() {
        assertNotNull(oldProvider);
        Time.setTimeProvider(oldProvider);
        oldProvider = null;
        fake = null;
    }

    @Before
    public void setUp() throws Exception {
        cfo = new CountingFloatOutput();
        fi = FloatInput.always(7.3f);
        fs = new FloatStatus();
        fs2 = new FloatStatus();
    }

    @After
    public void tearDown() throws Exception {
        cfo = null;
        fi = null;
        fs = fs2 = null;
    }

    @Test
    public void testAlways() {
        for (float f : Values.interestingFloats) {
            FloatInput always = FloatInput.always(f);
            assertEquals(f, always.get(), 0);
            cfo.ifExpected = true;
            cfo.valueExpected = f;
            always.send(cfo);
            cfo.check();
        }
    }

    @Test
    public void testZero() {
        FloatInput always = FloatInput.zero;
        assertEquals(0, always.get(), 0);
        cfo.ifExpected = true;
        cfo.valueExpected = 0;
        always.send(cfo);
        cfo.check();
    }

    private boolean gotProperly;
    private float result;

    @Test
    public void testSend() {
        gotProperly = false;
        fi = new FloatInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                assertFalse(gotProperly);
                cfo.check();// the earlier setup from outside this
                for (float f : Values.interestingFloats) {
                    result = f;
                    cfo.ifExpected = true;
                    cfo.valueExpected = f;
                    notify.event();
                    cfo.check();
                }
                gotProperly = true;
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                fail();
                return null;
            }

            @Override
            public float get() {
                return result;
            }
        };
        for (float f : Values.interestingFloats) {
            result = f;
            assertEquals(f, fi.get(), 0);
        }
        for (float initial : Values.interestingFloats) {
            cfo.ifExpected = true;
            cfo.valueExpected = result = initial;
            assertFalse(gotProperly);
            fi.send(cfo);
            assertTrue(gotProperly);
            gotProperly = false;
            cfo.check();// the real check is in onUpdateR above
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSendNull() {
        fi.send(null);
    }

    @Test
    public void testSendR() {
        CountingEventOutput expected = new CountingEventOutput();
        gotProperly = false;
        fi = new FloatInput() {
            @Override
            public void onUpdate(EventOutput notify) {
                fail();
            }

            @Override
            public EventOutput onUpdateR(EventOutput notify) {
                assertFalse(gotProperly);
                cfo.check();// the earlier setup from outside this
                for (float f : Values.interestingFloats) {
                    result = f;
                    cfo.ifExpected = true;
                    cfo.valueExpected = f;
                    notify.event();
                    cfo.check();
                }
                gotProperly = true;
                return expected;
            }

            @Override
            public float get() {
                return result;
            }
        };
        for (float f : Values.interestingFloats) {
            result = f;
            assertEquals(f, fi.get(), 0);
        }
        for (float initial : Values.interestingFloats) {
            cfo.ifExpected = true;
            cfo.valueExpected = result = initial;
            assertFalse(gotProperly);
            assertEquals(expected, fi.sendR(cfo));
            assertTrue(gotProperly);
            gotProperly = false;
            cfo.check();// the real check is in onUpdateR above
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSendRNull() {
        fi.sendR(null);
    }

    private void tryEach(FloatInput fi, FloatInput fi2) {
        for (float f1 : Values.interestingFloats) {
            fs.set(f1);
            for (float f2 : Values.interestingFloats) {
                fs2.set(f2);
                assertEquals(fi.get(), fi2.get(), 0);
            }
        }
    }

    @Test
    public void testPlusFloatInput() {
        tryEach(fs.plus(fs2), FloatOperation.addition.of(fs.asInput(), fs2.asInput()));
    }

    @Test(expected = NullPointerException.class)
    public void testPlusNull() {
        fi.plus(null);
    }

    @Test
    public void testMinusFloatInput() {
        tryEach(fs.minus(fs2), FloatOperation.subtraction.of(fs.asInput(), fs2.asInput()));
    }

    @Test(expected = NullPointerException.class)
    public void testMinusNull() {
        fi.minus(null);
    }

    @Test
    public void testMinusRevFloatInput() {
        tryEach(fs.minusRev(fs2), FloatOperation.subtraction.of(fs2.asInput(), fs.asInput()));
    }

    @Test(expected = NullPointerException.class)
    public void testMinusRevNull() {
        fi.minusRev(null);
    }

    @Test
    public void testMultipliedByFloatInput() {
        tryEach(fs.multipliedBy(fs2), FloatOperation.multiplication.of(fs.asInput(), fs2.asInput()));
    }

    @Test(expected = NullPointerException.class)
    public void testMultipliedByNull() {
        fi.multipliedBy(null);
    }

    @Test
    public void testDividedByFloatInput() {
        tryEach(fs.dividedBy(fs2), FloatOperation.division.of(fs.asInput(), fs2.asInput()));
    }

    @Test(expected = NullPointerException.class)
    public void testDividedByNull() {
        fi.dividedBy(null);
    }

    @Test
    public void testDividedByRevFloatInput() {
        tryEach(fs.dividedByRev(fs2), FloatOperation.division.of(fs2.asInput(), fs.asInput()));
    }

    @Test(expected = NullPointerException.class)
    public void testDividedByRevNull() {
        fi.dividedByRev(null);
    }

    private void trySet(FloatInput fi, FloatInput fi2) {
        for (float f1 : Values.interestingFloats) {
            fs.set(f1);
            assertEquals(fi.get(), fi2.get(), 0);
        }
    }

    @Test
    public void testPlusFloat() {
        for (float f2 : Values.interestingFloats) {
            trySet(fs.plus(f2), FloatOperation.addition.of(fs.asInput(), f2));
        }
    }

    @Test
    public void testMinusFloat() {
        for (float f2 : Values.interestingFloats) {
            trySet(fs.minus(f2), FloatOperation.subtraction.of(fs.asInput(), f2));
        }
    }

    @Test
    public void testMinusRevFloat() {
        for (float f2 : Values.interestingFloats) {
            trySet(fs.minusRev(f2), FloatOperation.subtraction.of(f2, fs.asInput()));
        }
    }

    @Test
    public void testMultipliedByFloat() {
        for (float f2 : Values.interestingFloats) {
            trySet(fs.multipliedBy(f2), FloatOperation.multiplication.of(fs.asInput(), f2));
        }
    }

    @Test
    public void testDividedByFloat() {
        for (float f2 : Values.interestingFloats) {
            trySet(fs.dividedBy(f2), FloatOperation.division.of(fs.asInput(), f2));
        }
    }

    @Test
    public void testDividedByRevFloat() {
        for (float f2 : Values.interestingFloats) {
            trySet(fs.dividedByRev(f2), FloatOperation.division.of(f2, fs.asInput()));
        }
    }

    @Test
    public void testAtLeastFloat() {
        for (float threshold : Values.interestingFloats) {
            if (Float.isNaN(threshold)) {
                continue;
            }
            BooleanInput bi = fs.atLeast(threshold);
            BooleanStatus bs = new BooleanStatus();
            bi.send(bs);
            for (float test : Values.interestingFloats) {
                fs.set(test);
                assertEquals(test >= threshold, bi.get());
                assertEquals(test >= threshold, bs.get());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAtLeastFloatInvalid() {
        fs.atLeast(Float.NaN);
    }

    @Test
    public void testAtLeastFloatInput() {
        FloatStatus ts = new FloatStatus();
        BooleanInput bi = fs.atLeast(ts);
        BooleanStatus bs = new BooleanStatus();
        bi.send(bs);
        for (float threshold : Values.interestingFloats) {
            ts.set(threshold);
            for (float test : Values.interestingFloats) {
                fs.set(test);
                assertEquals(test >= threshold, bi.get());
                assertEquals(test >= threshold, bs.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAtLeastNull() {
        fi.atLeast(null);
    }

    @Test
    public void testAtMostFloat() {
        for (float threshold : Values.interestingFloats) {
            if (Float.isNaN(threshold)) {
                continue;
            }
            BooleanInput bi = fs.atMost(threshold);
            BooleanStatus bs = new BooleanStatus();
            bi.send(bs);
            for (float test : Values.interestingFloats) {
                fs.set(test);
                assertEquals(test <= threshold, bi.get());
                assertEquals(test <= threshold, bs.get());
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAtMostFloatInvalid() {
        fs.atMost(Float.NaN);
    }

    @Test
    public void testAtMostFloatInput() {
        FloatStatus ts = new FloatStatus();
        BooleanInput bi = fs.atMost(ts);
        BooleanStatus bs = new BooleanStatus();
        bi.send(bs);
        for (float threshold : Values.interestingFloats) {
            ts.set(threshold);
            for (float test : Values.interestingFloats) {
                fs.set(test);
                assertEquals(test <= threshold, bi.get());
                assertEquals(test <= threshold, bs.get());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAtMostNull() {
        fi.atMost(null);
    }

    @Test
    public void testOutsideRangeFloatFloat() {
        BooleanStatus bs = new BooleanStatus();
        for (float min : Values.interestingFloats) {
            if (Float.isNaN(min)) {
                continue;
            }
            for (float max : Values.interestingFloats) {
                if (Float.isNaN(max)) {
                    continue;
                }
                BooleanInput bi = fs.outsideRange(min, max);
                EventOutput unbind = bi.sendR(bs);
                for (float test : Values.interestingFloats) {
                    fs.set(test);
                    assertEquals(test < min || test > max, bi.get());
                    assertEquals(test < min || test > max, bs.get());
                }
                unbind.event();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutsideRangeFloatInvalidA() {
        fs.outsideRange(Float.NaN, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutsideRangeFloatInvalidB() {
        fs.outsideRange(0, Float.NaN);
    }

    @Test
    public void testOutsideRangeFloatInputFloatInput() {
        FloatStatus min = new FloatStatus(), max = new FloatStatus();
        BooleanInput bi = fs.outsideRange(min, max);
        BooleanStatus bs = new BooleanStatus();
        bi.send(bs);
        for (float minv : Values.interestingFloats) {
            min.set(minv);
            for (float maxv : Values.interestingFloats) {
                max.set(maxv);
                for (float test : Values.interestingFloats) {
                    fs.set(test);
                    assertEquals(test < minv || test > maxv, bi.get());
                    assertEquals(test < minv || test > maxv, bs.get());
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testOutsideRangeNullA() {
        fi.outsideRange(null, fs);
    }

    @Test(expected = NullPointerException.class)
    public void testOutsideRangeNullB() {
        fi.outsideRange(fs, null);
    }

    @Test
    public void testInRangeFloatFloat() {
        for (float min : Values.interestingFloats) {
            if (Float.isNaN(min)) {
                continue;
            }
            for (float max : Values.interestingFloats) {
                if (Float.isNaN(max)) {
                    continue;
                }
                BooleanInput bi = fs.inRange(min, max);
                BooleanStatus bs = new BooleanStatus();
                bi.send(bs);
                for (float test : Values.interestingFloats) {
                    fs.set(test);
                    assertEquals(test >= min && test <= max, bi.get());
                    assertEquals(test >= min && test <= max, bs.get());
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInRangeFloatInvalidA() {
        fs.inRange(Float.NaN, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInRangeFloatInvalidB() {
        fs.inRange(0, Float.NaN);
    }

    @Test
    public void testInRangeFloatInputFloatInput() {
        FloatStatus min = new FloatStatus(), max = new FloatStatus();
        BooleanInput bi = fs.inRange(min, max);
        BooleanStatus bs = new BooleanStatus();
        bi.send(bs);
        for (float minv : Values.interestingFloats) {
            min.set(minv);
            for (float maxv : Values.interestingFloats) {
                max.set(maxv);
                for (float test : Values.interestingFloats) {
                    fs.set(test);
                    assertEquals(test >= minv && test <= maxv, bi.get());
                    assertEquals(test >= minv && test <= maxv, bs.get());
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testInRangeNullA() {
        fi.inRange(null, fs);
    }

    @Test(expected = NullPointerException.class)
    public void testInRangeNullB() {
        fi.inRange(fs, null);
    }

    @Test
    public void testNegated() {
        FloatInput neg = fs.negated();
        for (float v : Values.interestingFloats) {
            fs.set(v);
            assertEquals(v, -neg.get(), 0);
        }
    }

    @Test
    public void testOnChange() {
        CountingEventOutput ceo = new CountingEventOutput();
        fs.onChange().send(ceo);
        for (float v : Values.interestingFloats) {
            ceo.ifExpected = true;
            fs.set(v);
            ceo.check();
            fs.set(v);
            ceo.check();// should not happen
        }
    }

    @Test
    public void testOnChangeBy() {
        for (float d : Values.lessInterestingFloats) {
            if (Float.isNaN(d) || d < 0) {
                continue;
            }
            FloatStatus fs = new FloatStatus();
            CountingEventOutput ceo = new CountingEventOutput();
            fs.onChangeBy(d).send(ceo);
            float last = 0;
            ceo.ifExpected = true;
            fs.set(Float.NEGATIVE_INFINITY);
            ceo.check();
            ceo.ifExpected = true;
            fs.set(0);
            ceo.check();
            for (float v : Values.lessInterestingFloats) {
                if (Math.abs(last - v) > Math.abs(d)) {// note: if
                                                       // onChangeBy(0), then
                                                       // everything should
                                                       // trigger this.
                    last = v;
                    ceo.ifExpected = true;
                } else {
                    ceo.ifExpected = false;
                }
                fs.set(v);
                ceo.check();
                fs.set(v);
                ceo.check();// should not happen
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnChangeByNaN() {
        fs.onChangeBy(Float.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnChangeByNegative() {
        fs.onChangeBy(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnChangeByPInf() {
        fs.onChangeBy(Float.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnChangeByNInf() {
        fs.onChangeBy(Float.NEGATIVE_INFINITY);
    }

    @Test
    public void testDeadzone() {
        for (float zone : Values.lessInterestingFloats) {
            if (!Float.isFinite(zone) || zone < 0) {
                continue;
            }
            FloatInput fin = fs.deadzone(zone);
            for (float v : Values.lessInterestingFloats) {
                if (Float.isNaN(v)) {
                    continue;
                }
                fs.set(v);
                if (Math.abs(v) < Math.abs(zone)) {
                    assertEquals(0, fin.get(), 0.000001f);
                } else {
                    assertEquals(v, fin.get(), 0.000001f);
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeadzoneNaN() {
        fs.deadzone(Float.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeadzoneNegative() {
        fs.deadzone(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeadzoneInfinite() {
        fs.deadzone(Float.POSITIVE_INFINITY);
    }

    @Test
    public void testNormalize() {
        FloatStatus zs = new FloatStatus(), os = new FloatStatus();
        FloatInput fin1 = fs.normalize(zs, os);
        for (float zero : Values.interestingFloats) {
            if (!Float.isFinite(zero)) {
                continue;
            }
            zs.set(zero);
            FloatInput fin2 = fs.normalize(zero, os);
            for (float one : Values.interestingFloats) {
                if (!Float.isFinite(one) || !Float.isFinite(one - zero) || zero == one) {
                    continue;
                }
                os.set(one);
                FloatInput fin3 = fs.normalize(zs, one);
                FloatInput fin4 = fs.normalize(zero, one);
                for (float v : Values.interestingFloats) {
                    fs.set(v);
                    assertEquals((v - zero) / (one - zero), fin1.get(), 0.0001f);
                    assertEquals((v - zero) / (one - zero), fin2.get(), 0.0001f);
                    assertEquals((v - zero) / (one - zero), fin3.get(), 0.0001f);
                    assertEquals((v - zero) / (one - zero), fin4.get(), 0.0001f);
                }

                fs.set(zero);
                assertEquals(0, fin1.get(), 0);
                assertEquals(0, fin2.get(), 0);
                assertEquals(0, fin3.get(), 0);
                assertEquals(0, fin4.get(), 0);

                fs.set(one);
                assertEquals(1, fin1.get(), 0);
                assertEquals(1, fin2.get(), 0);
                assertEquals(1, fin3.get(), 0);
                assertEquals(1, fin4.get(), 0);

                if (Float.isFinite(2 * zero - one)) {
                    fs.set(2 * zero - one);
                    assertEquals(-1, fin1.get(), 0.000001f);
                    assertEquals(-1, fin2.get(), 0.000001f);
                    assertEquals(-1, fin3.get(), 0.000001f);
                    assertEquals(-1, fin4.get(), 0.000001f);
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeSame() {
        fs.normalize(3.2f, 3.2f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNaNA() {
        fs.normalize(Float.NaN, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNaNB() {
        fs.normalize(0, Float.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeInfA() {
        fs.normalize(Float.POSITIVE_INFINITY, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeInfB() {
        fs.normalize(0, Float.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNInfA() {
        fs.normalize(Float.NEGATIVE_INFINITY, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNInfB() {
        fs.normalize(0, Float.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNaNAI() {
        fs.normalize(Float.NaN, fs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNaNBI() {
        fs.normalize(fs, Float.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeInfAI() {
        fs.normalize(Float.POSITIVE_INFINITY, fs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeInfBI() {
        fs.normalize(fs, Float.POSITIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNInfAI() {
        fs.normalize(Float.NEGATIVE_INFINITY, fs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeNInfBI() {
        fs.normalize(fs, Float.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeHugeRange() {
        fs.normalize(-Float.MAX_VALUE, Float.MAX_VALUE);
    }

    @Test
    public void testFilterUpdates() {
        for (boolean not : new boolean[] { false, true }) {
            FloatStatus a = new FloatStatus(), out = new FloatStatus();
            BooleanStatus allowDeny = new BooleanStatus(true);
            fi = not ? a.filterUpdatesNot(allowDeny) : a.filterUpdates(allowDeny);
            fi.send(out);
            float expect = 0;
            int n = 0;
            for (int i = 0; i < 3; i++) {
                for (float f : Values.interestingFloats) {
                    allowDeny.set(((n++ % 9) < 5) ^ not);
                    a.set(f);
                    if (allowDeny.get() ^ not) {
                        expect = a.get();
                    }
                    assertEquals(expect, fi.get(), 0);
                    assertEquals(expect, out.get(), 0);
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFilterUpdatesNull() {
        fi.filterUpdates(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFilterUpdatesNotNull() {
        fi.filterUpdatesNot(null);
    }

    @Test
    public void testWithRamping() {
        EventStatus update = new EventStatus();
        FloatInput fi = fs.withRamping(0.2f, update);
        for (float i = -5f; i < 5f; i++) {
            float a = fi.get();
            fs.set(i);
            float b = fi.get();
            assertEquals(a, b, 0);
            int j = 0;
            while (true) {
                float old = fi.get();
                update.event();
                float delta = Math.abs(fi.get() - old);
                if (fi.get() == i) {
                    break;
                }
                assertEquals(delta, 0.2f, 0.000001f);
                if (++j >= 100) {
                    fail("never reached target");
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testWithRampingNull() {
        fi.withRamping(0.2f, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithRampingInvalid() {
        fi.withRamping(Float.NaN, EventInput.never);
    }

    @Test
    public void testCreateRampingEvent() {// TODO: perhaps flesh these tests out
                                          // a bit more?
        FloatStatus fi = new FloatStatus();
        EventOutput update = fs.createRampingEvent(0.2f, fi);
        for (float i = -5f; i < 5f; i++) {
            float a = fi.get();
            fs.set(i);
            float b = fi.get();
            assertEquals(a, b, 0);
            int j = 0;
            while (true) {
                float old = fi.get();
                update.event();
                float delta = Math.abs(fi.get() - old);
                if (fi.get() == i) {
                    break;
                }
                assertEquals(delta, 0.2f, 0.000001f);
                if (++j >= 100) {
                    fail("never reached target");
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCreateRampingEventNull() {
        fi.createRampingEvent(0.2f, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRampingEventInvalid() {
        fi.createRampingEvent(Float.NaN, fs);
    }

    @Test
    public void testDerivative() throws InterruptedException {
        Random rand = new Random(1);
        FloatInput fi = fs.derivative();
        for (float i = -10.0f; i <= 10.0f; i += 0.5f) {
            fs.set(i);
            for (float j = i; j <= i + 20.0f;) {
                float delta = (rand.nextInt(30) + 1) / 10.0f;
                long timeDelta = rand.nextInt(2000) + 1;
                fake.forward(timeDelta);
                j += delta;
                fs.set(j);
                assertEquals(1000 * delta / timeDelta, fi.get(), 0.00001f * (1000 * delta / timeDelta));
            }
            fake.forward(1000);
        }
    }
}
