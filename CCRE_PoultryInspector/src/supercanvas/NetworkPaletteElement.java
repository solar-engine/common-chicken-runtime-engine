/*
 * Copyright 2014 Colby Skeggs.
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
package supercanvas;

import ccre.cluck.CluckNode;
import java.io.Serializable;

/**
 * An element of a NetworkPaletteComponent.
 *
 * @author skeggsc
 */
class NetworkPaletteElement implements PaletteEntry, Serializable, Comparable<NetworkPaletteElement> {

    final String name;
    final int type;
    final Object target;

    NetworkPaletteElement(String name, Object target, int type) {
        this.name = name;
        this.target = target;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SuperCanvasComponent fetch(int x, int y) {
        SuperCanvasComponent out = NetworkPaletteComponent.createComponent(name, target, type, x, y);
        if (out == null) {
            return new FictionalComponent(x, y, name, CluckNode.rmtToString(type));
        }
        return out;
    }

    @Override
    public int compareTo(NetworkPaletteElement o) {
        return name.compareTo(o.name);
    }

    /**
     * @return the RMT type of this NetworkPaletteElement.
     */
    public int getType() {
        return type;
    }

}
