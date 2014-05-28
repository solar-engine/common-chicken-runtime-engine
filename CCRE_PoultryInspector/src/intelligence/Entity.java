/*
 * Copyright 2013-2014 Colby Skeggs
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
package intelligence;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.cluck.CluckNode;
import static ccre.cluck.CluckNode.RMT_BOOLOUTP;
import static ccre.cluck.CluckNode.RMT_BOOLPROD;
import static ccre.cluck.CluckNode.RMT_EVENTINPUT;
import static ccre.cluck.CluckNode.RMT_EVENTOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * A block on the screen representing a remote target.
 *
 * @author skeggsc
 */
public final class Entity {

    /**
     * The Remote that this Entity displays.
     */
    public final Remote represented;
    /**
     * The X coordinate of the Entity on the screen.
     */
    public int centerX;
    /**
     * The Y coordinate of the Entity on the screen.
     */
    public int centerY;
    /**
     * Has this been registered so that it will be updated by the remote?
     */
    protected boolean registered;
    /**
     * When did the current animation cycle start, if an animation cycle is
     * being used?
     */
    protected long countStart = 0;
    /**
     * The current value - this depends on the kind of Remote.
     */
    protected Object currentValue;
    /**
     * The cached width of the Entity.
     */
    protected int width = 20;
    /**
     * The cached height of the Entity.
     */
    protected int height = 20;

    /**
     * Create an Entity at the specified location and using the specified
     * Remote.
     *
     * @param remote The Remote to display in this entity.
     * @param centerX The initial X position.
     * @param centerY The initial Y position.
     */
    public Entity(Remote remote, int centerX, int centerY) {
        this.represented = remote;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * Render this Entity on the specified graphics pane.
     *
     * @param g The graphics pane.
     */
    public void render(Graphics g) {
        Rendering.setupFont(g);
        width = Rendering.calculateEntityWidth(g, represented.path);
        height = Rendering.calculateEntityHeight(g, represented.path);
        Rendering.drawEntityBackplate(g, centerX, centerY, width, height, Rendering.getColor(represented));
        Rendering.drawEntityText(g, centerX - width + 1, centerY - height + 1, new String[]{
            represented.path,
            CluckNode.rmtToString(represented.type)
        });
        Object co = represented.checkout();
        if (co == null) {
            return;
        }
        Color col = Rendering.getColor(represented);
        long count = (System.currentTimeMillis() - countStart);
        int rh = g.getFontMetrics().getHeight();
        if (!registered) {
            registered = true;
            this.register(co);
        }
        switch (represented.type) {
            case RMT_EVENTOUTP:
            case RMT_EVENTINPUT:
                g.setColor(Rendering.blend(col.darker(), col, count / 500.0f));
                g.fillRect(centerX - width + 1, centerY + height - rh - 1, width * 2 - 2, rh - 2);
                break;
            case RMT_BOOLPROD:
                if (currentValue != null) {
                    g.setColor((Boolean) currentValue ? Color.GREEN : Color.RED);
                    g.fillRect(centerX - width + 1, centerY + height - rh, width * 2 - 2, rh - 1);
                    g.setColor(Color.YELLOW);
                    g.drawString((Boolean) currentValue ? "TRUE" : "FALSE", centerX - width + 1, centerY + height - g.getFontMetrics().getDescent());
                }
                break;
            case RMT_BOOLOUTP:
                g.setColor(Color.GREEN);
                g.fillRect(centerX - width + 1, centerY + height - rh, width - 1, rh - 1);
                g.setColor(Color.RED);
                g.fillRect(centerX, centerY + height - rh, width - 1, rh - 1);
                if (currentValue != null) {
                    boolean cur = (Boolean) currentValue;
                    g.setColor(Rendering.blend(Color.BLACK, cur ? Color.GREEN : Color.RED, count / 500.0f));
                    g.drawString(cur ? "TRUE" : "FALSE",
                            cur ? centerX - g.getFontMetrics().stringWidth("TRUE") : centerX,
                            centerY + height - g.getFontMetrics().getDescent());
                }
                break;
            case RMT_FLOATOUTP:
                if (currentValue == null) {
                    break;
                }
            case RMT_FLOATPROD:
                float c = (Float) currentValue;
                g.setColor(Rendering.blend(Rendering.floatColorCalculate(c, col), col, count / 500.0f));
                g.fillRect(centerX - width + 1, centerY + height - rh - 1, width * 2 - 2, rh - 2);
                g.setColor(c < 0 ? Color.WHITE : Color.BLACK);
                g.drawString(String.valueOf(c), centerX - width + 1, centerY + height - g.getFontMetrics().getDescent());
                break;
        }
    }

    /**
     * Is the specified point on top of this block?
     *
     * @param point The point to check at.
     * @return If the point is within the bounds of the bounding shape.
     */
    public boolean isOver(Point point) {
        return Math.abs(point.getX() - centerX) <= width && Math.abs(point.getY() - centerY) <= height;
    }

    /**
     * Interact with this Entity - this is called when it is right-clicked.
     *
     * @param x The absolute mouse X.
     * @param y The absolute mouse Y.
     */
    public void interact(int x, int y) {
        Interactions.interact(this, represented, x - centerX, y - centerY);
    }

    public String toString() {
        return this.represented.path;
    }

    private void register(Object co) {
        switch (represented.type) {
            case RMT_EVENTINPUT:
                ((EventInput) co).send(new EventOutput() {
                    @Override
                    public void event() {
                        countStart = System.currentTimeMillis();
                    }
                });
                break;
            case RMT_BOOLPROD:
                ((BooleanInput) co).send(new BooleanOutput() {
                    @Override
                    public void set(boolean value) {
                        currentValue = value;
                    }
                });
                break;
            case RMT_FLOATPROD:
                ((FloatInput) co).send(new FloatOutput() {
                    @Override
                    public void set(float value) {
                        currentValue = value;
                    }
                });
                currentValue = 0f;
                break;
        }
    }
}
