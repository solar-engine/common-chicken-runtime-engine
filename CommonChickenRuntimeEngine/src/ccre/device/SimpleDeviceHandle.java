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

/**
 * An easy-to-use implementation of a DeviceHandle.
 *
 * @author skeggsc
 * @param <Type> The instance type.
 */
public abstract class SimpleDeviceHandle<Type> extends DeviceHandle<Type> {

    private Type activeInstance;

    protected abstract Type allocate();

    protected abstract void deallocate(Type target);

    @Override
    public Type open() throws DeviceException {
        if (activeInstance == null) {
            return activeInstance = allocate();
        } else {
            throw new DeviceException("Device already open!");
        }
    }

    @Override
    public void close(Object target) {
        if (activeInstance == null) {
            throw new IllegalStateException("Device already closed!");
        }
        if (activeInstance != target) {
            throw new IllegalArgumentException("Cannot close invalid value!");
        }
        deallocate(activeInstance);
    }
}
