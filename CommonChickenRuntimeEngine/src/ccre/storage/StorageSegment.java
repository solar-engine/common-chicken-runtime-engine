/*
 * Copyright 2013-2016 Cel Skeggs
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
package ccre.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;

import ccre.channel.BooleanCell;
import ccre.channel.FloatCell;
import ccre.log.Logger;
import ccre.util.UniqueIds;
import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

/**
 * A storage segment - a place to store various pieces of data. A StorageSegment
 * can be obtained using StorageProvider.
 */
public final class StorageSegment {

    private final HashMap<String, String> data = new HashMap<String, String>();
    private String name;
    private boolean modified = false;

    /**
     * Load a map from a properties-like file.
     * 
     * @param input the InputStream to read from.
     * @param keepInvalidLines whether or not to save invalid lines under backup
     * keys.
     * @param target the map to put the loaded keys into.
     * @throws IOException if reading from the input fails for some reason.
     */
    @SetupPhase
    public static void loadProperties(InputStream input, boolean keepInvalidLines, HashMap<String, String> target) throws IOException {
        BufferedReader din = new BufferedReader(new InputStreamReader(input));
        try {
            while (true) {
                String line = din.readLine();
                if (line == null) {
                    break;
                }
                int ind = line.indexOf('=');
                if (ind == -1) {// Invalid or empty line.
                    if (!line.isEmpty() && keepInvalidLines) {
                        Logger.warning("Invalid line ignored in configuration: " + line + " - saving under backup key.");
                        target.put(UniqueIds.global.nextHexId("unknown-" + System.currentTimeMillis() + "-" + line.hashCode()), line);
                    }
                    continue;
                }
                String key = line.substring(0, ind), value = line.substring(ind + 1);
                target.put(key, value);
            }
        } finally {
            din.close();
        }
    }

    StorageSegment(String unescaped_name) {
        if (unescaped_name == null) {
            throw new NullPointerException("Storage names cannot be null");
        }
        StringBuilder buf = new StringBuilder(unescaped_name);
        for (int i = buf.length() - 1; i >= 0; i--) {
            char c = buf.charAt(i);
            if (c == ' ') {
                buf.setCharAt(i, '_');
            } else if (!(Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isDigit(c))) {
                // escape any "weird" characters
                buf.setCharAt(i, '$');
                buf.insert(i + 1, (int) c);
            }
        }
        this.name = buf.toString();
        try {
            InputStream target = Storage.openInput("ccre_storage_" + name);
            if (target == null) {
                Logger.info("No data file for: " + name + " - assuming empty.");
            } else {
                try {
                    loadProperties(target, true, data);
                } finally {
                    target.close();
                }
            }
        } catch (IOException ex) {
            Logger.warning("Error reading storage: " + name, ex);
        }
    }

    /**
     * Get a String value for the specified key.
     *
     * @param key the key to look up.
     * @return the String contained there, or null if the key doesn't exist.
     */
    @SetupPhase
    public synchronized String getStringForKey(String key) {
        return data.get(key);
    }

    /**
     * Set the string value behind the specified key.
     *
     * @param key the key to put the String under.
     * @param value the String to store under this key.
     */
    @FlowPhase
    public synchronized void setStringForKey(String key, String value) {
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
        modified = true;
    }

    /**
     * Flush the segment. This attempts to make sure that all data is stored on
     * disk (or somewhere else, depending on the provider). If this is not
     * called, data might not be saved!
     */
    @SetupPhase
    public synchronized void flush() {
        if (modified) {
            try {
                PrintStream pout = new PrintStream(Storage.openOutput("ccre_storage_" + name));
                try {
                    for (String key : data.keySet()) {
                        if (key.contains("=")) {
                            Logger.warning("Invalid key ignored during save: " + key + " - saving under backup key.");
                            data.put(UniqueIds.global.nextHexId("badkey-" + System.currentTimeMillis() + "-" + key.hashCode()), key);
                        } else {
                            String value = data.get(key);
                            if (value != null) {
                                pout.println(key + "=" + value);
                            }
                        }
                    }
                } finally {
                    pout.close();
                }
            } catch (IOException ex) {
                Logger.warning("Error writing storage: " + name, ex);
            }
            modified = false;
        }
    }

    /**
     * Get the name of this segment, if available.
     *
     * @return the segment's name, or null if none exists.
     */
    public String getName() {
        return name;
    }

    /**
     * Attach a FloatHolder to this storage segment. This will restore data if
     * it has been stored as modified in the segment. This will save the data of
     * the float holder as it updates, although you will need to call flush() to
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
    @SetupPhase
    public void attachFloatHolder(String name, final FloatCell holder) {
        final String key = "float_holder_" + name, default_key = "float_holder_default_" + name;
        final float originalValue = holder.get();
        String vraw = getStringForKey(key);
        if (vraw != null) {
            try {
                float value = Float.parseFloat(vraw);
                String draw = getStringForKey(default_key);
                float default_ = draw == null ? Float.NaN : Float.parseFloat(draw);
                // If the default is the same as the holder's default, then load
                // the value
                if (draw == null || Float.floatToIntBits(default_) == Float.floatToIntBits(originalValue)) {
                    Logger.config("Loaded config for " + name + ": def:" + default_ + " old:" + originalValue + " new:" + value);
                    holder.set(value);
                }
                // Otherwise, the default has changed from the holder, and
                // therefore we want the updated value from the holder
            } catch (NumberFormatException ex) {
                Logger.warning("Invalid float value: '" + vraw + "'!", ex);
            }
        }
        holder.send((value) -> {
            setStringForKey(key, Float.toString(value));
            setStringForKey(default_key, Float.toString(originalValue));
        });
    }

    /**
     * Attach a BooleanHolder to this storage segment. This will restore data if
     * it has been stored as modified in the segment. This will save the data of
     * the boolean holder as it updates, although you will need to call flush()
     * to ensure that the data is saved.
     *
     * This will only overwrite the current value of the BooleanHolder if the
     * data was saved when the BooleanHolder had the same default (value when
     * this method is called). This means that you can modify the contents using
     * either the StorageSegment or by changing the BooleanHolder's original
     * value.
     *
     * @param name the name to save the holder under.
     * @param holder the holder to save.
     */
    @SetupPhase
    public void attachBooleanHolder(String name, final BooleanCell holder) {
        final String key = "boolean_holder_" + name, default_key = "boolean_holder_default_" + name;
        final boolean originalValue = holder.get();
        String vraw = getStringForKey(key);
        if (vraw != null) {
            try {
                boolean value = Boolean.parseBoolean(vraw);
                String draw = getStringForKey(default_key);
                // If the default is the same as the holder's default, then load
                // the value
                if (draw == null || Boolean.parseBoolean(draw) == originalValue) {
                    Logger.config("Loaded config for " + name + ": def:" + draw + " old:" + originalValue + " new:" + value);
                    holder.set(value);
                }
                // Otherwise, the default has changed from the holder, and
                // therefore we want the updated value from the holder
            } catch (NumberFormatException ex) {
                Logger.warning("Invalid boolean value: '" + vraw + "'!", ex);
            }
        }
        holder.send((value) -> {
            setStringForKey(key, Boolean.toString(value));
            setStringForKey(default_key, Boolean.toString(originalValue));
        });
    }
}
