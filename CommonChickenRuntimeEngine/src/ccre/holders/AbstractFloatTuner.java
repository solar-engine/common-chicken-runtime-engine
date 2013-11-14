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

import ccre.chan.FloatOutput;
import ccre.util.CArrayList;

/**
 * An AbstractFloatTuner is an abstract class that allows for easy
 * implementation of a FloatTuner.
 *
 * @author skeggsc
 */
public abstract class AbstractFloatTuner implements FloatTuner {

    protected CArrayList<FloatOutput> consumers = new CArrayList<FloatOutput>();

    protected void notifyConsumers() {
        for (FloatOutput o : consumers) {
            o.writeValue(getCurrentValue());
        }
    }

    @Override
    public float readValue() {
        return getCurrentValue();
    }

    @Override
    public void addTarget(FloatOutput output) {
        consumers.add(output);
        output.writeValue(readValue());
    }

    @Override
    public boolean removeTarget(FloatOutput output) {
        return consumers.remove(output);
    }

    @Override
    public void writeValue(float newValue) {
        tuneTo(newValue);
    }
}
