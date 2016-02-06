/*
 * Copyright 2016 Colby Skeggs
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
package ccre.frc.devices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import ccre.channel.BooleanIO;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatIO;
import ccre.channel.FloatOutput;
import ccre.frc.Device;
import ccre.frc.DeviceGroup;
import ccre.frc.DeviceListPanel;

/**
 * A virtual device, with helper functions for caching subscription results.
 *
 * @author skeggsc
 */
public class KeyedSubDevice extends DeviceGroup implements Disableable {

    /**
     * Gets the BooleanControlDevice for the given name, or creates it if it
     * doesn't exist.
     *
     * @param name the unique name of this control.
     * @return the fetched or created control.
     */
    protected BooleanIO getBooleanInput(String name) {
        return getOrCreate(BooleanIO.class, name, () -> add(new BooleanControlDevice(name)).asIO());
    }

    /**
     * Gets the FloatControlDevice for the given name, or creates it if it
     * doesn't exist.
     *
     * @param name the unique name of this control.
     * @return the fetched or created control.
     */
    protected FloatIO getFloatInput(String name) {
        return getOrCreate(FloatIO.class, name, () -> add(new FloatControlDevice(name)).asIO());
    }

    /**
     * Gets the SpinDevice for the given name, or creates it if it doesn't
     * exist.
     *
     * @param name the unique name of this control.
     * @return the fetched or created control.
     */
    protected FloatIO getFloatInputSpinner(String name) {
        return getOrCreate(FloatIO.class, name, () -> add(new SpinDevice(name, null)).asIO());
    }

    /**
     * Gets the BooleanViewDevice for the given name, or creates it if it
     * doesn't exist.
     *
     * @param name the unique name of this control.
     * @return the fetched or created control.
     */
    protected BooleanOutput getBooleanOutput(String name) {
        return getOrCreate(BooleanOutput.class, name, () -> add(new BooleanViewDevice(name)));
    }

    /**
     * Gets the FloatViewDevice for the given name, or creates it if it doesn't
     * exist.
     *
     * @param name the unique name of this control.
     * @return the fetched or created control.
     */
    protected FloatOutput getFloatOutput(String name) {
        return getOrCreate(FloatOutput.class, name, () -> add(new FloatViewDevice(name)));
    }

    private synchronized <T> T getOrCreate(Class<T> cls, String name, Supplier<T> init) {
        Map<String, T> ht = getClassMap(cls);
        T out = ht.get(name);
        if (out == null) {
            out = cls.cast(init.get());
            ht.put(name, out);
        }
        return out;
    }

    private <T> Map<String, T> getClassMap(Class<T> cls) {
        HashMap<String, ?> map = cmap.get(cls);
        if (map == null) {
            map = new HashMap<>();
            cmap.put(cls, map);
        }
        return convertMap(map, String.class, cls);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> convertMap(Map<?, ?> map, Class<K> key, Class<V> value) {
        return Collections.checkedMap((Map<K, V>) map, key, value);
    }

    private HashMap<Class<?>, HashMap<String, ?>> cmap = new HashMap<>();
    private DeviceListPanel master;
    private boolean wasAddedToMaster = false;

    /**
     * Creates a new CANTalonDevice described as name with a specified
     * DeviceListPanel to contain this device.
     *
     * Make sure to call addToMaster - don't add this directly.
     *
     * @param name how to describe the CANTalonDevice.
     * @param master the panel that contains this.
     * @see #addToMaster()
     */
    public KeyedSubDevice(String name, DeviceListPanel master) {
        add(new HeadingDevice(name));
        this.master = master;
    }

    /**
     * Add this device to its master panel, or do nothing if this has already
     * been done.
     *
     * @return this KeyedSubDevice, for method chaining purposes.
     */
    public synchronized KeyedSubDevice addToMaster() {
        if (!wasAddedToMaster) {
            wasAddedToMaster = true;
            master.add(this);
        }
        return this;
    }

    @Override
    public void notifyDisabled(boolean disabled) {
        for (Device d : this) {
            if (d instanceof Disableable) {
                ((Disableable) d).notifyDisabled(disabled);
            }
        }
    }
}
