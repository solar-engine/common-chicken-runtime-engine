/*
 * Copyright 2014 Colby Skeggs
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

public class EditModeComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = -9102993613096088676L;

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setColor(getPanel().editmode ? Color.WHITE : Color.BLACK);
        String countReport = getPanel().editmode ? "EDIT MODE" : "OPERATE MODE";
        g.drawString(countReport, 0, screenHeight - fontMetrics.getDescent());
    }

    @Override
    public boolean contains(int x, int y) {
        return x < 100 && y >= getPanel().getHeight() - 20;
    }

    @Override
    public boolean onInteract(int x, int y) {
        getPanel().editmode = !getPanel().editmode;
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        getPanel().editmode = !getPanel().editmode;
        return true;
    }

}
