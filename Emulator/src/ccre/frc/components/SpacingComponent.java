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
package ccre.frc.components;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.frc.DeviceComponent;

/**
 * A component of empty space. Doesn't do anything but shift subsequent
 * components over.
 *
 * @author skeggsc
 */
public class SpacingComponent extends DeviceComponent {

    private final int spacing;

    /**
     * Create a new SpacingComponent with a certain amount of spacing.
     *
     * @param spacing the amount of spacing, in pixels.
     */
    public SpacingComponent(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift) {
        return lastShift + spacing;
    }
}
