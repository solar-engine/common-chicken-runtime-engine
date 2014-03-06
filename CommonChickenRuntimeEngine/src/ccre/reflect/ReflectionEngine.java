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

    private CHashMap<String, Integer> mapping;
    private static ReflectionEngine instance;

    public static ReflectionEngine getInstance() {
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

    public synchronized Integer lookup(String symbolname) {
        if (mapping == null) {
            mapping = new CHashMap<String, Integer>();
            complete(mapping);
        }
        return mapping.get(symbolname);
    }
    
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
    
    public synchronized Iterable<String> getSymbolIterable() {
        if (mapping == null) {
            mapping = new CHashMap<String, Integer>();
            complete(mapping);
        }
        return mapping;
    }

    protected abstract void complete(CHashMap<String, Integer> map);
    
    public abstract String getSuperclass(String name);

    public abstract Object dispatch(int uid, Object self, Object[] args) throws Throwable;
}
