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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class FolderComponent extends DraggableBoxComponent {

    private static class Element {

        public final SuperCanvasComponent component;
        public final int relX;
        public final int relY;

        public Element(SuperCanvasComponent component, int relX, int relY) {
            this.component = component;
            this.relX = relX;
            this.relY = relY;
        }
    }

    private final ArrayList<Element> components = new ArrayList<Element>();

    public FolderComponent(int cx, int cy) {
        super(cx, cy);
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        this.width = 20;
        this.height = 20;
        String str = "[" + components.size() + "]";
        g.setColor(Color.BLACK);
        g.fillRect(centerX - width, centerY - height, width * 2, height * 2);
        g.setColor(Color.YELLOW);
        g.fillRect(centerX - width + 1, centerY - height + 1, width * 2 - 2, height * 2 - 2);
        g.setColor(Color.BLACK);
        g.drawString(str, centerX - fontMetrics.stringWidth(str) / 2, centerY);
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (!components.isEmpty()) {
            Element comp = components.remove(components.size() - 1);
            getPanel().add(comp.component);
            comp.component.moveForDrag(centerX + comp.relX, centerY + comp.relY);
            return true;
        }
        return false;
    }

    public boolean onReceiveDrop(int x, int y, SuperCanvasComponent activeEntity) {
        getPanel().remove(activeEntity);
        components.add(new Element(activeEntity, activeEntity.getDragRelX(centerX), activeEntity.getDragRelY(centerY)));
        return true;
    }
}
