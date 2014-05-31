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

/**
 * A simple draggable SuperCanvasComponent.
 *
 * @author skeggsc
 */
public abstract class DraggableBoxComponent extends SuperCanvasComponent {

    protected int centerX, centerY;
    protected int width = 20, height = 20;

    public DraggableBoxComponent(int cx, int cy) {
        centerX = cx;
        centerY = cy;
    }

    @Override
    public boolean contains(int x, int y) {
        return Math.abs(x - centerX) <= width && Math.abs(y - centerY) <= height;
    }

    protected boolean containsForInteract(int x, int y) {
        return false;
    }

    @Override
    public int getDragRelX(int x) {
        return centerX - x;
    }

    @Override
    public int getDragRelY(int y) {
        return centerY - y;
    }

    @Override
    public void moveForDrag(int x, int y) {
        centerX = x;
        centerY = y;
    }

    @Override
    public boolean onSelect(int x, int y) {
        if (containsForInteract(x, y)) {
            return onInteract(x, y);
        } else {
            getPanel().startDrag(this, x, y);
            return true;
        }
    }
}
