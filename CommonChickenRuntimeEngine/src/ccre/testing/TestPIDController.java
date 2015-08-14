/*
 * Copyright 2015 Colby Skeggs.
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

import ccre.channel.EventStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatStatus;
import ccre.ctrl.FloatMixing;
import ccre.ctrl.PIDController;

/**
 * Test the PIDController class.
 *
 * @author skeggsc
 */
public class TestPIDController extends BaseTest {

    @Override
    public String getName() {
        return "PID Controller Test";
    }

    @Override
    protected void runTest() throws Throwable {
        EventStatus updateOn = new EventStatus();
        FloatStatus currentValue = new FloatStatus();
        FloatStatus setPoint = new FloatStatus();
        FloatInput delta = FloatMixing.subtraction.of(setPoint.asInput(), currentValue.asInput());
        float P = 1, I = 0.1f, D = 0.01f;
        PIDController ppid = PIDController.createFixed(updateOn, currentValue, setPoint, P, 0, 0);
        PIDController ipid = PIDController.createFixed(updateOn, currentValue, setPoint, 0, I, 0);
        PIDController dpid = PIDController.createFixed(updateOn, currentValue, setPoint, 0, 0, D);
        PIDController merged = PIDController.createFixed(updateOn, currentValue, setPoint, P, I, D);
        PIDController shifted = new PIDController(FloatMixing.subtraction.of(setPoint.asInput(), currentValue.asInput()),
                FloatMixing.always(P), FloatMixing.always(I), FloatMixing.always(D));
        PIDController chopped = PIDController.createFixed(updateOn, currentValue, setPoint, P, I, D);
        float integralBounds = 0.05f;
        ipid.setIntegralBounds(integralBounds);
        merged.setIntegralBounds(integralBounds);
        shifted.setIntegralBounds(FloatMixing.always(integralBounds));
        chopped.setIntegralBounds(integralBounds);

        float outputBounds = 1.1f;
        merged.setOutputBounds(outputBounds);
        shifted.setOutputBounds(FloatMixing.always(outputBounds));
        chopped.setOutputBounds(outputBounds);

        final int millis = 20;
        chopped.setMaximumTimeDelta(millis / 1000f);
        
        FloatStatus reflection = new FloatStatus();
        merged.send(reflection);

        float lastSample = 0, lastIntegral = ipid.integralTotal.get();
        float lastMergedResult = 0;
        for (float sp : new float[] { 10, -5, 0, 3 }) {
            boolean lastCycle = (sp == 3);
            if (lastCycle) {
                merged.unsend(reflection);
            }
            setPoint.set(sp);
            reflection.set(Float.NaN); // so we know when it gets updated
            for (int i = 0; i < 1000; i++) {
                currentValue.set(currentValue.get() + lastMergedResult * 0.05f);
                ppid.update(millis);
                ipid.update(millis);
                dpid.update(millis);
                merged.update(millis);
                shifted.update(millis);
                chopped.update(millis * 37); // update a lot more - but it'll get CHOPPED
                //System.out.println("Step " + sp + "/" + i + ": " + currentValue.get() + ": " + ppid.get() + " + " + ipid.get() + " + " + dpid.get() + " = " + merged.get());
                float inttotal = lastIntegral + (millis / 1000f) * delta.get();
                if (inttotal < -integralBounds) {
                    inttotal = -integralBounds;
                } else if (inttotal > integralBounds) {
                    inttotal = integralBounds;
                }
                assertFloatsNear(ipid.integralTotal.get(), inttotal, "integral total mismatch");
                assertFloatsNear(ppid.get(), P * delta.get(), "proportional mismatch");
                assertFloatsNear(ipid.get(), I * ipid.integralTotal.get(), "integral mismatch");
                assertFloatsNear(dpid.get(), D * (delta.get() - lastSample) * (1000f / millis), "derivative mismatch");
                float expectedMerged = ppid.get() + ipid.get() + dpid.get();
                if (expectedMerged < -outputBounds) {
                    expectedMerged = -outputBounds;
                } else if (expectedMerged > outputBounds) {
                    expectedMerged = outputBounds;
                }
                assertFloatsNear(expectedMerged, merged.get(), "mismatched merged result");
                assertFloatsNear(merged.get(), shifted.get(), "mismatched shifted result");
                assertFloatsNear(merged.get(), chopped.get(), "mismatched chopped result");
                if (!lastCycle) {
                    assertFalse(Float.isNaN(reflection.get()), "expected reflection update");
                    assertFloatsNear(merged.get(), reflection.get(), "mismatched reflection result");
                } else {
                    // because we unsent from merged and set it to NaN
                    assertTrue(Float.isNaN(reflection.get()), "expected no reflection update");
                }
                lastSample = delta.get();
                lastIntegral = ipid.integralTotal.get();
                lastMergedResult = merged.get();
            }
            assertFloatsNear(currentValue.get(), sp, 10, "PID controller did not reach setpoint");

            // Exceptional cases
            float oldValue = currentValue.get();
            float curTotal = merged.integralTotal.get();
            float prevErr = merged.getPreviousError();

            try {
                merged.update(-1);
                assertFail("Expected an IllegalArgumentException!");
            } catch (IllegalArgumentException ex) {
                // correct!
            }

            currentValue.set(Float.NaN);
            merged.update(millis);
            assertTrue(Float.isNaN(merged.get()), "expected NaN output!");
            currentValue.set(Float.POSITIVE_INFINITY);
            merged.update(millis);
            assertTrue(Float.isNaN(merged.get()), "expected NaN output!");
            currentValue.set(Float.NEGATIVE_INFINITY);
            merged.update(millis);
            assertTrue(Float.isNaN(merged.get()), "expected NaN output!");

            currentValue.set(oldValue * 300); // something that will throw off the whole thing... if it gets used
            merged.update(0); // which it won't
            assertObjectEqual(merged.integralTotal.get(), curTotal, "update(0) affected integral total!");
            assertObjectEqual(merged.getPreviousError(), prevErr, "update(0) affected error!");

            currentValue.set(oldValue);
        }
    }
}
