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
package ccre.supercanvas.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ccre.log.CCRELoggerFactory;
import ccre.log.LogLevel;
import ccre.log.LoggingTarget;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;
import ccre.util.LineCollectorOutputStream;

/**
 * A component that displays a scrollable log of recentCCRE logging messages.
 *
 * @author skeggsc
 */
public class LoggingComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = -946247852428245215L;

    private transient List<String> lines;
    private transient PrintStream pstr;
    private transient LoggingTarget tgt;
    private transient ResizeState resizeState;
    private transient int scroll = 0, maxScroll = 0, clearingThreshold = 0;
    private transient boolean isClearing = false;

    /**
     * Create a new LoggingComponent at the specified position.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     */
    public LoggingComponent(int cx, int cy) {
        super(cx, cy);
        halfWidth = 300;
        halfHeight = 95;
        setupLines();
    }

    private void setupLines() {
        resizeState = ResizeState.TRANSLATE;
        lines = new ArrayList<String>(100);
        this.pstr = new PrintStream(new LineCollectorOutputStream() {
            @Override
            protected void collect(String param) {
                synchronized (LoggingComponent.this) {
                    lines.add(param);
                }
            }
        });
        this.tgt = new LoggingTarget() {
            @Override
            public synchronized void log(LogLevel level, String message, Throwable thr) {
                if (thr != null) {
                    pstr.println("{" + level.message + "} " + message);
                    thr.printStackTrace(pstr);
                } else {
                    pstr.println("[" + level.message + "] " + message);
                }
            }

            @Override
            public synchronized void log(LogLevel level, String message, String extended) {
                pstr.println("[" + level.message + "] " + message);
                if (extended != null && !extended.isEmpty()) {
                    pstr.println(extended);
                }
            }
        };
        CCRELoggerFactory.getLogger().addTarget(tgt);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setupLines();
    }

    @Override
    public void moveForDrag(int x, int y) {
        switch (resizeState) {
        case CORNER_BR:
            halfWidth = x;
            halfHeight = y;
            break;
        case CORNER_UR:
            halfWidth = x;
            halfHeight = -y;
            break;
        case CORNER_BL:
            halfWidth = -x;
            halfHeight = y;
            break;
        case CORNER_UL:
            halfWidth = -x;
            halfHeight = -y;
            break;
        case SCROLL:
            scroll = y * -maxScroll / (2 * halfHeight - 24);
            constrainScrolling();
            return;
        default:
            super.moveForDrag(x, y);
            return;
        }
        if (halfWidth < 50) {
            halfWidth = 50;
        }
        if (halfHeight < 50) {
            halfHeight = 50;
        }
    }

    @Override
    public boolean canDrop() {
        return resizeState == ResizeState.TRANSLATE;
    }

    @Override
    public int getDragRelX(int x) {
        switch (resizeState) {
        case CORNER_BR:
        case CORNER_UR:
            return halfWidth - x;
        case CORNER_BL:
        case CORNER_UL:
            return -halfWidth - x;
        case SCROLL:
            return 0;
        default:
            return super.getDragRelX(x);
        }
    }

    @Override
    public int getDragRelY(int y) {
        switch (resizeState) {
        case CORNER_BR:
        case CORNER_BL:
            return halfHeight - y;
        case CORNER_UR:
        case CORNER_UL:
            return -halfHeight - y;
        case SCROLL:
            return (int) ((2 * halfHeight - 24) * (scroll / (float) -maxScroll)) - y;
        default:
            return super.getDragRelY(y);
        }
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        drawBackground(g);
        Shape originalClippingShape = g.getClip();
        int lineCount;
        {
            g.setClip(new Rectangle(centerX - halfWidth + 16, centerY - halfHeight + 16, halfWidth * 2 - 32, halfHeight * 2 - 32));
            lineCount = drawLoggedLines(g, fontMetrics, centerY + halfHeight - 16 - fontMetrics.getDescent() - scroll, centerX - halfWidth + 16, fontMetrics.getHeight());
        }
        g.setClip(originalClippingShape);
        drawScrollbar(lineCount, fontMetrics, g);
        if (isClearing) {
            drawClearingOverlay(g, fontMetrics, mouseX);
        }
    }

    private void drawBackground(Graphics2D g) {
        Rendering.drawBody(Color.LIGHT_GRAY.brighter(), g, this);
        g.setColor(Color.BLACK);
        if (getPanel().editmode) {
            g.drawLine(centerX - halfWidth + 6, centerY - halfHeight + 10, centerX - halfWidth + 10, centerY - halfHeight + 6);
            g.drawLine(centerX + halfWidth - 6, centerY - halfHeight + 10, centerX + halfWidth - 10, centerY - halfHeight + 6);
            g.drawLine(centerX - halfWidth + 6, centerY + halfHeight - 10, centerX - halfWidth + 10, centerY + halfHeight - 6);
            g.drawLine(centerX + halfWidth - 6, centerY + halfHeight - 10, centerX + halfWidth - 10, centerY + halfHeight - 6);
        }
    }

    private void drawScrollbar(int lineCount, FontMetrics fontMetrics, Graphics2D g) {
        this.maxScroll = lineCount * fontMetrics.getHeight() - halfHeight;
        float frac = scroll / (float) -maxScroll;
        if (frac < 0) {
            frac = 0;
        } else if (frac > 1) {
            frac = 1;
        }
        //g.setColor(scroll == 0 ? Color.GREEN : Color.BLACK);
        //g.fillOval(centerX - halfWidth + 4, centerY - halfHeight + 8 + (int) ((2 * halfHeight - 24) * frac), 8, 8);
        Rendering.drawScrollbar(g, scroll != 0, centerX - halfWidth + 8, centerY - halfHeight + 12 + (int) ((2 * halfHeight - 24) * frac));
    }

    private synchronized int drawLoggedLines(Graphics2D g, FontMetrics fontMetrics, int initialYPos, int xPos, int rowHeight) {
        int yPos = initialYPos;
        ArrayList<String> temp = new ArrayList<String>(lines.size() + lines.size() >> 1);
        int lineCount = 0;
        for (int i = lines.size() - 1; i >= 0; i--) {
            g.setColor(Color.BLACK);
            String line = lines.get(i);
            if (fontMetrics.stringWidth(line) > halfWidth * 2 - 32) {
                temp.clear();
                int base = 0;
                int linepos = line.length();
                while (linepos > base) {
                    String substr = line.substring(base, linepos);
                    if (fontMetrics.stringWidth(substr) <= halfWidth * 2 - 32) {
                        temp.add(substr);
                        base = linepos;
                        linepos = line.length() - 1;
                    } else {
                        linepos -= 1;
                    }
                }
                for (int j = temp.size() - 1; j >= 0; j--) {
                    g.drawString(temp.get(j), xPos, yPos);
                    yPos -= rowHeight;
                    lineCount++;
                }
            } else {
                g.drawString(line, xPos, yPos);
                yPos -= rowHeight;
                lineCount++;
            }
        }
        return lineCount;
    }

    private void drawClearingOverlay(Graphics2D g, FontMetrics fontMetrics, int mouseX) {
        Composite composite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.7f));
        Rendering.drawBody(Color.LIGHT_GRAY, g, this);
        g.setComposite(composite);
        String display = "Really clear?";
        int width = fontMetrics.stringWidth(display);
        g.setColor(mouseX < centerX - width / 2 ? Color.RED : Color.GRAY);
        g.drawLine(centerX - halfWidth + 12, centerY - halfHeight + 12, centerX - width / 2 - 2, centerY - fontMetrics.getAscent());
        g.drawLine(centerX - halfWidth + 12, centerY + halfHeight - 12, centerX - width / 2 - 2, centerY - fontMetrics.getAscent() + fontMetrics.getHeight());
        g.setColor(mouseX > centerX + width / 2 ? Color.GREEN.darker() : Color.GRAY);
        g.drawLine(centerX + halfWidth - 12, centerY - halfHeight + 12, centerX + width / 2 + 2, centerY - fontMetrics.getAscent());
        g.drawLine(centerX + halfWidth - 12, centerY + halfHeight - 12, centerX + width / 2 + 2, centerY - fontMetrics.getAscent() + fontMetrics.getHeight());
        g.setColor(Color.BLACK);
        clearingThreshold = centerX - width / 2;
        g.drawString(display, centerX - width / 2, centerY);
        display = "Clear";
        g.setColor(Color.RED);
        g.drawString(display, centerX - halfWidth / 2 - fontMetrics.stringWidth(display) / 2, centerY);
        display = "Keep";
        g.setColor(Color.GREEN.darker());
        g.drawString(display, centerX + halfWidth / 2 - fontMetrics.stringWidth(display) / 2, centerY);
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (isClearing) {
            if (x < clearingThreshold) {
                lines.clear();
            }
            isClearing = false;
        } else if (x < centerX) {
            resizeState = ResizeState.SCROLL;
            getPanel().startDrag(this, x, y);
        } else {
            isClearing = true;
        }
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        if (containsForInteract(x, y)) {
            return onInteract(x, y);
        }
        resizeState = ResizeState.TRANSLATE;
        if (x >= centerX + halfWidth - 10) {
            if (y >= centerY + halfHeight - 10) {
                resizeState = ResizeState.CORNER_BR;
            } else if (y <= centerY - halfHeight + 10) {
                resizeState = ResizeState.CORNER_UR;
            }
        } else if (x <= centerX - halfWidth + 10) {
            if (y >= centerY + halfHeight - 10) {
                resizeState = ResizeState.CORNER_BL;
            } else if (y <= centerY - halfHeight + 10) {
                resizeState = ResizeState.CORNER_UL;
            } else {
                resizeState = ResizeState.SCROLL;
            }
        }
        getPanel().startDrag(this, x, y);
        return true;
    }

    @Override
    public boolean onScroll(int x, int y, int wheelRotation) {
        scroll += wheelRotation;
        constrainScrolling();
        return true;
    }

    private void constrainScrolling() {
        if (scroll > 0) {
            scroll = 0;
        } else if (scroll < -maxScroll) {
            scroll = -maxScroll;
        }
    }

    @Override
    public String toString() {
        return lines == null ? "deactivated logging window" : "logging window [" + lines.size() + "]";
    }

    @Override
    public boolean onDelete(boolean forced) {
        if (tgt != null) {
            CCRELoggerFactory.getLogger().removeTarget(tgt);
            tgt = null;
            pstr = null;
            lines = null;
        }
        return true;
    }

    private static enum ResizeState {

        TRANSLATE, CORNER_BR, CORNER_UR, CORNER_BL, CORNER_UL, SCROLL
    }
}
