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
package treeframe;

import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.ctrl.ExpirationTimer;
import intelligence.Rendering;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.JPanel;

/**
 * A base display panel used in tree-and-canvas panels. "SuperCanvas" means that
 * it's a superset of the canvas space.
 *
 * @author skeggsc
 */
public final class SuperCanvasPanel extends JPanel {

    /**
     * The currently held component.
     */
    private transient SuperCanvasComponent activeEntity = null;
    /**
     * The currently visible list of components.
     */
    private final LinkedList<SuperCanvasComponent> components = new LinkedList<SuperCanvasComponent>();
    /**
     * The components currently being hovered over by the mouse.
     */
    private transient final LinkedHashSet<SuperCanvasComponent> mouseOver = new LinkedHashSet<SuperCanvasComponent>(4);
    /**
     * The relative position between the cursor and the currently held
     * component.
     */
    private transient int relActiveX, relActiveY;
    /**
     * The most recent position of the mouse.
     */
    private transient int mouseX, mouseY;
    /**
     * The mouse button pressed while dragging a component.
     */
    private transient int dragBtn;
    /**
     * An expiration timer to repaint the pane when appropriate.
     */
    private transient ExpirationTimer painter;

    /**
     * Add the specified component to this panel.
     *
     * @param comp The component to add.
     */
    public synchronized void add(SuperCanvasComponent comp) {
        comp.setPanel(this);
        components.add(comp);
        repaint();
    }

    /**
     * Raise the specified component to the top of the display stack.
     *
     * @param comp the component to raise.
     */
    public synchronized void raise(SuperCanvasComponent comp) {
        if (components.remove(comp)) {
            components.add(comp);
            repaint();
        }
    }

    /**
     * Remove the specified component from this panel.
     *
     * @param comp The component to remove.
     */
    public synchronized void remove(SuperCanvasComponent comp) {
        comp.unsetPanel(this);
        if (components.remove(comp)) {
            if (comp == activeEntity) {
                activeEntity = null;
            }
            mouseOver.remove(comp);
            repaint();
        }
    }

    /**
     * Start the IntelligenceMain instance so that it runs.
     */
    public void start() {
        MouseAdapter listener = new SuperCanvasMouseAdapter();
        this.addMouseWheelListener(listener);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
        painter = new ExpirationTimer();
        painter.schedule(100, new EventOutput() {
            @Override
            public void event() {
                repaint();
            }
        });
        painter.start();
    }

    @Override
    public void paint(Graphics go) {
        Graphics2D g = (Graphics2D) go;
        int w = getWidth();
        int h = getHeight();
        g.setFont(Rendering.console);
        FontMetrics fontMetrics = g.getFontMetrics();
        renderBackground(g, w, h, fontMetrics, mouseX, mouseY);
        for (SuperCanvasComponent comp : components) {
            comp.render(g, w, h, fontMetrics, mouseX, mouseY);
        }
        if (painter != null) {
            painter.feed();
        } else {
            String navail = "Panel Not Started";
            g.setColor(Color.BLACK);
            g.drawString(navail, w / 2 - fontMetrics.stringWidth(navail) / 2, h / 2 - fontMetrics.getHeight() / 2);
        }
    }

    /**
     * Start a drag operation on the specified component. This involves asking
     * it for its drag offsets and beginning the drag.
     *
     * @param component the component to drag.
     * @param x the mouse X coordinate.
     * @param y the mouse Y coordinate.
     * @see SuperCanvasComponent#getDragRelX(int)
     * @see SuperCanvasComponent#getDragRelY(int)
     */
    public void startDrag(SuperCanvasComponent component, int x, int y) {
        activeEntity = component;
        relActiveX = component.getDragRelX(x);
        relActiveY = component.getDragRelY(y);
    }

    private void renderBackground(Graphics2D g, int w, int h, FontMetrics fontMetrics, int mouseX, int mouseY) {
        /*float[] gf = {0, 1};
         Color[] gc = {Color.ORANGE, Color.RED};
         g.setPaint(new RadialGradientPaint(w / 3f, h / 3f, (w + h) / 2f, gf, gc));*/
        g.setPaint(new GradientPaint(0, h, Color.ORANGE, w, 0, Color.RED));
        //g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
    }

    /**
     * Save the layout of this panel to the specified ObjectOutputStream.
     *
     * @param out the stream to write to.
     * @throws IOException if the contents cannot be saved.
     */
    public void save(ObjectOutputStream out) throws IOException {
        out.writeObject(components);
    }

