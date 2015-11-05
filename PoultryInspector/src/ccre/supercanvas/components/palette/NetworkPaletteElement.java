/*
 * Copyright 2014-2015 Colby Skeggs.
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
package ccre.supercanvas.components.palette;

import ccre.cluck.CluckConstants;
import ccre.supercanvas.SuperCanvasComponent;
import ccre.supercanvas.components.channels.FictionalChannelComponent;

/**
 * An element of a NetworkPaletteComponent.
 *
 * @author skeggsc
 */
class NetworkPaletteElement implements PaletteEntry, Comparable<NetworkPaletteElement> {

    private static final long serialVersionUID = 4574977118418297829L;
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
            return new FictionalChannelComponent(x, y, name, CluckConstants.rmtToString(type));
        }
        return out;
    }

    @Override
    public int compareTo(NetworkPaletteElement o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NetworkPaletteElement && name.equals(((NetworkPaletteElement) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @return the RMT type of this NetworkPaletteElement.
     */
    public int getType() {
        return type;
    }

}
