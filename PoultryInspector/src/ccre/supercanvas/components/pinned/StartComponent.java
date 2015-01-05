/*
 * Copyright 2014-2015 Colby Skeggs
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

import ccre.supercanvas.SuperCanvasComponent;
import ccre.supercanvas.components.palette.TopLevelPaletteComponent;

/**
 * The "Start-Menu"-like component to allow opening and closing the top-level
 * palette.
 * 
 * @author skeggsc
 */
public class StartComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = 5841953202431409373L;

    /**
     * Create a new StartComponent.
     */
    public StartComponent() {
        super(true);
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        boolean open = getPanel().containsAny(TopLevelPaletteComponent.class);
        g.setColor(getPanel().editmode ? Color.WHITE : Color.BLACK);
        String countReport = open ? "CLOSE PALETTE" : "OPEN PALETTE";
        g.drawString(countReport, screenWidth - fontMetrics.stringWidth(countReport), screenHeight - fontMetrics.getDescent());
    }

    @Override
    public boolean contains(int x, int y) {
        return x >= getPanel().getWidth() - 100 && y >= getPanel().getHeight() - 20;
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (!getPanel().removeAll(TopLevelPaletteComponent.class)) {
            getPanel().add(new TopLevelPaletteComponent(200, 200));
        }
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        return onInteract(x, y);
    }

}
