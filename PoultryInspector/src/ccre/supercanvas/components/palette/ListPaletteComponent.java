/*
 * Copyright 2014 Cel Skeggs.
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

import java.util.ArrayList;
import java.util.Collection;

import ccre.supercanvas.SuperCanvasComponent;

/**
 * Create a new Palette backed by a collection (usually a list, hence the name),
 * and thusly can receive new objects added to it.
 *
 * @author skeggsc
 */
public class ListPaletteComponent extends PaletteComponent<Collection<PaletteEntry>> {

    private static final long serialVersionUID = 8666085475924970031L;

    /**
     * Create a new ListPaletteComponent backed by the specified collection.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     * @param entries the collection backing this object.
     */
    public ListPaletteComponent(int cx, int cy, Collection<PaletteEntry> entries) {
        super(cx, cy, entries);
    }

    /**
     * Create a new ListPaletteComponent backed by a new ArrayList.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     */
    public ListPaletteComponent(int cx, int cy) {
        super(cx, cy, new ArrayList<PaletteEntry>(8));
    }

    @Override
    public boolean onReceiveDrop(int x, int y, final SuperCanvasComponent activeComponent) {
        entries.add(new Element(activeComponent, activeComponent.getDragRelX(x), activeComponent.getDragRelY(y)));
        getPanel().remove(activeComponent);
        return true;
    }

    private class Element implements PaletteEntry {

        private static final long serialVersionUID = 1174601259257426927L;
        public final SuperCanvasComponent component;
        public final int relX;
        public final int relY;

        Element(SuperCanvasComponent component, int relX, int relY) {
            this.component = component;
            this.relX = relX;
            this.relY = relY;
        }

        @Override
        public String getName() {
            return component.toString();
        }

        @Override
        public SuperCanvasComponent fetch(int x, int y) {
            entries.remove(this);
            component.moveForDrag(x + relX, y + relY);
            return component;
        }
    }
}
