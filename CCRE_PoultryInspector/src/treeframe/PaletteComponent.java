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
import java.awt.Graphics2D;

public class PaletteComponent<T extends Iterable<PaletteComponent.PaletteEntry>> extends DraggableBoxComponent {

    public final T entries;
    private int rowHeight, yshift;

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        int maxWidth = 100, count = 0;
        for (PaletteEntry ent : entries) {
            maxWidth = Math.max(maxWidth, fontMetrics.stringWidth(ent.getName()));
            count++;
        }
        width = (maxWidth + 10) / 2;
        rowHeight = fontMetrics.getHeight();
        height = (count * rowHeight + 40) / 2;
        g.setColor(Color.BLACK);
        g.fillRect(centerX - width, centerY - height, width * 2, height * 2);
        g.setColor(Color.YELLOW);
        g.fillRect(centerX - width + 1, centerY - height + 1, width * 2 - 2, height * 2 - 2);
        int xPos = centerX - width + 1;
        int yPos = centerY - height + 26;
        yshift = fontMetrics.getAscent();
        for (PaletteEntry ent : entries) {
            if (mouseX >= xPos && mouseX <= xPos + width * 2 - 2
                    && mouseY >= yPos - yshift && mouseY < yPos - yshift + rowHeight) {
                g.setColor(Color.ORANGE);
                g.fillRect(xPos, yPos - yshift, width * 2 - 2, rowHeight);
            }
            g.setColor(Color.BLACK);
            g.drawString(ent.getName(), xPos, yPos);
            yPos += rowHeight;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        int xPos = centerX - width + 1;
        int yPos = centerY - height + 26;
        for (PaletteEntry ent : entries) {
            if (x >= xPos && x <= xPos + width * 2 - 2 && y >= yPos - yshift && y < yPos - yshift + rowHeight) {
                onInteract(ent);
            }
            yPos += rowHeight;
        }
        return false;
    }

    protected void onInteract(PaletteEntry ent) {
        // No default active.
    }

    @Override
    public boolean onSelect(int x, int y) {
        int xPos = centerX - width + 1;
        int yPos = centerY - height + 26;
        for (PaletteEntry ent : entries) {
            if (x >= xPos && x <= xPos + width * 2 - 2 && y >= yPos - yshift && y < yPos - yshift + rowHeight) {
                SuperCanvasComponent nent = ent.fetch(x, y);
                getPanel().add(nent);
                getPanel().startDrag(nent, x, y);
                return true;
            }
            yPos += rowHeight;
        }
        getPanel().startDrag(this, x, y);
        return true;
    }

    public static interface PaletteEntry {

        public String getName();

        public SuperCanvasComponent fetch(int x, int y);
    }

    public PaletteComponent(int cx, int cy, T entries) {
        super(cx, cy);
        this.entries = entries;
    }
}
