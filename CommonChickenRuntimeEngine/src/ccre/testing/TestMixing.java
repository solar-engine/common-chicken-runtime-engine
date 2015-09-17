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
import ccre.channel.FloatStatus;

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
        testSelectBool(false);
        testSelectBool(true);
    }

    private void testSelectBool(boolean default_) throws TestingException {
        FloatStatus offStat = new FloatStatus(), onStat = new FloatStatus();
        CountingFloatOutput cfo1 = new CountingFloatOutput();
        CountingFloatOutput cfo2 = new CountingFloatOutput();
        cfo1.valueExpected = 0;
        cfo1.ifExpected = true;
        BooleanOutput bout1 = cfo1.fromBoolean(offStat, onStat, default_);
        cfo1.check();
        boolean lastValueTo1 = default_;
        for (float off : TestFloatMixing.interestingFloats) {
            for (float on : TestFloatMixing.interestingFloats) {
                // at this point, the value is FALSE
                cfo1.valueExpected = lastValueTo1 ? on : off;
                cfo1.ifExpected = true;
                offStat.set(off);
                onStat.set(on);// should make no difference because the last value was FALSE
                cfo1.check();

                cfo2.valueExpected = default_ ? on : off;
                cfo2.ifExpected = true;
                BooleanOutput bout2 = cfo2.fromBoolean(off, on, default_);
                cfo2.check();
                boolean last = default_;
                for (boolean value : new boolean[] { false, true, true, false, false, true, false, true, false, true, true, true, false, true, false, false, false, true }) {
                    float newlyExpected = cfo2.valueExpected = value ? on : off;
                    boolean repeat = newlyExpected == cfo1.valueExpected;
                    cfo1.valueExpected = newlyExpected;
                    cfo1.ifExpected = (value != lastValueTo1 && !repeat);// TODO: is this test too specific?
                    bout1.set(value);
                    lastValueTo1 = value;
                    cfo1.check();
                    cfo2.ifExpected = (value != last);
                    bout2.set(value);
                    cfo2.check();
                    last = value;
                }
                if (last) {
                    cfo2.valueExpected = off;
                    cfo2.ifExpected = true;
                    bout2.set(false);
                    cfo2.check();
                }
            }
        }
    }
}
