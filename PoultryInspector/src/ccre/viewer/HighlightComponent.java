/*
 * Copyright 2016 Cel Skeggs
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
package ccre.viewer;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.ObjectInputStream;

import ccre.supercanvas.DraggableBoxComponent;

public class HighlightComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = 4742533819471453628L;
    private static final Color[] chain = new Color[] { Color.BLACK, Color.RED, Color.BLUE, Color.ORANGE, Color.CYAN, Color.MAGENTA, Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.PINK };

    private static Color nextColor(Color color) {
        for (int i = 0; i < chain.length - 1; i++) {
            if (color.equals(chain[i])) {
                return chain[i + 1];
            }
        }
        return chain[0];
    }

    private transient ResizeState resizeState;
    private Color color = Color.BLACK;

    /**
     * Create a new HighlightComponent at the specified position.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     */
    public HighlightComponent(int cx, int cy) {
        super(cx, cy);
        halfWidth = 100;
        halfHeight = 50;
        resizeState = ResizeState.TRANSLATE;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        resizeState = ResizeState.TRANSLATE;
    }

    @Override
    public void moveForDrag(int x, int y) {
        int nx, ny;
        switch (resizeState) {
        case CORNER_BR:
            nx = 1;
            ny = 1;
            break;
        case CORNER_UR:
            nx = 1;
            ny = -1;
            break;
        case CORNER_BL:
            nx = -1;
            ny = 1;
            break;
        case CORNER_UL:
            nx = -1;
            ny = -1;
            break;
        default:
            super.moveForDrag(x, y);
            return;
        }
        int ox = centerX - nx * halfWidth;
        int oy = centerY - ny * halfHeight;
        halfWidth = x * nx / 2;
        halfHeight = y * ny / 2;
        if (halfWidth < 5) {
            halfWidth = 5;
        }
        if (halfHeight < 5) {
            halfHeight = 5;
        }
        centerX = ox + nx * halfWidth;
        centerY = oy + ny * halfHeight;
    }

    @Override
    public boolean canDrop() {
        return resizeState == ResizeState.TRANSLATE;
    }

    @Override
    public int getDragRelX(int x) {
        switch (resizeState) {
        case CORNER_BR:
        case CORNER_UR:
            return halfWidth * 2 - x;
        case CORNER_BL:
        case CORNER_UL:
            return -halfWidth * 2 - x;
        default:
            return super.getDragRelX(x);
        }
    }

    @Override
    public int getDragRelY(int y) {
        switch (resizeState) {
        case CORNER_BR:
        case CORNER_BL:
            return halfHeight * 2 - y;
        case CORNER_UR:
        case CORNER_UL:
            return -halfHeight * 2 - y;
        default:
            return super.getDragRelY(y);
        }
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setColor(color);
        g.drawRect(this.centerX - (this.halfWidth * 2) / 2, this.centerY - (this.halfHeight * 2) / 2, this.halfWidth * 2, this.halfHeight * 2);
        g.setColor(Color.WHITE);
        if (getPanel().editmode) {
            g.drawLine(centerX - halfWidth + 6, centerY - halfHeight + 10, centerX - halfWidth + 10, centerY - halfHeight + 6);
            g.drawLine(centerX + halfWidth - 6, centerY - halfHeight + 10, centerX + halfWidth - 10, centerY - halfHeight + 6);
            g.drawLine(centerX - halfWidth + 6, centerY + halfHeight - 10, centerX - halfWidth + 10, centerY + halfHeight - 6);
            g.drawLine(centerX + halfWidth - 6, centerY + halfHeight - 10, centerX + halfWidth - 10, centerY + halfHeight - 6);
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        this.color = nextColor(color);
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        if (containsForInteract(x, y)) {
            return onInteract(x, y);
        }
        resizeState = ResizeState.TRANSLATE;
        if (x >= centerX + halfWidth - 10) {
            if (y >= centerY + halfHeight - 10) {
                resizeState = ResizeState.CORNER_BR;
            } else if (y <= centerY - halfHeight + 10) {
                resizeState = ResizeState.CORNER_UR;
            }
        } else if (x <= centerX - halfWidth + 10) {
            if (y >= centerY + halfHeight - 10) {
                resizeState = ResizeState.CORNER_BL;
            } else if (y <= centerY - halfHeight + 10) {
                resizeState = ResizeState.CORNER_UL;
            }
        }
        getPanel().startDrag(this, x, y);
        return true;
    }

    @Override
    public String toString() {
        return "highlight";
    }

    private static enum ResizeState {

        TRANSLATE, CORNER_BR, CORNER_UR, CORNER_BL, CORNER_UL
    }
}
