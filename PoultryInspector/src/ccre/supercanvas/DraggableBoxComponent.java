/*
 * Copyright 2014,2016 Cel Skeggs.
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
package ccre.supercanvas;

/**
 * A simple draggable SuperCanvasComponent.
 *
 * @author skeggsc
 */
public abstract class DraggableBoxComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = -7316128361122443951L;
    /**
     * The X-coordinate of the center of the draggable box.
     */
    protected int centerX;
    /**
     * The Y-coordinate of the center of the draggable box.
     */
    protected int centerY;
    /**
     * The horizontal distance from the center to the side of the box.
     */
    protected int halfWidth = 20;
    /**
     * The vertical distance from the center to the side of the box.
     */
    protected int halfHeight = 20;

    /**
     * Construct a new DraggableBoxComponent with the specified position. This
     * will be visible in both EDIT mode and OPERATE mode
     *
     * @param cx The X-coordinate.
     * @param cy The Y-coordinate.
     */
    public DraggableBoxComponent(int cx, int cy) {
        centerX = cx;
        centerY = cy;
    }

    /**
     * Construct a new DraggableBoxComponent with the specified position.
     *
     * @param cx The X-coordinate.
     * @param cy The Y-coordinate.
     * @param hideInOperateMode if this should be invisible during OPERATE mode.
     */
    public DraggableBoxComponent(int cx, int cy, int zIndex, boolean hideInOperateMode) {
        super(zIndex, hideInOperateMode);
        centerX = cx;
        centerY = cy;
    }

    @Override
    public boolean contains(int x, int y) {
        return Math.abs(x - centerX) <= halfWidth && Math.abs(y - centerY) <= halfHeight;
    }

    /**
     * Check if clicks on the specified position should be sent as an
     * interaction event instead of selection event.
     *
     * @param x the X-coordinate.
     * @param y the Y-coordinate.
     * @return if the position is in an interaction zone.
     */
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

    /**
     * Check if this component is being dragged.
     *
     * @return if this component is being dragged.
     */
    public boolean isBeingDragged() {
        return getPanel().isBeingDragged(this);
    }
}
