/*
 * Copyright 2016 Cel Skeggs
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
package ccre.viewer;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import ccre.channel.CancelOutput;
import ccre.channel.EventCell;
import ccre.cluck.Cluck;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.SuperCanvasPanel;
import ccre.util.LineCollectorOutputStream;

/**
 * A webcam viewer component, either controlled locally or across the network by robot code.
 *
 * @author skeggsc
 */
public class WebcamComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = 6555290483696874964L;
    private String address = null;
    private final StringBuilder addressField = new StringBuilder();
    private transient WebcamThread webcam;
    private transient BufferedImage image;
    private transient volatile String error = "Connecting...";
    private boolean flipH, flipV;
    private String cluckLink;
    private transient CancelOutput cluckCancel;

    private static final class CluckEntry {
        private final EventCell onChange = new EventCell();
        private String address;

        public CluckEntry(String linkName) {
            Cluck.subscribe(linkName, new LineCollectorOutputStream() {
                @Override
                protected void collect(String param) {
                    address = param;
                    onChange.event();
                }
            });
            // because this happens at runtime
            Cluck.getNode().notifyNetworkModified();
        }

        public CancelOutput connect(WebcamComponent component) {
            return onChange.send(() -> {
                component.setAddress(address);
            });
        }
    }

    private static final HashMap<String, CluckEntry> ents = new HashMap<>();

    private static synchronized CluckEntry getEntry(String name) {
        CluckEntry ent = ents.get(name);
        if (ent == null) {
            ent = new CluckEntry(name);
            ents.put(name, ent);
        }
        return ent;
    }

    /**
     * Create a new WebcamComponent at the specified position.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     */
    public WebcamComponent(int cx, int cy) {
        super(cx, cy);
        halfWidth = 100;
        halfHeight = 100;
    }

    private synchronized void setImage(BufferedImage image) {
        this.image = image;
        if (image != null) {
            this.halfWidth = image.getWidth() / 2;
            this.halfHeight = image.getHeight() / 2;
        }
        SuperCanvasPanel panel = this.getPanel();
        if (panel != null) {
            panel.repaint();
        }
    }

    private synchronized void setCluckLink(String link) {
        if (this.cluckCancel != null) {
            this.cluckCancel.cancel();
            this.cluckCancel = null;
        }
        this.cluckLink = link;
        if (link != null) {
            this.cluckCancel = getEntry(link).connect(this);
        }
    }

    private synchronized void setAddress(String address) {
        if (address != null && address.isEmpty()) {
            address = null;
        }
        this.address = address;
        if (this.webcam != null) {
            this.webcam.setAddress(address);
        }
        setImage(null);
    }

    private void setError(String error) {
        if (error == null) {
            error = "";
        }
        this.error = error;
        SuperCanvasPanel panel = this.getPanel();
        if (panel != null) {
            panel.repaint();
        }
    }

    @Override
    public synchronized void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        int x0 = centerX - halfWidth, y0 = centerY - halfHeight;
        g.setColor(Color.BLACK);
        g.fillRect(x0, y0, halfWidth * 2, halfHeight * 2);
        g.setColor(Color.WHITE);
        g.drawRect(x0, y0, halfWidth * 2, halfHeight * 2);
        BufferedImage image = this.image;
        if (image != null) {
            g.drawImage(image, x0, y0, x0 + halfWidth * 2, y0 + halfHeight * 2, flipH ? image.getWidth() : 0, flipV ? image.getHeight() : 0, flipH ? 0 : image.getWidth(), flipV ? 0 : image.getHeight(), null);
        }

        g.drawString(this.address + (this.cluckLink == null ? "" : " :" + this.cluckLink), x0 + 3, y0 + halfHeight * 2 - 3);
        if (getPanel().editing == addressField) {
            String text = this.addressField.toString() + "|";
            g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, centerY);
        }
        String text = this.error;
        g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, centerY + g.getFontMetrics().getHeight());
    }

    @Override
    public void onPressedEnter() {
        if (getPanel().editing == addressField) {
            if (addressField.length() > 0 && addressField.charAt(0) == ':') {
                setCluckLink(addressField.substring(1));
            } else {
                setCluckLink(null);
                setAddress(addressField.toString());
            }
            getPanel().editing = null;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        boolean used = false;
        if (Math.abs(x - centerX) >= halfWidth - 10) {
            flipH = !flipH;
            used = true;
        }
        if (Math.abs(y - centerY) >= halfHeight - 10) {
            flipV = !flipV;
            used = true;
        }
        if (!used) {
            if (getPanel().editing == addressField) {
                onPressedEnter();
            } else {
                addressField.setLength(0);
                if (cluckLink != null) {
                    addressField.append(":").append(cluckLink);
                } else if (address != null) {
                    addressField.append(address);
                }
                getPanel().editing = addressField;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "camera " + address;
    }

    @Override
    protected synchronized void onChangePanel(SuperCanvasPanel panel) {
        if (webcam != null && panel == null) {
            if (this.cluckCancel != null) {
                this.cluckCancel.cancel();
                this.cluckCancel = null;
            }
            webcam.terminate();
            webcam = null;
        } else if (webcam == null && panel != null) {
            setCluckLink(cluckLink);
            webcam = new WebcamThread(this::setImage, this::setError);
            webcam.setAddress(address);
        }
    }
}
