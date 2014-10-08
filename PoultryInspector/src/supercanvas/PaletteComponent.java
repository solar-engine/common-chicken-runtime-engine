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
package supercanvas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * A palette of entities - which can be dragged out of the palette into the main
 * canvas. This is backed by an iterable, usually managed by a subclass.
 *
 * @author skeggsc
 * @param <T> The type of the backing iterable or collection.
 */
public class PaletteComponent<T extends Iterable<? extends PaletteEntry>> extends DraggableBoxComponent {

    private static final long serialVersionUID = 4042918337094646087L;
    /**
     * The list of entries available in this component.
     */
    public final T entries;
    private transient int rowHeight, yshift, scroll, maxScroll;
    private transient boolean isScrolling = false;

    /**
     * Create a new PaletteComponent at the specified position with the
     * specified iterator backing this element.
     *
     * @param cx The X coordinate.
     * @param cy The Y coordinate.
     * @param entries The iterable of entries to include on the list.
     */
    public PaletteComponent(int cx, int cy, T entries) {
        super(cx, cy, true);
        this.entries = entries;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        calculatePaletteSize(fontMetrics);
        Rendering.drawBody(Color.LIGHT_GRAY, g, this);
        Shape clip = g.getClip();
        g.setClip(new Rectangle(centerX - halfWidth + 5, centerY - halfHeight + 24, halfWidth * 2 - 10, halfHeight * 2 - 36));
        int entryCount = drawPaletteEntries(mouseX, mouseY, centerX - halfWidth + 16, centerY - halfHeight + 36 - scroll, g, fontMetrics);
        g.setClip(clip);
        drawScrollbar(entryCount, fontMetrics, g);
    }

    private int drawPaletteEntries(int mouseX, int mouseY, int xPos, int initialYPos, Graphics2D g, FontMetrics fontMetrics) {
        int yPos = initialYPos;
        yshift = fontMetrics.getAscent();
        int entryCount = 0;
        synchronized (entries) {
            for (PaletteEntry ent : entries) {
                if (mouseX >= xPos - 5 && mouseX <= xPos + halfWidth * 2 - 28 && mouseY >= yPos - yshift && mouseY < yPos - yshift + rowHeight) {
                    g.setColor(Color.WHITE);
                    g.fillRoundRect(xPos - 5, yPos - yshift, halfWidth * 2 - 22, rowHeight, 10, 10);
                }
                g.setColor(Color.BLACK);
                g.drawString(ent.getName(), xPos, yPos);
                yPos += rowHeight;
                entryCount++;
            }
        }
        return entryCount;
    }

    private void drawScrollbar(int cnt, FontMetrics fontMetrics, Graphics2D g) {
        this.maxScroll = cnt * fontMetrics.getHeight() - halfHeight;
        float frac = scroll / (float) maxScroll;
        if (frac < 0) {
            frac = 0;
        } else if (frac > 1) {
            frac = 1;
        }
        g.setColor(scroll == 0 ? Color.GREEN : Color.BLACK);
        Rendering.drawScrollbar(g, scroll != 0, centerX - halfWidth + 8, centerY - halfHeight + 12 + (int) ((2 * halfHeight - 24) * frac));
    }

    private void calculatePaletteSize(FontMetrics fontMetrics) {
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
    }
    
    protected boolean onInteractWithTitleBar() {
        // Do nothing by default.
        return false;
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (y < centerY - halfHeight + 24) {
            return onInteractWithTitleBar();
        }
        int xPos = centerX - halfWidth + 16;
        int yPos = centerY - halfHeight + 36 - scroll;
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
        int yPos = centerY - halfHeight + 36 - scroll;
        isScrolling = false;
        for (PaletteEntry ent : entries) {
            if (x < xPos - 5) {
                isScrolling = true;
                break;
            } else if (y >= centerY - halfHeight + 24 && x <= xPos + halfWidth * 2 - 28 && y >= yPos - yshift && y < yPos - yshift + rowHeight) {
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
}
