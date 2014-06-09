/*
 * Copyright 2014 (YOUR NAME HERE).
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

import java.io.Serializable;

/**
 * An Entry in a PaletteComponent.
 *
 * @author skeggsc
 */
public interface PaletteEntry extends Serializable {

    /**
     * @return the displayed name of this component.
     */
    public String getName();

    /**
     * Allocate or fetch a new component instance of this entry.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return the new instance.
     */
    public SuperCanvasComponent fetch(int x, int y);
}
