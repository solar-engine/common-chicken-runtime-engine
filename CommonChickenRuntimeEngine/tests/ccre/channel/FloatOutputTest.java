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
package ccre.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ccre.log.LogLevel;
import ccre.log.VerifyingLogger;
import ccre.scheduler.VirtualTime;
import ccre.testing.CountingFloatOutput;
import ccre.util.Utils;
import ccre.util.Values;

@SuppressWarnings("javadoc")
public class FloatOutputTest {

    private static final String ERROR_STRING = "safeSet purposeful failure.";

    private final FloatOutput evil = (v) -> {
        throw new NoSuchElementException(ERROR_STRING);
    };
    private CountingFloatOutput cfo, cfo2;
    private FloatCell fs;

    @Before
    public void setUp() throws Exception {
        // For derivative testing
        VirtualTime.startFakeTime();
        fs = new FloatCell();
        cfo = new CountingFloatOutput();
        cfo2 = new CountingFloatOutput();
        VerifyingLogger.begin();
    }

    @After
    public void tearDown() throws Exception {
        VerifyingLogger.checkAndEnd();
        fs = null;
        cfo = null;
        cfo2 = null;
        VirtualTime.endFakeTime();
    }

    @Test
    public void testIgnored() {
        FloatOutput.ignored.set(0);
    }

    @Test
    public void testIgnoredCombine() {
        assertEquals(cfo, FloatOutput.ignored.combine(cfo));
    }

    @Test(expected = NullPointerException.class)
    public void testIgnoredCombineNull() {
        FloatOutput.ignored.combine((FloatOutput) null);
    }

    @Test
    public void testGetSetEventFloat() {
        for (float f : Values.interestingFloats) {
            EventOutput setE = cfo.eventSet(f);
            cfo.ifExpected = true;
            cfo.valueExpected = f;
            setE.event();
            cfo.check();
        }
    }

