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
package ccre.reflect;

import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CHashMap;

/**
 * A base reflection engine.
 *
 * @author skeggsc
 */
public abstract class ReflectionEngine {

    /**
     * The mapping between symbol names and indexes to invoke them.
     */
    private CHashMap<String, Integer> mapping;
    /**
     * The global Reflection Engine instance.
     */
    private static ReflectionEngine instance;

    /**
     * Get an instance of the global reflection engine.
     *
     * @return The global reflection engine.
     */
    public static synchronized ReflectionEngine getInstance() {
        if (instance == null) {
            try {
                instance = (ReflectionEngine) Class.forName("ccre.reflect.ReflectionEngineImpl").newInstance();
            } catch (ClassNotFoundException ex) {
                Logger.log(LogLevel.WARNING, "Could not start Reflection Engine!", ex);
            } catch (InstantiationException ex) {
                Logger.log(LogLevel.WARNING, "Could not start Reflection Engine!", ex);
            } catch (IllegalAccessException ex) {
                Logger.log(LogLevel.WARNING, "Could not start Reflection Engine!", ex);
            }
            if (instance == null) {
                instance = new FakeReflectionEngine();
            }
        }
        return instance;
    }

    /**
     * Look up the invocation index for the specified symbol name.
     *
     * @param symbolname The name to look up.
     * @return The index, or null if it cannot be found.
     */
    public synchronized Integer lookup(String symbolname) {
        if (mapping == null) {
            mapping = new CHashMap<String, Integer>();
            complete(mapping);
        }
        return mapping.get(symbolname);
    }

    /**
     * Look up the symbol name for the specified invocation index.
     *
     * @param number The index to look up.
     * @return The symbol name, or null if it cannot be found.
     */
    public synchronized String reverseLookup(int number) {
        if (mapping == null) {
            mapping = new CHashMap<String, Integer>();
            complete(mapping);
        }
        for (String ent : mapping) {
            if (mapping.get(ent).intValue() == number) {
                return ent;
            }
        }
        return null;
    }

    /**
     * Get an Iterable over the list of symbol names.
     *
     * @return The list of symbol names.
     */
    public synchronized Iterable<String> getSymbolIterable() {
        if (mapping == null) {
            mapping = new CHashMap<String, Integer>();
            complete(mapping);
        }
        return mapping;
    }

    /**
     * Overridden by the engine implementation to fill out a map of symbol names
     * and invocation indexes.
     *
     * @param map The map to complete.
     */
    protected abstract void complete(CHashMap<String, Integer> map);

    /**
     * Get the superclass for the specified named class.
     *
     * @param name The class to check.
     * @return The superclass.
     */
    public abstract String getSuperclass(String name);

    /**
     * Invoke an indexed symbol using this engine, with the specified invocation
     * index, THIS object, and arguments.
     *
     * @param uid The invocation index.
     * @param self The this object.
     * @param args The arguments.
     * @return The result from the invocation.
     * @throws Throwable If any errors occur.
     */
    public abstract Object dispatch(int uid, Object self, Object[] args) throws Throwable;
}
