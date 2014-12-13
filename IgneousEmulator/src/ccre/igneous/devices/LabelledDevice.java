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
package ccre.igneous.devices;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.igneous.Device;

public abstract class LabelledDevice extends Device {

    private final String label;

    public LabelledDevice(String label) {
        this.label = label;
    }

    @Override
    public final void render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY) {
        super.render(g, width, height, fontMetrics, mouseX, mouseY);
        g.setColor(Color.WHITE);
        g.drawString(label, 20, height / 2 + fontMetrics.getDescent());
        int shift = fontMetrics.stringWidth(label) + 35;
        g.translate(shift, 0);
        labelledRender(g, width - shift, height, fontMetrics, mouseX - shift, mouseY);
        g.translate(-shift, 0);
    }

    protected abstract void labelledRender(Graphics2D g, int i, int height, FontMetrics fontMetrics, int j, int mouseY);
}
