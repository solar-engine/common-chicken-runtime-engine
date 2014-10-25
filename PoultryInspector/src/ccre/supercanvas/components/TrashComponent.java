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
package ccre.supercanvas.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.log.Logger;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasComponent;

/**
 * A trash can - (almost?) anything can be dragged here to delete it from the
 * canvas.
 *
 * @author skeggsc
 */
public class TrashComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = 6252825684766041481L;

    /**
     * Create a new TrashComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     */
    public TrashComponent(int cx, int cy) {
        super(cx, cy, true);
    }

    @Override
    public boolean onReceiveDrop(int x, int y, SuperCanvasComponent activeEntity) {
        if (activeEntity.onDelete(false)) {
            getPanel().remove(activeEntity);
            Logger.fine("Deleted component: " + activeEntity);
        } else {
            Logger.warning("Component deletion disallowed: " + activeEntity);
        }
        return true;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        Rendering.drawBody(Color.RED, g, this);
        // TODO: Improve graphics?
        //g.setColor(Color.GRAY);
        //g.fillRect(centerX - halfWidth, centerY - halfHeight * 2 / 3, halfWidth * 2, halfHeight * 4 / 3);
        //g.fillOval(centerX - halfWidth - 1, centerY + halfHeight / 3, halfWidth * 2, halfHeight * 2 / 3);
        g.setColor(Color.BLACK);
        g.drawLine(centerX - halfWidth / 2, centerY - halfHeight / 2, centerX + halfWidth / 2, centerY + halfHeight / 2);
        g.drawLine(centerX + halfWidth / 2, centerY - halfHeight / 2, centerX - halfWidth / 2, centerY + halfHeight / 2);
        //g.setColor(Color.LIGHT_GRAY);
        //g.fillOval(centerX - halfWidth - 1, centerY - halfHeight, halfWidth * 2, halfHeight * 2 / 3);
    }

    @Override
    public boolean onInteract(int x, int y) {
        getPanel().remove(this);
        return true;
    }

    @Override
    public String toString() {
        return "Trash Can";
    }
}
