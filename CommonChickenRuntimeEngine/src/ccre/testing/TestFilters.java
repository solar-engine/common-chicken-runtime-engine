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
 *
 *
 * This file contains code inspired by/based on code Copyright 2008-2014 FIRST.
 * To see the license terms of that code (modified BSD), see the root of the CCRE.
 */
package ccre.testing;

import ccre.channel.BooleanFilter;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatFilter;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;

/**
 * Tests some of the innate attributes of filters.
 * 
 * @author skeggsc
 */
public class TestFilters extends BaseTest {

    @Override
    public String getName() {
        return "Filter Tests";
    }

    @Override
    protected void runTest() throws Throwable {
        testBooleanFailures(); // other aspects tested elsewhere; at least for BooleanFilters.
        testFloatFailures();
        testFloatFilters();
    }

    private void testBooleanFailures() throws TestingException {
        BooleanFilter f = new BooleanFilter() {
            @Override
            public boolean filter(boolean input) {
                return input;
            }
        };
        try {
            f.wrap((BooleanInputPoll) null);
            assertFail("Expected a NullPointerException from a null parameter!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            f.wrap((BooleanInput) null);
            assertFail("Expected a NullPointerException from a null parameter!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            f.wrap((BooleanOutput) null);
            assertFail("Expected a NullPointerException from a null parameter!");
        } catch (NullPointerException ex) {
            // correct!
        }
    }

    private void testFloatFailures() throws TestingException {
        FloatFilter f = new FloatFilter() {
            @Override
            public float filter(float input) {
                return input;
            }
        };
        try {
            f.wrap((FloatInputPoll) null);
            assertFail("Expected a NullPointerException from a null parameter!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            f.wrap((FloatInput) null);
            assertFail("Expected a NullPointerException from a null parameter!");
        } catch (NullPointerException ex) {
            // correct!
        }
        try {
            f.wrap((FloatOutput) null);
            assertFail("Expected a NullPointerException from a null parameter!");
        } catch (NullPointerException ex) {
            // correct!
        }
    }

    private void testFloatFilters() throws TestingException {
        final float inc = 2.03f;
        FloatFilter f = new FloatFilter() {
            @Override
            public float filter(float input) {
                return input + inc;
            }
        };

        FloatStatus stat = new FloatStatus(7);
        FloatStatus target = new FloatStatus(0);
        FloatInput input1 = f.wrap(stat.asInput());
        FloatInputPoll input2 = f.wrap((FloatInputPoll) stat.asInput());
        input1.send(target);

        for (float fv : new float[] {8.76f, 1.23f, -129123.4f}) {
            assertObjectEqual(stat.get() + inc, target.get(), "Expected same value!");
            assertObjectEqual(input1.get(), target.get(), "Expected same value!");
            assertObjectEqual(input2.get(), target.get(), "Expected same value!");
            stat.set(fv);
            assertObjectEqual(stat.get(), fv, "Expected same value!");
            assertObjectEqual(stat.get() + inc, target.get(), "Expected same value!");
            assertObjectEqual(input1.get(), target.get(), "Expected same value!");
            assertObjectEqual(input2.get(), target.get(), "Expected same value!");
            stat.set(fv);
            assertObjectEqual(stat.get(), fv, "Expected same value!");
            assertObjectEqual(stat.get() + inc, target.get(), "Expected same value!");
            assertObjectEqual(input1.get(), target.get(), "Expected same value!");
            assertObjectEqual(input2.get(), target.get(), "Expected same value!");
        }
        
        FloatOutput owrap = f.wrap(stat.asOutput());

        for (float fv : new float[] {8.76f, 1.23f, -129123.4f}) {
            owrap.set(fv);
            assertObjectEqual(stat.get(), fv + inc, "Expected same value!");
            assertObjectEqual(stat.get() + inc, target.get(), "Expected same value!");
            assertObjectEqual(input1.get(), target.get(), "Expected same value!");
            assertObjectEqual(input2.get(), target.get(), "Expected same value!");
            owrap.set(fv);
            assertObjectEqual(stat.get(), fv + inc, "Expected same value!");
            assertObjectEqual(stat.get() + inc, target.get(), "Expected same value!");
            assertObjectEqual(input1.get(), target.get(), "Expected same value!");
            assertObjectEqual(input2.get(), target.get(), "Expected same value!");
        }
    }
}
