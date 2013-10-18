/*
 * Copyright 2013 Colby Skeggs
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
package ccre.holders;

import ccre.chan.FloatInput;
import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;

/**
 * A compound class that combines a FloatInput and a FloatOutput to create a
 * FloatTuner. Can also take an optional channel for autotuning.
 *
 * @author skeggsc
 */
public class CompoundFloatTuner implements FloatTuner {

    /**
     * The input used by this implementation.
     */
    public FloatInput in;
    /**
     * The output used by this implementation.
     */
    public FloatOutput out;
    /**
     * The automatic tuning channel used by this implementation.
     */
    public FloatInput auto;

    /**
     * Create a new CompoundFloatTuner from the specified input and output and
     * no autotuning channel.
     *
     * @param in The input to use.
     * @param out The output to use.
     */
    public CompoundFloatTuner(FloatInput in, FloatOutput out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Create a new CompoundFloatTuner from the specified input, output and
     * autotuning channel.
     *
     * @param in The input to use.
     * @param out The output to use.
     * @param auto The autotuning input.
     */
    public CompoundFloatTuner(FloatInput in, FloatOutput out, FloatInput auto) {
        this.in = in;
        this.out = out;
        this.auto = auto;
    }

    public FloatInputProducer getAutomaticChannel() {
        return auto;
    }

    public Float getCurrentValue() {
        return in.readValue();
    }

    public void tuneTo(float newValue) {
        out.writeValue(newValue);
    }

    public float readValue() {
        return in.readValue();
    }

    public void addTarget(FloatOutput output) {
        in.addTarget(output);
    }

    public boolean removeTarget(FloatOutput output) {
        return in.removeTarget(output);
    }

    public void writeValue(float value) {
        out.writeValue(value);
    }
}
