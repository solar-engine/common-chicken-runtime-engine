/*
 * Copyright 2014-2016 Cel Skeggs
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
package ccre.supercanvas.components.pinned;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.function.Supplier;

import ccre.supercanvas.SuperCanvasComponent;

/**
 * The "Start-Menu"-like component to allow opening and closing the top-level
 * palette.
 *
 * @author skeggsc
 */
public class StartComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = 5841953202431409373L;
    private final Supplier<? extends SuperCanvasComponent> popup;
    private final Class<? extends SuperCanvasComponent> expected;
    private final String name;
    private final int index;

    /**
     * Create a new StartComponent.
     *
     * @param popup an allocator (usually constructor) for new instances of
     * the popup class
     * @param name the name to display
     * @param index the index, starting from the bottom of the screen
     */
    public StartComponent(Supplier<? extends SuperCanvasComponent> popup, String name, int index) {
        super(true);
        this.name = name;
        this.index = index;
        this.expected = popup.get().getClass();
        this.popup = popup;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        boolean open = getPanel().containsAny(expected);
        g.setColor(getPanel().editmode ? Color.WHITE : Color.BLACK);
        String countReport = open ? "CLOSE " + name.toUpperCase() : "OPEN " + name.toUpperCase();
        g.drawString(countReport, screenWidth - fontMetrics.stringWidth(countReport), screenHeight - fontMetrics.getDescent() - 15 * index);
    }

    @Override
    public boolean contains(int x, int y) {
        int dy = getPanel().getHeight() - y;
        return x >= getPanel().getWidth() - 100 && 15 * index <= dy && dy < 15 + 15 * index;
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (!getPanel().removeAll(expected)) {
            getPanel().add(popup.get());
        }
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        return onInteract(x, y);
    }

}