    /**
     * Load the layout of this panel from the specified ObjectInputStream. This
     * deletes all of the current contents of the panel!
     *
     * @param in the stream to read from.
     * @throws IOException if the contents cannot be loaded.
     * @throws java.lang.ClassNotFoundException if the contents cannot be
     * loaded.
     */
    public void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Remove components
        for (Iterator<SuperCanvasComponent> it = components.iterator(); it.hasNext();) {
            SuperCanvasComponent comp = it.next();
            comp.unsetPanel(this);
            comp.onDelete(true);
            it.remove();
        }
        activeEntity = null;
        mouseOver.clear();
        // Load new components
        components.addAll((Collection<? extends SuperCanvasComponent>) in.readObject());
        for (SuperCanvasComponent comp : components) {
            comp.setPanel(this);
        }
        Cluck.getNode().notifyNetworkModified();
        repaint();
    }

    /**
     * (Safely) remove all instances of the specified class (or any subclass)
     * present in this panel.
     *
     * @param componentType the type of component to remove.
     * @return if any components were removed by this operation.
     */
    public boolean removeAll(Class<? extends SuperCanvasComponent> componentType) {
        boolean any = false;
        for (Iterator<SuperCanvasComponent> it = components.iterator(); it.hasNext();) {
            SuperCanvasComponent comp = it.next();
            if (componentType.isAssignableFrom(comp.getClass())) {
                comp.unsetPanel(this);
                comp.onDelete(true);
                it.remove();
                if (comp == activeEntity) {
                    activeEntity = null;
                }
                mouseOver.remove(comp);
                any = true;
            }
        }
        return any;
    }

    private class SuperCanvasMouseAdapter extends MouseAdapter {

        SuperCanvasMouseAdapter() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragBtn = e.getButton();
            if (e.getButton() == MouseEvent.BUTTON3) {
                for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                    SuperCanvasComponent comp = it.previous();
                    if (comp.contains(e.getX(), e.getY())) {
                        if (comp.onInteract(e.getX(), e.getY())) {
                            break;
                        }
                    }
                }
            } else {
                for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                    SuperCanvasComponent comp = it.previous();
                    if (comp.contains(e.getX(), e.getY())) {
                        raise(comp);
                        if (comp.onSelect(e.getX(), e.getY())) {
                            break;
                        }
                    }
                }
            }
        }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (activeEntity != null) {
                    for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                        SuperCanvasComponent comp = it.previous();
                        if (comp != activeEntity && comp.contains(e.getX(), e.getY())) {
                            if (activeEntity.canDrop() && comp.onReceiveDrop(e.getX(), e.getY(), activeEntity)) {
                                break;
                            }
                        }
                    }
                    activeEntity = null;
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                    SuperCanvasComponent comp = it.previous();
                    if (comp.contains(e.getX(), e.getY())) { // TODO: Does the X, Y get set for this event?
                        if (comp.onScroll(e.getX(), e.getY(), e.getWheelRotation())) {
                            break;
                        }
                    }
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (dragBtn == MouseEvent.BUTTON3) {
                    dragToInteract();
                } else if (activeEntity != null) {
                    dragToMove(e);
                } else {
                    dragToSelect(e);
                }
                repaint();
            }

            private void dragToSelect(MouseEvent e) {
                for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                    SuperCanvasComponent comp = it.previous();
                    if (comp.wantsDragSelect() && comp.contains(e.getX(), e.getY())) {
                        if (comp.onSelect(e.getX(), e.getY())) {
                            break;
                        }
                    }
                }
            }

        private void dragToMove(MouseEvent e) {
            int gx = e.getX(), gy = e.getY();
            if (gx < 5) {
                gx = 5;
            } else if (gx > getWidth() - 5) {
                gx = getWidth() - 5;
            }
            if (gy < 5) {
                gy = 5;
            } else if (gy > getHeight() - 5) {
                gy = getHeight() - 5;
            }
            activeEntity.moveForDrag(relActiveX + gx, relActiveY + gy);
        }

        private void dragToInteract() {
            for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                SuperCanvasComponent comp = it.previous();
                if (comp.contains(mouseX, mouseY)) {
                    if (comp.onInteract(mouseX, mouseY)) {
                        break;
                    }
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            boolean mod = false;
            mouseX = e.getX();
            mouseY = e.getY();
            for (SuperCanvasComponent cmp : components) {
                if (cmp.contains(e.getX(), e.getY())) {
                    if (mouseOver.add(cmp)) {
                        mod |= cmp.onMouseEnter(e.getX(), e.getY());
                    } else {
                        mod |= cmp.onMouseMove(e.getX(), e.getY());
                    }
                } else {
                    if (mouseOver.remove(cmp)) {
                        mod |= cmp.onMouseExit(e.getX(), e.getY());
                    }
                }
            }
            if (mod) {
                repaint();
            }
        }
    }
}
