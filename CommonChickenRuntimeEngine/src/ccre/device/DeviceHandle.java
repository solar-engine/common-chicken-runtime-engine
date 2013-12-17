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
package ccre.device;

import ccre.chan.*;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.util.CArrayUtils;
import ccre.util.Utils;

/**
 * A handle representing a specific peripheral device.
 *
 * @author skeggsc
 */
public abstract class DeviceHandle<Type> {

    public abstract Class<Type> getPrimaryDeviceType();

    protected boolean hasSecondaryUses() {
        return false;
    }

    public abstract Type open() throws DeviceException;
    
    public Iterable<String> getConfigs() {
        return CArrayUtils.getEmptyList();
    }
    
    public void configure(String key, String value) throws DeviceException {
        throw new DeviceException("No such configuration key: " + key);
    }

    public abstract void close(Type target);

    public <Target> Target open(Class<Target> target) throws DeviceException {
        if (!hasSecondaryUses()) {
            if (target.isAssignableFrom(getPrimaryDeviceType())) {
                Object out = open();
                if (target.isInstance(out)) {
                    return Utils.dynamicCast(out, target);
                } else {
                    throw new DeviceException("Device is dynamically not: " + target.getName());
                }
            } else {
                throw new DeviceException("Device is statically not: " + target.getName());
            }
        } else {
            Type out = open();
            if (target.isInstance(out)) {
                return Utils.dynamicCast(out, target);
            } else {
                close(out);
                throw new DeviceException("Device is not: " + target.getName());
            }
        }
    }

    public EventConsumer openEventConsumer() throws DeviceException {
        return open(EventConsumer.class);
    }

    public EventSource openEventSource() throws DeviceException {
        return open(EventSource.class);
    }

    public BooleanOutput openBooleanOutput() throws DeviceException {
        return open(BooleanOutput.class);
    }

    public BooleanInputPoll openBooleanInputPoll() throws DeviceException {
        return open(BooleanInputPoll.class);
    }

    public BooleanInputProducer openBooleanInputProducer() throws DeviceException {
        return open(BooleanInputProducer.class);
    }

    public BooleanInput openBooleanInput() throws DeviceException {
        return open(BooleanInput.class);
    }

    public FloatOutput openFloatOutput() throws DeviceException {
        return open(FloatOutput.class);
    }

    public FloatInputPoll openFloatInputPoll() throws DeviceException {
        return open(FloatInputPoll.class);
    }

    public FloatInputProducer openFloatInputProducer() throws DeviceException {
        return open(FloatInputProducer.class);
    }

    public FloatInput openFloatInput() throws DeviceException {
        return open(FloatInput.class);
    }
}
