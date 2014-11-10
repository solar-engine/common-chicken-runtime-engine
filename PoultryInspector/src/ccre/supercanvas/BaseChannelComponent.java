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
package ccre.supercanvas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * A base component of channel components, such as float, boolean, and event
 * inputs and outputs.
 *
 * @author skeggsc
 * @param <View> the type of the View enum used for this component.
 */
public abstract class BaseChannelComponent<View extends Enum<View>> extends DraggableBoxComponent {

    private static final String CONFIGURATION_ENUM_NAME = "CONFIGURATION";
    private static final long serialVersionUID = 6151244350551965041L;
    /**
     * The name of this channel as it is displayed on the box.
     */
    protected final String name;
    /**
     * The active view of this channel.
     */
    protected View activeView;
    /**
     * The mouse locations to select views.
     */
    private int[] viewPositions = new int[0];

    /**
     * Create a new BaseChannelComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the input.
     */
    public BaseChannelComponent(int cx, int cy, String name) {
        super(cx, cy);
        this.name = name;
        setDefaultView();
    }

    @Override
    public final void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        halfWidth = Math.max(70, g.getFontMetrics().stringWidth(name) / 2 + 5);
        halfHeight = 46;
        if (activeView == null) {
            setDefaultView();
        }
        boolean inConfiguration = activeView.name().equals(CONFIGURATION_ENUM_NAME);
        if (getPanel().editmode) {
            Rendering.drawBody(Color.YELLOW, g, this);
            g.setColor(Color.BLACK);
            g.drawString(name, centerX - halfWidth + 5, centerY - halfHeight + 1 + g.getFontMetrics().getAscent());
            if (!inConfiguration) {
                g.setColor(new Color(128, 128, 128, 128));
                g.fillOval(centerX - halfWidth + 2, centerY + halfHeight - 10, 8, 8);
            }
        }
        g.setColor(Color.BLACK);
        if (inConfiguration) {
            if (getPanel().editmode) {
                int y = centerY - halfHeight + 5 + g.getFontMetrics().getAscent() + g.getFontMetrics().getHeight();
                String header = "Select View:";
                g.drawString(header, centerX - g.getFontMetrics().stringWidth(header) / 2, y);
                View[] enumConstants = activeView.getDeclaringClass().getEnumConstants();
                int[] localViewPositions = new int[enumConstants.length + 1];
                for (int i = 0; i < enumConstants.length; i++) {
                    View option = enumConstants[i];
                    localViewPositions[i] = y + g.getFontMetrics().getHeight() - g.getFontMetrics().getAscent();
                    if (option == activeView) {
                        continue;
                    }
                    y += g.getFontMetrics().getHeight();
                    if (mouseX >= centerX - halfWidth && mouseX <= centerX + halfWidth && mouseY >= y - g.getFontMetrics().getAscent() && mouseY < y + g.getFontMetrics().getDescent()) {
                        g.setColor(Color.GREEN);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.drawString(option.toString(), centerX - g.getFontMetrics().stringWidth(option.toString()) / 2, y);
                }
                localViewPositions[enumConstants.length] = y + g.getFontMetrics().getDescent();
                viewPositions = localViewPositions;
            }
        } else {
            channelRender(g, screenWidth, screenHeight, fontMetrics, mouseX, mouseY);
        }
    }
    
    /*
     * Set the view to the default view.
     */
    protected abstract void setDefaultView();

    @Override
    public boolean onSelect(int x, int y) {
        if (activeView.name().equals(CONFIGURATION_ENUM_NAME)) {
            for (int i=0; i<viewPositions.length - 1; i++) {
                if (x >= centerX - halfWidth && x <= centerX + halfWidth && y >= viewPositions[i] && y < viewPositions[i + 1]) {
                    activeView = activeView.getDeclaringClass().getEnumConstants()[i];
                    return true;
                }
            }
        } else if (getPanel().editmode && centerX - halfWidth + 2 <= x && x <= centerX - halfWidth + 10 && centerY + halfHeight - 10 <= y && y <= centerY + halfHeight - 2) {
            activeView = Enum.valueOf(activeView.getDeclaringClass(), CONFIGURATION_ENUM_NAME);
            return true;
        }
        return super.onSelect(x, y);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Called to render the channel-specific part of this component.
     *
     * @param g The graphics pen to use to render.
     * @param screenWidth The width of the screen.
     * @param screenHeight The height of the screen.
     * @param fontMetrics The metrics of the (monospaced) font.
     * @param mouseX The mouse X position.
     * @param mouseY The mouse Y position.
     */
    protected abstract void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY);
}
