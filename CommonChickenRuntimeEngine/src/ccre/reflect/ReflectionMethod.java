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

import ccre.util.Utils;

public class ReflectionMethod {

    public final String fullName; // full path, including class name.
    public final Class[][] paramTypes;
    public final Class[] results;
    public final int[] invokeIds;
    public final ReflectionEngine engine;
    
    public ReflectionMethod(String fullName, Class[][] paramTypes, Class[] results, int[] invokeIds, ReflectionEngine engine) {
        if (results.length != invokeIds.length || invokeIds.length != paramTypes.length) {
            throw new RuntimeException();
        }
        this.fullName = fullName;
        this.paramTypes = paramTypes;
        this.results = results;
        this.invokeIds = invokeIds;
        this.engine = engine;
    }

    public ReflectionMethod(String serialized, ReflectionEngine engine) throws NumberFormatException, ClassNotFoundException {
        String[] spt = Utils.split(serialized, '|');
        fullName = spt[0];
        int invokeCount = spt.length - 1;
        paramTypes = new Class[invokeCount][];
        results = new Class[invokeCount];
        invokeIds = new int[invokeCount];
        this.engine = engine;
        for (int i=0; i<invokeCount; i++) {
            String[] parts = Utils.split(spt[i+1], '~');
            invokeIds[i] = Integer.parseInt(parts[0]);
            results[i] = Class.forName(parts[1]);
            paramTypes[i] = new Class[parts.length - 2];
            for (int j=2; j<parts.length; j++) {
                paramTypes[i][j-2] = Class.forName(parts[j]);
            }
        }
    }
    
    public String serialize() {
        StringBuffer out = new StringBuffer();
        out.append(fullName);
        for (int i=0; i<results.length; i++) {
            out.append('|').append(invokeIds[i]).append('~').append(results[i].getName());
            for (int j=0; j<paramTypes[i].length; j++) {
                out.append('~').append(paramTypes[i][j].getName());
            }
        }
        return out.toString();
    }

    public Object invoke(Object aThis, Object... params) throws Throwable {
        // TODO: Won't work propertly if multiple signatures match!
        int id = -1;
        for (Class[] signature : paramTypes) {
            if (signature.length != params.length) {
                continue;
            }
            for (int i = 0; i < params.length; i++) {
                if (params[i] == null || signature[i].isInstance(params[i])) {
                    if (id != -1) {
                        throw new RuntimeException("Not unique!");
                    }
                    id = invokeIds[i];
                }
            }
        }
        if (id == -1) {
            throw new IllegalArgumentException("Cannot find invoke!");
        }
        return engine.invoke(id, aThis, params);
    }
}