    @Test
    public void testGetSetEventFloatInput() {
        EventOutput setE = cfo.eventSet(fs);
        for (float f : Values.interestingFloats) {
            fs.set(f);
            cfo.ifExpected = true;
            cfo.valueExpected = f;
            setE.event();
            cfo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testGetSetEventFloatInputNull() {
        cfo.eventSet(null);
    }

    @Test
    public void testSetWhenFloatEventInput() {
        for (float f : Values.interestingFloats) {
            EventCell es = new EventCell();
            cfo.setWhen(f, es);
            cfo.ifExpected = true;
            cfo.valueExpected = f;
            es.event();
            cfo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSetWhenFloatEventInputNull() {
        cfo.setWhen(0, null);
    }

    @Test
    public void testSetWhenFloatInputEventInput() {
        EventCell setE = new EventCell();
        cfo.setWhen(fs, setE);
        for (float f : Values.interestingFloats) {
            fs.set(f);
            cfo.ifExpected = true;
            cfo.valueExpected = f;
            setE.event();
            cfo.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSetWhenFloatInputEventInputNullA() {
        cfo.setWhen(null, EventInput.never);
    }

    @Test(expected = NullPointerException.class)
    public void testSetWhenFloatInputEventInputNullB() {
        cfo.setWhen(FloatInput.zero, null);
    }

    @Test
    public void testCombine() {
        FloatOutput combine = cfo.combine(cfo2);
        for (float f : Values.interestingFloats) {
            cfo.ifExpected = cfo2.ifExpected = true;
            cfo.valueExpected = cfo2.valueExpected = f;
            combine.set(f);
            cfo.check();
            cfo2.check();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCombineNull() {
        cfo.combine(null);
    }

    @Test
    public void testStaticCombineSimplification() {
        assertEquals(FloatOutput.combine(), FloatOutput.ignored);
        assertEquals(FloatOutput.combine(new FloatOutput[] { cfo }), cfo);
    }

    @Test
    public void testStaticCombine() {
        for (int n = 0; n < 20; n++) {
            CountingFloatOutput[] cfos = new CountingFloatOutput[n];
            for (int i = 0; i < n; i++) {
                cfos[i] = new CountingFloatOutput();
            }
            FloatOutput combined = FloatOutput.combine(cfos);
            for (float f : Values.interestingFloats) {
                for (int i = 0; i < n; i++) {
                    cfos[i].ifExpected = true;
                    cfos[i].valueExpected = f;
                }
                combined.set(f);
                for (int i = 0; i < n; i++) {
                    cfos[i].check();
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNull() {
        FloatOutput.combine((FloatOutput[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullElem() {
        FloatOutput.combine(new FloatOutput[] { null });
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullEarlierElem() {
        FloatOutput.combine(null, cfo);
    }

    @Test(expected = NullPointerException.class)
    public void testStaticCombineNullLaterElem() {
        FloatOutput.combine(cfo, null);
    }

    @Test
    public void testNegate() {
        FloatOutput neg = cfo.negate();
        for (float f : Values.interestingFloats) {
            cfo.ifExpected = true;
            cfo.valueExpected = -f;
            neg.set(f);
            cfo.check();
        }
    }

    @Test
    public void testOutputDeadzone() {
        for (float zone : Values.lessInterestingFloats) {
            if (!Float.isFinite(zone) || zone < 0) {
                continue;
            }
            FloatOutput fout = cfo.outputDeadzone(zone);
            for (float v : Values.lessInterestingFloats) {
                if (Float.isNaN(v)) {
                    continue;
                }
                if (Math.abs(v) < Math.abs(zone)) {
                    cfo.valueExpected = 0;
                } else {
                    cfo.valueExpected = v;
                }
                cfo.ifExpected = true;
                fout.set(v);
                cfo.check();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputDeadzoneNaN() {
        cfo.outputDeadzone(Float.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputDeadzoneNegative() {
        cfo.outputDeadzone(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputDeadzoneInfinity() {
        cfo.outputDeadzone(Float.POSITIVE_INFINITY);
    }

    @Test
    public void testAddRamping() { // TODO: flesh this out a bit more
        EventCell update = new EventCell();
        FloatOutput fo = cfo.addRamping(0.2f, update);
        for (int i = 0; i < 5; i++) {
            cfo.ifExpected = true;
            cfo.valueExpected = 0;
            update.event();
            cfo.check();
        }
        float last = 0;
        for (float i = -5f; i < 5f; i++) {
            fo.set(i);
            int j = 0;
            while (true) {
                cfo.ifExpected = true;
                last = cfo.valueExpected = Utils.updateRamping(last, i, 0.2f);
                update.event();
                cfo.check();
                if (last == i) {
                    break;
                }
                if (++j >= 100) {
                    fail("never reached target");
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testAddRampingNull() {
        cfo.addRamping(0.2f, null);
    }

    @Test
    public void testViaDerivative() throws InterruptedException {
        Random rand = new Random(1);
        FloatOutput fo = cfo.viaDerivative();
        float j = 0;
        for (float i = -10.0f; i <= 10.0f; i += 0.5f) {
            cfo.valueExpected = (i - j); // for subsequent times around the
            // loop
            fo.set(i);
            cfo.check();
            for (j = i; j <= i + 20.0f;) {
                float delta = (rand.nextInt(30) + 1) / 10.0f;
                long timeDelta = rand.nextInt(2000) + 1;
                VirtualTime.forward(timeDelta);
                j += delta;
                cfo.ifExpected = true;
                cfo.valueExpected = 1000 * delta / timeDelta;
                cfo.maxDelta = 0.00001f * Math.abs(cfo.valueExpected);
                fo.set(j);
                cfo.check();
            }
            VirtualTime.forward(1000);
            cfo.ifExpected = true;
        }
    }

    @Test
    public void testFilter() {
        for (boolean not : new boolean[] { false, true }) {
            BooleanCell allowDeny = new BooleanCell();
            FloatCell out = new FloatCell();
            FloatOutput fo = not ? out.filterNot(allowDeny) : out.filter(allowDeny);
            int j = 0;
            float expect = 0;
            for (int i = 0; i < 10; i++) {
                for (float f : Values.interestingFloats) {
                    boolean nvalue = ((j % 19) < 10) ^ not;
                    if (allowDeny.get() == not && nvalue != not) {
                        float f2 = Values.getRandomFloat();
                        // switching to active: expect an update as necessary
                        fo.set(f2);
                        assertEquals(Float.floatToIntBits(expect), Float.floatToIntBits(out.get()));

                        allowDeny.set(nvalue);
                        expect = f2;

                        assertEquals(Float.floatToIntBits(expect), Float.floatToIntBits(out.get()));
                    } else {
                        allowDeny.set(nvalue);
                    }

                    fo.set(f);
                    if (allowDeny.get() ^ not) {
                        expect = f;
                    }
                    assertEquals(Float.floatToIntBits(expect), Float.floatToIntBits(out.get()));

                    j++;
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFilterNull() {
        cfo.filter(null);
    }

    @Test(expected = NullPointerException.class)
    public void testFilterNotNull() {
        cfo.filterNot(null);
    }

    @Test
    public void testFromBoolean1() {
        FloatCell offV = new FloatCell();
        FloatCell onV = new FloatCell();
        Boolean lastSent = null;
        BooleanOutput b1 = cfo.fromBoolean(offV, onV);
        for (float off : Values.interestingFloats) {
            cfo.ifExpected = (lastSent != null && !lastSent);
            cfo.valueExpected = off;
            offV.set(off);
            cfo.check();
            for (float on : Values.interestingFloats) {
                cfo.ifExpected = (lastSent != null && lastSent);
                cfo.valueExpected = on;
                onV.set(on);
                cfo.check();
                for (boolean b : Values.interestingBooleans) {
                    cfo.valueExpected = b ? on : off;

                    cfo.ifExpected = (lastSent == null || lastSent != b);
                    b1.set(b);
                    cfo.check();

                    lastSent = b;
                }
            }
            boolean b = Values.getRandomBoolean();
            cfo.ifExpected = (lastSent != null && lastSent != b);
            cfo.valueExpected = b ? onV.get() : off;
            b1.set(b);
            cfo.check();
            lastSent = b;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFromBoolean1NullA() {
        cfo.fromBoolean(null, FloatInput.zero);
    }

    @Test(expected = NullPointerException.class)
    public void testFromBoolean1NullB() {
        cfo.fromBoolean(FloatInput.zero, null);
    }

    @Test
    public void testFromBoolean2() {
        for (float off : Values.interestingFloats) {
            Boolean lastSent = null;
            FloatCell onV = new FloatCell();
            BooleanOutput b1 = cfo.fromBoolean(off, onV);
            for (float on : Values.interestingFloats) {
                cfo.ifExpected = (lastSent != null && lastSent);
                cfo.valueExpected = on;
                onV.set(on);
                cfo.check();
                for (boolean b : Values.interestingBooleans) {
                    cfo.valueExpected = b ? on : off;

                    cfo.ifExpected = (lastSent == null || lastSent != b);
                    b1.set(b);
                    cfo.check();

                    lastSent = b;
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFromBoolean2Null() {
        cfo.fromBoolean(0, null);
    }

    @Test
    public void testFromBoolean3() {
        for (float on : Values.interestingFloats) {
            Boolean lastSent = null;
            FloatCell offV = new FloatCell();
            BooleanOutput b1 = cfo.fromBoolean(offV, on);
            for (float off : Values.interestingFloats) {
                cfo.ifExpected = (lastSent != null && !lastSent);
                cfo.valueExpected = on;
                offV.set(off);
                cfo.check();
                for (boolean b : Values.interestingBooleans) {
                    cfo.valueExpected = b ? on : off;

                    cfo.ifExpected = (lastSent == null || lastSent != b);
                    b1.set(b);
                    cfo.check();

                    lastSent = b;
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testFromBoolean3Null() {
        cfo.fromBoolean(null, 0);
    }

    @Test
    public void testFromBoolean4() {
        for (float off : Values.interestingFloats) {
            for (float on : Values.interestingFloats) {
                Boolean lastSent = null;
                BooleanOutput b1 = cfo.fromBoolean(off, on);
                for (boolean b : Values.interestingBooleans) {
                    cfo.valueExpected = b ? on : off;

                    cfo.ifExpected = (lastSent == null || lastSent != b);
                    b1.set(b);
                    cfo.check();

                    lastSent = b;
                }
            }
        }
    }

    @Test
    public void testSafeSet() {
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        evil.safeSet(0);
        VerifyingLogger.check();
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        evil.safeSet(1);
        VerifyingLogger.check();
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionPropagationFalse() {
        evil.set(1);
    }

    @Test(expected = NoSuchElementException.class)
    public void testCombineWithError1CausesError() {
        cfo.ifExpected = true;
        cfo.valueExpected = 1.2f;
        cfo.combine(evil).set(1.2f);
    }

    @Test(expected = NoSuchElementException.class)
    public void testCombineWithError2CausesError() {
        cfo.ifExpected = true;
        cfo.valueExpected = 1;
        evil.combine(cfo).set(1);
    }

    @Test
    public void testCombineWithError1Succeeds() {
        cfo.ifExpected = true;
        cfo.valueExpected = 1;
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        cfo.combine(evil).safeSet(1);
        VerifyingLogger.check();
        cfo.check();
    }

    @Test
    public void testCombineWithError2Succeeds() {
        cfo.ifExpected = true;
        cfo.valueExpected = 1;
        VerifyingLogger.configure(LogLevel.SEVERE, "Error during channel propagation", (t) -> t.getClass() == NoSuchElementException.class && ERROR_STRING.equals(t.getMessage()));
        evil.combine(cfo).safeSet(1);
        VerifyingLogger.check();
        cfo.check();
    }

    @Test
    public void testCombineWithError3CausesError() {
        boolean errored = false;
        try {
            evil.combine(evil).set(1);
        } catch (NoSuchElementException ex) {
            errored = true;
            assertEquals(ex.getSuppressed().length, 1);
            assertTrue(ex.getSuppressed()[0] instanceof NoSuchElementException);
        }
        assertTrue(errored);
    }

    @Test
    public void testStaticCombineSingleError() {
        for (int n = 1; n < 6; n++) {
            CountingFloatOutput[] cfos = new CountingFloatOutput[n];
            for (int i = 0; i < n; i++) {
                cfos[i] = new CountingFloatOutput();
            }
            for (int bad = 0; bad < n; bad++) {
                FloatOutput[] reals = new FloatOutput[n];
                System.arraycopy(cfos, 0, reals, 0, n);
                reals[bad] = evil;
                FloatOutput combined = FloatOutput.combine(reals);
                for (float f : Values.interestingFloats) {
                    for (int i = 0; i < n; i++) {
                        cfos[i].ifExpected = i != bad;
                        cfos[i].valueExpected = f;
                    }
                    try {
                        combined.set(f);
                        fail();
                    } catch (NoSuchElementException ex) {
                        assertEquals(0, ex.getSuppressed().length);
                    }
                    for (int i = 0; i < n; i++) {
                        cfos[i].check();
                    }
                }
            }
        }
    }

    @Test
    public void testStaticCombineManyErrors() {
        for (int n = 1; n < 6; n++) {
            CountingFloatOutput[] cfos = new CountingFloatOutput[n];
            for (int i = 0; i < n; i++) {
                cfos[i] = new CountingFloatOutput();
            }
            for (int bad = 0; bad < 4 * n * n; bad++) {
                boolean[] evils = new boolean[n];
                int evil_count = 0;
                FloatOutput[] reals = new FloatOutput[n];
                System.arraycopy(cfos, 0, reals, 0, n);
                for (int i = 0; i < n; i++) {
                    evils[i] = Values.getRandomBoolean();
                    if (evils[i]) {
                        evil_count++;
                        reals[i] = evil;
                    }
                }
                FloatOutput combined = FloatOutput.combine(reals);
                for (float f : Values.interestingFloats) {
                    for (int i = 0; i < n; i++) {
                        cfos[i].ifExpected = !evils[i];
                        cfos[i].valueExpected = f;
                    }
                    try {
                        combined.set(f);
                        assertEquals(0, evil_count);
                    } catch (NoSuchElementException ex) {
                        Throwable[] suppressed = ex.getSuppressed();
                        assertEquals(evil_count - 1, suppressed.length);
                        for (int i = 0; i < suppressed.length; i++) {
                            assertTrue(suppressed[i] instanceof NoSuchElementException);
                        }
                    }
                    for (int i = 0; i < n; i++) {
                        cfos[i].check();
                    }
                }
            }
        }
    }

    @Test
    public void testNegateIf() {
        for (boolean init : new boolean[] { false, true }) {
            BooleanCell cond = new BooleanCell(init);
            FloatOutput neg = cfo.negateIf(cond);
            for (float v : Values.interestingFloats) {
                cfo.ifExpected = true;
                cfo.valueExpected = cond.get() ? -v : v;
                neg.set(v);
                cfo.check();
                if (Values.getRandomBoolean()) {
                    cfo.ifExpected = true;
                    cfo.valueExpected = cond.get() ? v : -v;
                    cond.toggle();
                    cfo.check();
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNegateIfNull() {
        fs.negateIf(null);
    }

    @Test
    public void testCell() {
        for (float of : Values.interestingFloats) {
            FloatOutput out = cfo::set;
            cfo.ifExpected = true;
            cfo.valueExpected = of;
            FloatIO fio = out.cell(of);
            cfo.check();
            cfo2.ifExpected = true;
            cfo2.valueExpected = of;
            fio.send(cfo2);
            cfo2.check();
            for (int i = 0; i < 20; i++) {
                float f = Values.getRandomFloat();
                cfo.ifExpected = cfo2.ifExpected = Float.floatToIntBits(fio.get()) != Float.floatToIntBits(f);
                cfo.valueExpected = cfo2.valueExpected = f;
                fio.set(f);
                cfo2.check();
                cfo.check();
                assertEquals(f, fio.get(), 0);
            }
        }
    }
}
