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
package ccre.supercanvas.components.channels;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;

/**
 * A component allowing for writing to an output stream, in either textual or
 * binary mode.
 *
 * @author skeggsc
 */
public class OutputStreamControlComponent extends DraggableBoxComponent {
    private static Logger logger = LoggerFactory.getLogger(OutputStreamControlComponent.class);

    private static final long serialVersionUID = 800737743696942747L;
    private final OutputStream out;
    private final String name;
    private final StringBuilder contents = new StringBuilder();
    private boolean inBinaryMode = false;

    /**
     * Create a new OutputStreamControlComponent with an OutputStream to write
     * to.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the input.
     * @param out the OutputStream to write to.
     */
    public OutputStreamControlComponent(int cx, int cy, String name, OutputStream out) {
        super(cx, cy);
        this.name = name;
        this.out = out;
    }

    private void setHalfWidth(int halfWidth) {
        int min = getPanel().getWidth() / 4;
        if (halfWidth < this.halfWidth) {
            if (this.halfWidth > min) {
                halfWidth = Math.max(halfWidth, min);
            } else {
                return;
            }
        }
        this.centerX += halfWidth - this.halfWidth;
        this.halfWidth = halfWidth;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        int conHeight = g.getFontMetrics().getHeight();
        g.setFont(Rendering.labels);
        checkContents(true);
        String render = (inBinaryMode ? "0x" : "> ") + contents.toString();
        setHalfWidth(5 + Math.max(g.getFontMetrics().stringWidth(render) / 2, g.getFontMetrics(Rendering.console).stringWidth(name)));
        this.halfHeight = g.getFontMetrics().getHeight() / 2 + conHeight / 2;
        if (getPanel().editing == contents || getPanel().editmode) {
            Rendering.drawBody(getPanel().editing == contents ? Color.GREEN : Color.YELLOW, g, this);
        }
        g.setColor(Color.BLACK);
        g.drawString(render, centerX - halfWidth + 5, centerY + 5 + conHeight / 2);
        int yh = g.getFontMetrics().getHeight();
        g.setColor(Color.BLACK);
        g.setFont(Rendering.console);
        g.drawString(name, centerX - halfWidth + 5, centerY + 5 - yh / 2);
    }

    private int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else {
            return -1;
        }
    }

    private void checkContents(boolean allowSpaces) {
        if (inBinaryMode) {
            for (int i = 0; i < contents.length(); i++) {
                char c = contents.charAt(i);
                if (decodeHexDigit(c) == -1 && !(c == ' ' && allowSpaces)) {
                    contents.deleteCharAt(i--);
                }
            }
        }
    }

    private byte[] decodeHex() {
        checkContents(false);
        byte[] decoded = new byte[contents.length() / 2];
        for (int i = 0; i < decoded.length; i++) {
            int a = decodeHexDigit(contents.charAt(i * 2)), b = decodeHexDigit(contents.charAt(i * 2 + 1));
            if (a == -1 || b == -1) {
                logger.warn("Undiscovered bad hex digit in sendable.");
            } else {
                decoded[i] = (byte) ((a << 4) | b);
            }
        }
        logger.info("Decoded " + decoded.length + " bytes.");
        return decoded;
    }

    @Override
    public void onPressedEnter() {
        checkContents(true);
        if (getPanel().editing == contents) {
            if (contents.length() == 0) {
                inBinaryMode = !inBinaryMode;
                return;
            }
            try {
                if (inBinaryMode) {
                    out.write(decodeHex());
                    out.flush();
                } else {
                    out.write((contents + "\n").getBytes());
                }
            } catch (IOException e) {
                logger.warn("Error while writing to OutputStream " + name, e);
            }
            contents.setLength(0);
            getPanel().editing = null;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (getPanel().editing == contents) {
            getPanel().editing = null;
        } else {
            getPanel().editing = contents;
        }
        return true;
    }
}
