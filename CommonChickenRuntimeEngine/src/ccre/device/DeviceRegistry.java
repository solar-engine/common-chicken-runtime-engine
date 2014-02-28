/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.util.CHashMap;
import java.util.Iterator;

/**
 * A DeviceRegistry is an abstract representation of all the devices on a robot
 * or system.
 *
 * @author skeggsc
 */
public class DeviceRegistry implements Iterable<String> {

    protected final CHashMap<String, DeviceHandle<? extends Object>> devices = new CHashMap<String, DeviceHandle<? extends Object>>();

    public <Type> void putHandle(String path, DeviceHandle<Type> hndl) throws DeviceException {
        if (devices.get(path) != null) {
            throw new DeviceException("Device already registered!");
        }
        devices.put(path, hndl);
    }

    public <Type> void putSimple(String path, final Type device, final Class<Type> type) throws DeviceException {
        DeviceHandle<Type> h = new SimpleDeviceHandle<Type>() {
            @Override
            protected Type allocate() {
                return device;
            }

            @Override
            protected void deallocate(Type target) {
            }

            @Override
            public Class<Type> getPrimaryDeviceType() {
                return type;
            }
        };
        putHandle(path, h);
    }

    public DeviceHandle<? extends Object> getHandle(String path) throws DeviceException {
        DeviceHandle<? extends Object> handle = devices.get(path);
        if (handle == null) {
            throw new DeviceException("No such device: " + path);
        }
        return handle;
    }

    public Iterator<String> iterator() {
        return devices.iterator();
    }
}
