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
package ccre.saver;

import ccre.chan.FloatOutput;
import ccre.chan.FloatStatus;
import ccre.event.EventConsumer;
import ccre.holders.StringHolder;
import ccre.log.LogLevel;
import ccre.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A storage segment - a place to store various pieces of data. One of these can
 * be obtained using StorageProvider.
 *
 * @see StorageProvider
 * @author skeggsc
 */
public abstract class StorageSegment {

    /**
     * Get raw bytes for the specified key. The return array is expected to not
     * be modified! Create a copy if you need to change the data.
     *
     * @param key the key to look up.
     * @return the byte data contained there, or null if the key doesn't exist.
     */
    public abstract byte[] getBytesForKey(String key);

    /**
     * Get a String value for the specified key.
     *
     * @param key the key to look up.
     * @return the String contained there, or null if the key doesn't exist.
     */
    public String getStringForKey(String key) {
        byte[] byts = getBytesForKey(key);
        return byts == null ? null : new String(byts);
    }

    /**
     * Get a DataInputStream that will read from the specified key. This creates
     * a DataInputStream on a ByteArrayInputStream of the bytes stored in the
     * key.
     *
     * @param key the key to look up.
     * @return the DataInputStream to read from there, or null if the key
     * doesn't exist.
     */
    public DataInputStream getDataInputForKey(String key) {
        byte[] byts = getBytesForKey(key);
        if (byts == null) {
            return null;
        }
        return new DataInputStream(new ByteArrayInputStream(byts));
    }

    /**
     * Set the byte data behind the specified key. Note: this method assumes
     * that the byte array given will not be modified! Do not modify it after
     * passing it!
     *
     * @param key the key to put the bytes under.
     * @param bytes the bytes to store under this key.
     */
    public abstract void setBytesForKey(String key, byte[] bytes);

    /**
     * Set the string value behind the specified key.
     *
     * @param key the key to put the String under.
     * @param value the String to store under this key.
     */
    public void setStringForKey(String key, String value) {
        setBytesForKey(key, value.getBytes());
    }

    /**
     * Create a DataOutputStream to allow for easy writing of data to this key.
     * You must close this stream, or the data will not save!
     *
     * @param key the key to store data under.
     * @return the DataOutputStream to write data to.
     */
    // You must close the returned stream for the data to save!
    public DataOutputStream setDataOutputForKey(final String key) {
        return new DataOutputStream(new ByteArrayOutputStream() {
            @Override
            public void close() {
                setBytesForKey(key, toByteArray());
            }
        });
    }

    /**
     * Flush the segment. This attempts to make sure that all data is stored on
     * disk (or somewhere else, depending on the provider). If this is not
     * called, data might not be saved!
     */
    public abstract void flush();

    /**
     * Close the segment. This includes flushing the segment if applicable. The
     * segment may be unusable once this is called. Do not use the segment
     * afterwards.
     */
    public abstract void close();

    /**
     * Attach a StringHolder to this storage segment. This will restore data if
     * it has been stored as modified in the segment. This will save the data of
     * the string holder as it updates, although you may need to call flush() to
     * ensure that the data is saved.
     *
     * This will only overwrite the current value of the StringHolder if the
     * data was saved when the StringHolder had the same default (value when
     * this method is called). This means that you can modify the contents using
     * either the StorageSegment or by changing the StringHolder's original
     * value.
     *
     * @param name the name to save the holder under.
     * @param holder the holder to save.
     */
    public void attachStringHolder(String name, final StringHolder holder) {
        final String key = "$h:" + name;
        String dflt = "$h@" + name;
        String value = getStringForKey(key);
        if (value == null) {
            if (holder.hasModified()) {
                value = holder.get();
                setStringForKey(key, value);
                setStringForKey(dflt, value);
            }
        } else {
            String default_ = getStringForKey(dflt);
            // If the default is the same as the holder's default, or the holder doesn't have a value, then load the value
            if ((default_ != null && default_.equals(holder.get())) || !holder.hasModified()) {
                holder.set(value);
            }
            // Otherwise, the holder has been modified and the default has changed from the holder, and therefore we want the updated value from the holder
        }
        holder.whenModified(new EventConsumer() {
            public void eventFired() {
                setStringForKey(key, holder.get());
            }
        });
    }

    /**
     * Attach a FloatHolder to this storage segment. This will restore data if
     * it has been stored as modified in the segment. This will save the data of
     * the float holder as it updates, although you may need to call flush() to
     * ensure that the data is saved.
     *
     * This will only overwrite the current value of the FloatHolder if the data
     * was saved when the FloatHolder had the same default (value when this
     * method is called). This means that you can modify the contents using
     * either the StorageSegment or by changing the FloatHolder's original
     * value.
     *
     * @param name the name to save the holder under.
     * @param holder the holder to save.
     */
    public void attachFloatHolder(String name, final FloatStatus holder) {
        // TODO: Fix this up to remove the default field, when it won't break the robot code.
        final String key = "~h:" + name;
        DataInputStream din = getDataInputForKey(key);
        final float originalValue = holder.readValue();
        if (din != null) {
            try {
                float value = din.readFloat();
                if (din.readBoolean()) {
                    float default_ = din.readFloat();
                    // If the default is the same as the holder's default, then load the value
                    if (Float.floatToIntBits(default_) == Float.floatToIntBits(originalValue)) {
                        Logger.config("Loaded config for " + name + ": def:" + default_ + " old:" + originalValue + " new:" + value);
                        holder.writeValue(value);
                    }
                    // Otherwise, the default has changed from the holder, and therefore we want the updated value from the holder
                }
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Exception in self-contained float saving!", ex);
            }
        }
        holder.addTarget(new FloatOutput() {
            public void writeValue(float value) {
                DataOutputStream dout = setDataOutputForKey(key);
                try {
                    dout.writeFloat(value); // value
                    dout.writeBoolean(true); // has default
                    dout.writeFloat(originalValue); // default
                    dout.close();
                } catch (IOException ex) {
                    Logger.log(LogLevel.SEVERE, "Exception in self-contained float saving!", ex);
                }
            }
        });
    }
}
