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

    private CHashMap<String, ReflectionMethod> mapping;
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
                throw new RuntimeException("No reflection engine!");
            }
        }
        return instance;
    }

    public ReflectionMethod lookup(Class c, String methodName) {
        CHashMap<String, ReflectionMethod> lmap = mapping;
        if (lmap == null) {
            synchronized (this) {
                lmap = mapping;
                if (lmap == null) {
                    lmap = mapping = new CHashMap<String, ReflectionMethod>();
                    fillLookup();
                }
            }
        }
        ReflectionMethod out = lmap.get(c.getName() + "." + methodName);
        if (out == null) {
            throw new IllegalArgumentException("No such method!");
        }
        return out;
    }

    protected abstract void fillLookup();

    protected void fillLookup(String serialized) {
        try {
            ReflectionMethod m = new ReflectionMethod(serialized, this);
            mapping.put(m.fullName, m);
        } catch (ClassNotFoundException e) {
            Logger.log(LogLevel.WARNING, "Missing class in reflection engine!", e);
        } catch (NumberFormatException e) {
            Logger.log(LogLevel.WARNING, "Invalid number in reflection engine!", e);
        }
    }

    public abstract Object invoke(int id, Object aThis, Object[] args) throws Throwable;
}
