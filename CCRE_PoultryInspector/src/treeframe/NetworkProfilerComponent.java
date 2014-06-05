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

import ccre.net.CountingNetworkProvider;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * A component that displays the current results from the
 * CountingNetworkProvider.
 *
 * @author skeggsc
 */
public class NetworkProfilerComponent extends SuperCanvasComponent {

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setColor(Color.BLACK);
        String countReport = "~" + CountingNetworkProvider.getTotal() / 128 + "kbs";
        g.drawString(countReport, screenWidth - fontMetrics.stringWidth(countReport), fontMetrics.getAscent());
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    @Override
    public boolean onInteract(int x, int y) {
        return false;
    }

    @Override
    public boolean onSelect(int x, int y) {
        return false;
    }
}
