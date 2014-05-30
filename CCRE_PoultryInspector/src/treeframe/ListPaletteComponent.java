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
package treeframe;

import java.util.Collection;

public class ListPaletteComponent extends PaletteComponent<Collection<PaletteComponent.PaletteEntry>> {

    private class Element implements PaletteComponent.PaletteEntry {

        public final SuperCanvasComponent component;
        public final int relX;
        public final int relY;

        public Element(SuperCanvasComponent component, int relX, int relY) {
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
            component.moveForDrag(x, y);
            return component;
        }
    }

    public ListPaletteComponent(int cx, int cy, Collection<PaletteEntry> entries) {
        super(cx, cy, entries);
    }

    public boolean onReceiveDrop(int x, int y, final SuperCanvasComponent activeComponent) {
        entries.add(new Element(activeComponent, activeComponent.getDragRelX(x), activeComponent.getDragRelY(y)));
        getPanel().remove(activeComponent);
        return true;
    }
}
