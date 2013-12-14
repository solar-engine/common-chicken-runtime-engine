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

import ccre.util.CHashMap;
import ccre.util.Utils;
import java.util.Iterator;

/**
 * A DeviceTree is an abstract representation of all the devices on a device.
 *
 * @author skeggsc
 */
public class DeviceTree implements Iterable<String> {

    protected final CHashMap<String, DeviceHandle> devices = new CHashMap<String, DeviceHandle>();
    protected final CHashMap<String, DeviceFilter> filters = new CHashMap<String, DeviceFilter>();

    public void putFilter(String suffix, DeviceFilter filter) throws DeviceException {
        if (filters.get(suffix) != null) {
            throw new DeviceException("Filter already registered!");
        }
        filters.put(suffix, filter);
    }
    
    /*public void putDefaultFilters(final EventSource defaultCycleEvent) throws DeviceException {
        putFilter("pressed", new DeviceFilter<Object, EventSource>() { // Object: Either BooleanInputPoll or BooleanInputProducer
            public DeviceHandle<EventSource> filter(final DeviceHandle<Object> h) {
                return new DeviceHandle<EventSource>() {
                    protected Event e = new Event();
                    
                    @Override
                    public Class<EventSource> getPrimaryDeviceType() {
                        return EventSource.class;
                    }

                    @Override
                    public EventSource open() throws DeviceException {
                        Object target = h.open();
                        if (target instanceof BooleanInputProducer) {
                            return Mixing.whenBooleanBecomes((BooleanInputProducer) target, true);
                        } else if (target instanceof BooleanInputPoll) {
                            defaultCycleEvent.addListener(e);
                            return Mixing.whenBooleanBecomes((BooleanInputPoll) target, true, e);
                        } else {
                            h.close(target);
                            throw new DeviceException("Cannot compute pressed for: " + target.getClass());
                        }
                    }

                    @Override
                    public void close(EventSource target) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };
            }
        });
    }*/

    public void putHandle(String path, DeviceHandle handle) throws DeviceException {
        if (devices.get(path) != null) {
            throw new DeviceException("Device already registered!");
        }
        devices.put(path, handle);
    }

    public <Type> void putSimple(String path, final Type device) throws DeviceException {
        putHandle(path, new SimpleDeviceHandle<Type>() {
            @Override
            protected Type allocate() {
                return device;
            }

            @Override
            protected void deallocate(Type target) {
            }

            @Override
            public Class<Type> getPrimaryDeviceType() {
                return (Class<Type>) device.getClass();
            }
        });
    }

    public DeviceHandle getHandle(String path) throws DeviceException {
        DeviceHandle handle = devices.get(path);
        if (handle == null) {
            String[] parts = Utils.split(path, ':');
            if (parts.length > 1) {
                handle = devices.get(parts[0]);
                if (handle != null) {
                    for (int i = 1; i < parts.length; i++) {
                        DeviceFilter f = filters.get(parts[i]);
                        if (f == null) {
                            handle = null;
                            break;
                        }
                        handle = f.filter(handle);
                    }
                    if (handle != null) {
                        return handle;
                    }
                }
            }
            throw new DeviceException("No such device: " + path);
        }
        return handle;
    }

    public Iterator<String> iterator() {
        return devices.iterator();
    }
}
