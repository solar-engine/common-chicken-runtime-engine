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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;

/**
 * A palette of entities - which can be dragged out of the palette into the main
 * canvas. This is backed by an iterable, usually managed by a subclass.
 *
 * @author skeggsc
 * @param <T> The type of the backing iterable or collection.
 */
public class PaletteComponent<T extends Iterable<? extends PaletteComponent.PaletteEntry>> extends DraggableBoxComponent {

    /**
     * The list of entries available in this component.
     */
    public final T entries;
    private transient int rowHeight, yshift, scroll, maxScroll;
    private transient boolean isScrolling = false;

    public PaletteComponent(int cx, int cy, T entries) {
        super(cx, cy);
        this.entries = entries;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        int maxWidth = 100, count = 0;
        synchronized (entries) {
            for (PaletteEntry ent : entries) {
                maxWidth = Math.max(maxWidth, fontMetrics.stringWidth(ent.getName()));
                count++;
            }
        }
        halfWidth = (maxWidth) / 2 + 20;
        rowHeight = fontMetrics.getHeight();
        halfHeight = (count * rowHeight + 40) / 2;
        if (halfHeight > 200) {
            halfHeight = 200;
        }
        g.setPaint(new GradientPaint(centerX + 20, centerY - halfHeight, Color.LIGHT_GRAY, centerX - 20, centerY + halfHeight, Color.LIGHT_GRAY.darker()));
        g.fillRoundRect(centerX - halfWidth + 1, centerY - halfHeight + 1, halfWidth * 2 - 2, halfHeight * 2 - 2, 10, 10);
        int xPos = centerX - halfWidth + 16;
        int yPos = centerY - halfHeight + 26 - scroll;
        yshift = fontMetrics.getAscent();
        Shape clip = g.getClip();
        g.setClip(new Rectangle(centerX - halfWidth + 5, centerY - halfHeight + 5, halfWidth * 2 - 10, halfHeight * 2 - 10));
        int cnt = 0;
        synchronized (entries) {
            for (PaletteEntry ent : entries) {
                if (mouseX >= xPos - 5 && mouseX <= xPos + halfWidth * 2 - 28
                        && mouseY >= yPos - yshift && mouseY < yPos - yshift + rowHeight) {
                    g.setColor(Color.WHITE);
                    g.fillRoundRect(xPos - 5, yPos - yshift, halfWidth * 2 - 22, rowHeight, 10, 10);
                }
                g.setColor(Color.BLACK);
                g.drawString(ent.getName(), xPos, yPos);
                yPos += rowHeight;
                cnt++;
            }
        }
        this.maxScroll = cnt * fontMetrics.getHeight() - halfHeight;
        g.setClip(clip);
        float frac = scroll / (float) maxScroll;
        if (frac < 0) {
            frac = 0;
        } else if (frac > 1) {
            frac = 1;
        }
        g.setColor(scroll == 0 ? Color.GREEN : Color.BLACK);
        g.fillOval(centerX - halfWidth + 4, centerY - halfHeight + 8 + (int) ((2 * halfHeight - 24) * frac), 8, 8);
    }

    @Override
    public boolean onInteract(int x, int y) {
        int xPos = centerX - halfWidth + 16;
        int yPos = centerY - halfHeight + 26 - scroll;
        for (PaletteEntry ent : entries) {
            if (x >= xPos - 5 && x <= xPos + halfWidth * 2 - 28 && y >= yPos - yshift && y < yPos - yshift + rowHeight) {
                onInteract(ent);
            }
            yPos += rowHeight;
        }
        return false;
    }

    /**
     * Called when the specified PaletteEntry is right-clicked on in the
     * palette.
     *
     * @param ent the entry clicked on.
     */
    protected void onInteract(PaletteEntry ent) {
        // No default active.
    }

    @Override
    public boolean onSelect(int x, int y) {
        int xPos = centerX - halfWidth + 16;
        int yPos = centerY - halfHeight + 26 - scroll;
        isScrolling = false;
        for (PaletteEntry ent : entries) {
            if (x < xPos - 5) {
                isScrolling = true;
                break;
            } else if (x <= xPos + halfWidth * 2 - 28 && y >= yPos - yshift && y < yPos - yshift + rowHeight) {
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

    @Override
    public boolean onMouseMove(int x, int y) {
        return true;
    }

    @Override
    public boolean onScroll(int x, int y, int wheelRotation) {
        scroll += wheelRotation;
        constrainScrolling();
        return true;
    }

    private void constrainScrolling() {
        if (scroll < 0) {
            scroll = 0;
        } else if (scroll > maxScroll) {
            scroll = maxScroll;
        }
    }

    @Override
    public void moveForDrag(int x, int y) {
        if (isScrolling) {
            scroll = y * maxScroll / (2 * halfHeight - 24);
            constrainScrolling();
        } else {
            super.moveForDrag(x, y);
        }
    }

    @Override
    public boolean canDrop() {
        return !isScrolling;
    }

    @Override
    public int getDragRelX(int x) {
        return isScrolling ? 0 : super.getDragRelX(x);
    }

    @Override
    public int getDragRelY(int y) {
        if (isScrolling) {
            return (int) ((2 * halfHeight - 24) * (scroll / (float) maxScroll)) - y;
        } else {
            return super.getDragRelY(y);
        }
    }

    public static interface PaletteEntry extends Serializable {

        public String getName();

        public SuperCanvasComponent fetch(int x, int y);
    }

}
