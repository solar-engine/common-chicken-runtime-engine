/*
 * Copyright 2014 Colby Skeggs
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

import ccre.util.CHashMap;

/**
 * A fake implementation of ReflectionEngine. Doesn't actually work, but is
 * provided when there's no real implementation.
 *
 * @author skeggsc
 */
public class FakeReflectionEngine extends ReflectionEngine {

    @Override
    protected void complete(CHashMap<String, Integer> map) {
        // Nothing!
    }

    @Override
    public Object dispatch(int uid, Object self, Object[] args) throws Throwable {
        throw new RuntimeException("No dispatches in a FakeReflectionEngine!");
    }

    @Override
    public String getSuperclass(String name) {
        return null;
    }
}
