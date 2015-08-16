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

import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
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
        for (float off : TestFloatMixing.interestingFloats) {
            for (float on : TestFloatMixing.interestingFloats) {
                offStat.set(off);
                onStat.set(on);
                FloatInput fin2 = Mixing.select(vbool.asInput(), off, on);
                // make sure that changes to the parameters propagate
                //vbool.set(false);
                //vbool.set(true);
                for (boolean value : TestBooleanMixing.interestingBooleans) {
                    vbool.set(value);
                    float expected = value ? on : off;
                    assertObjectEqual(fin1.get(), expected, "bad float 1: " + value + " => " + off + ", " + on);
                    assertObjectEqual(fin2.get(), expected, "bad float 5: " + value + " => " + off + ", " + on);
                }
            }
        }
    }

    private void testSelectBool() throws TestingException {
        FloatStatus offStat = new FloatStatus(), onStat = new FloatStatus();
        TestFloatMixing.CountingFloatOutput cfo1 = new TestFloatMixing.CountingFloatOutput();
        TestFloatMixing.CountingFloatOutput cfo2 = new TestFloatMixing.CountingFloatOutput();
        BooleanOutput bout1 = Mixing.select(cfo1, offStat, onStat);
        boolean firstIteration = true;
        for (float off : TestFloatMixing.interestingFloats) {
            for (float on : TestFloatMixing.interestingFloats) {
                // at this point, the value is FALSE
                cfo1.valueExpected = off;
                cfo1.ifExpected = (Float.floatToIntBits(off) != Float.floatToIntBits(offStat.get())) && !firstIteration; // if it's the first iteration, updating the arguments should have no effect, since it's never received a value
                offStat.set(off);
                cfo1.check();
                onStat.set(on); // should make no difference because the last value was FALSE

                BooleanOutput bout2 = Mixing.select(cfo2, off, on);
                boolean last = false;
                for (boolean value : TestBooleanMixing.interestingBooleans) {
                    cfo1.valueExpected = cfo2.valueExpected = value ? on : off;
                    cfo1.ifExpected = true;
                    bout1.set(value);
                    cfo1.check();
                    cfo2.ifExpected = true;
                    bout2.set(value);
                    cfo2.check();
                    last = value;
                }
                if (last) {
                    cfo1.valueExpected = cfo2.valueExpected = off;
                    cfo1.ifExpected = true;
                    bout1.set(false);
                    cfo1.check();
                    cfo2.ifExpected = true;
                    bout2.set(false);
                    cfo2.check();
                }

                firstIteration = false;
            }
        }
    }
}
