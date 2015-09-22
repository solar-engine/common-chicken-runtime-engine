/*
 * Copyright 2014-2015 Colby Skeggs.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.supercanvas.components.channels.RConfComponent;

/**
 * A base component of channel components, such as float, boolean, and event
 * inputs and outputs.
 *
 * @author skeggsc
 * @param <View> the type of the View enum used for this component.
 */
public abstract class BaseChannelComponent<View extends Enum<View>> extends DraggableBoxComponent implements RConfable {
    private static Logger logger = LoggerFactory.getLogger(BaseChannelComponent.class);

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
        if (getPanel().editmode) {
            Rendering.drawBody(Color.YELLOW, g, this);
            g.setColor(Color.BLACK);
            g.drawString(name, centerX - halfWidth + 5, centerY - halfHeight + 1 + g.getFontMetrics().getAscent());
            g.setColor(new Color(128, 128, 128, 128));
            g.fillOval(centerX - halfWidth + 2, centerY + halfHeight - 10, 8, 8);
            g.setColor(new Color(255, 0, 0, 128));
            g.fillOval(centerX + halfWidth - 10, centerY + halfHeight - 10, 8, 8);
        }
        g.setColor(Color.BLACK);
        channelRender(g, screenWidth, screenHeight, fontMetrics, mouseX, mouseY);
    }

    /*
     * Set the view to the default view.
     */
    protected abstract void setDefaultView();

    @Override
    public boolean onSelect(int x, int y) {
        if (getPanel().editmode && centerY + halfHeight - 10 <= y && y <= centerY + halfHeight - 2) {
            if (centerX - halfWidth + 2 <= x && x <= centerX - halfWidth + 10) {
                getPanel().add(new RConfComponent(x, y, "display config", this));
                return true;
            } else if (centerX + halfWidth - 10 <= x && x <= centerX + halfWidth - 2) {
                if (this.onDelete(false)) {
                    getPanel().remove(this);
                } else {
                    logger.warn("Component deletion disallowed: " + this);
                }
                return true;
            }
        }
        return super.onSelect(x, y);
    }

    /**
     * A helper function for the channel implementations, so that they can just
     * specify their own RConf entries without having to worry about the default
     * ones that will be prepended.
     *
     * Use as in <code>return rconfBase(... entries ...);</code>
     *
     * @param userEntries the RConf entries from the subclass.
     * @return the list of RConf entries.
     */
    protected Entry[] rconfBase(Entry... userEntries) {
        View[] cst = activeView.getDeclaringClass().getEnumConstants();
        Entry[] out = new Entry[userEntries.length + 1 + cst.length];
        out[0] = RConf.title(toString());
        for (int i = 0; i < cst.length; i++) {
            out[1 + i] = RConf.button(cst[i] == activeView ? "[" + cst[i].name() + "]" : cst[i].name());
        }
        System.arraycopy(userEntries, 0, out, cst.length + 1, userEntries.length);
        return out;
    }

    /**
     * @see #rconfBase(int, byte[])
     */
    protected static final int BASE_VALID = -1;
    /**
     * @see #rconfBase(int, byte[])
     */
    protected static final int BASE_INVALID = -2;

    /**
     * A helper function for the channel implementations, so that they can just
     * specify their own RConf entries without having to worry about the default
     * ones.
     *
     * If the given field is for a default entry, this function takes care of
     * doing whatever it means, and returns a negative number: -1 (BASE_VALID)
     * if the request was processed, or -2 (BASE_INVALID) if the request was
     * invalid.
     *
     * If not, it returns a value equal to the index in the user entries of the
     * interacted-with component.
     *
     * @param field the overall field number.
     * @param data the data of the command.
     * @return the index in the user entries array of the interacted-with field,
     * or a negative number if before that.
     */
    protected int rconfBase(int field, byte[] data) {
        View[] cst = activeView.getDeclaringClass().getEnumConstants();
        field -= 1;
        if (field < 0) {
            return BASE_INVALID;
        }
        if (field < cst.length) {
            activeView = cst[field];
            return BASE_VALID;
        }
        return field - cst.length;
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
