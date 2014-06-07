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

import ccre.channel.EventOutput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.holders.StringHolder;
import ccre.log.Logger;

/**
 * A storage segment - a place to store various pieces of data. One of these can
 * be obtained using StorageProvider.
 *
 * @see StorageProvider
 * @author skeggsc
 */
public abstract class StorageSegment {

    /**
     * Get a String value for the specified key.
     *
     * @param key the key to look up.
     * @return the String contained there, or null if the key doesn't exist.
     */
    public abstract String getStringForKey(String key);

    /**
     * Set the string value behind the specified key.
     *
     * @param key the key to put the String under.
     * @param value the String to store under this key.
     */
    public abstract void setStringForKey(String key, String value);

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
        final String key = "string_holder_" + name;
        String dflt = "string_holder_default_" + name;
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
        holder.whenModified(new EventOutput() {
            public void event() {
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
        final String key = "float_holder_" + name, default_key = "float_holder_default_" + name;
        final float originalValue = holder.get();
        String vraw = getStringForKey(key);
        if (vraw != null) {
            try {
                float value = Float.parseFloat(vraw);
                String draw = getStringForKey(default_key);
                float default_ = draw == null ? Float.NaN : Float.parseFloat(draw);
                // If the default is the same as the holder's default, then load the value
                if (draw == null || Float.floatToIntBits(default_) == Float.floatToIntBits(originalValue)) {
                    Logger.config("Loaded config for " + name + ": def:" + default_ + " old:" + originalValue + " new:" + value);
                    holder.set(value);
                }
                // Otherwise, the default has changed from the holder, and therefore we want the updated value from the holder
            } catch (NumberFormatException ex) {
                Logger.warning("Invalid float value: '" + vraw + "'!", ex);
            }
        }
        holder.send(new SegmentFloatSaver(key, default_key, originalValue));
    }

    private class SegmentFloatSaver implements FloatOutput {

        private final String key, default_key;
        private final float originalValue;

        SegmentFloatSaver(String key, String dkey, float originalValue) {
            this.key = key;
            this.default_key = dkey;
            this.originalValue = originalValue;
        }

        public void set(float value) {
            setStringForKey(key, Float.toString(value));
            setStringForKey(default_key, Float.toString(originalValue));
        }
    }
}
