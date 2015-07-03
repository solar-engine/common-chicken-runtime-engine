/*
 * Copyright 2013-2015 Colby Skeggs
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
package ccre.testing;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatStatus;
import ccre.ctrl.Mixing;

/**
 * Tests the Mixing class.
 *
 * @author skeggsc
 */
public class TestMixing extends BaseTest {

    @Override
    public String getName() {
        return "Mixing Test";
    }

    @Override
    protected void runTest() throws TestingException, InterruptedException {
        testSelectFloat();
        testSelectBool();
    }

    private void testSelectFloat() throws TestingException {
        FloatStatus offStat = new FloatStatus(1), onStat = new FloatStatus(2);
        BooleanStatus vbool = new BooleanStatus();
        FloatInput fin1 = Mixing.select(vbool.asInput(), offStat, onStat);
        FloatInputPoll fin2 = Mixing.select((BooleanInputPoll) vbool, offStat, onStat);
        FloatInput fin3 = Mixing.select(vbool.asInput(), false, offStat, onStat);
        FloatInput fin4 = Mixing.select(vbool.asInput(), true, offStat, onStat);
        // We don't bother testing the initial values of fin3 and fin4 because vbool instantly overwrites the defaults on registration.
        // TODO: Maybe we should?
        for (float off : TestFloatMixing.interestingFloats) {
            for (float on : TestFloatMixing.interestingFloats) {
                offStat.set(off);
                onStat.set(on);
                FloatInput fin5 = Mixing.select(vbool.asInput(), off, on);
                FloatInputPoll fin6 = Mixing.select((BooleanInputPoll) vbool, off, on);
                FloatInput fin7 = Mixing.select(vbool.asInput(), false, off, on);
                FloatInput fin8 = Mixing.select(vbool.asInput(), true, off, on);
                // make sure that changes to the parameters propagate
                //vbool.set(false);
                //vbool.set(true);
                for (boolean value : TestBooleanMixing.interestingBooleans) {
                    vbool.set(value);
                    float expected = value ? on : off;
                    assertObjectEqual(fin1.get(), expected, "bad float 1: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin2.get(), expected, "bad float 2: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin3.get(), expected, "bad float 3: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin4.get(), expected, "bad float 4: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin5.get(), expected, "bad float 5: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin6.get(), expected, "bad float 6: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin7.get(), expected, "bad float 7: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin8.get(), expected, "bad float 8: " + value + " => " + off + ", " + on);
                }
            }
        }
    }

    private void testSelectBool() throws TestingException {
        FloatStatus offStat = new FloatStatus(), onStat = new FloatStatus();
        TestFloatMixing.CountingFloatOutput cfo1 = new TestFloatMixing.CountingFloatOutput();
        TestFloatMixing.CountingFloatOutput cfo2 = new TestFloatMixing.CountingFloatOutput();
        BooleanOutput bout1 = Mixing.select(cfo1, offStat, onStat);
        for (float off : TestFloatMixing.interestingFloats) {
            for (float on : TestFloatMixing.interestingFloats) {
                offStat.set(off);
                onStat.set(on);
                BooleanOutput bout2 = Mixing.select(cfo2, off, on);
                for (boolean value : TestBooleanMixing.interestingBooleans) {
                    cfo1.valueExpected = cfo2.valueExpected = value ? on : off;
                    cfo1.ifExpected = true;
                    bout1.set(value);
                    cfo1.check();
                    cfo2.ifExpected = true;
                    bout2.set(value);
                    cfo2.check();
                }
            }
        }
    }
}
