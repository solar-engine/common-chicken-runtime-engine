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
import ccre.ctrl.ExpirationTimer;
import intelligence.Rendering;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
public final class SuperCanvasPanel extends JPanel implements MouseMotionListener, MouseWheelListener, MouseListener {

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
    private transient final LinkedHashSet<SuperCanvasComponent> mouseOver = new LinkedHashSet<SuperCanvasComponent>();
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

    /*public void save(DataOutputStream dout) throws IOException {
     dout.writeInt(0xC00FFEED);
     SuperCanvasComponent[] out = components.toArray(new SuperCanvasComponent[components.size()]);
     dout.writeInt(out.length);
     for (SuperCanvasComponent elem : out) {
     dout.writeUTF(elem.getClass().getName());
     elem.save(dout);
     }
     dout.writeInt(0xA11DEAD);
     }

     public void load(DataInputStream din) throws IOException {
     components.clear();
     if (din.readInt() != (int) 0xC00FEED) {
     throw new IOException("Bad magic number!");
     }
     int count = din.readInt();
     if (count < 0 || count > 65536) {
     throw new IOException("Bad count!");
     }
     while (count-- > 0) {
     try {
     Class<? extends SuperCanvasComponent> cf = Class.forName(din.readUTF()).asSubclass(SuperCanvasComponent.class);
     Method mthd = cf.getMethod("loadCanvasComponent", DataInputStream.class);
     Object comp = mthd.invoke(null, din);
     if (!(comp instanceof SuperCanvasComponent)) {
     throw new IOException("Result from loadCanvasComponent is not a SuperCanvasComponent!");
     }
     components.add((SuperCanvasComponent) comp);
     } catch (ClassNotFoundException ex) {
     throw new IOException("Cannot load serialized class", ex);
     } catch (NoSuchMethodException ex) {
     throw new IOException("Cannot load deserialization method", ex);
     } catch (IllegalAccessException ex) {
     throw new IOException("Cannot access deserialization method", ex);
     } catch (IllegalArgumentException ex) {
     throw new IOException("Cannot invoke deserialization method", ex);
     } catch (InvocationTargetException ex) {
     throw new IOException("Cannot deserialize component", ex);
     }
     }
     if (din.readInt() != (int) 0xA11DEAD) {
     throw new IOException("Bad MAGIC post-number when deserializing!");
     }
     }*/
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
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
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
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (dragBtn == MouseEvent.BUTTON3) {
            for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                SuperCanvasComponent comp = it.previous();
                if (comp.contains(e.getX(), e.getY())) {
                    if (comp.onInteract(e.getX(), e.getY())) {
                        break;
                    }
                }
            }
            repaint();
        } else if (activeEntity != null) {
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
            repaint();
        } else {
            for (ListIterator<SuperCanvasComponent> it = components.listIterator(components.size()); it.hasPrevious();) {
                SuperCanvasComponent comp = it.previous();
                if (comp.wantsDragSelect() && comp.contains(e.getX(), e.getY())) {
                    if (comp.onSelect(e.getX(), e.getY())) {
                        break;
                    }
                }
            }
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean mod = false;
        this.mouseX = e.getX();
        this.mouseY = e.getY();
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
    public void mouseClicked(MouseEvent e) {
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
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
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

    public void startDrag(SuperCanvasComponent comp, int x, int y) {
        activeEntity = comp;
        relActiveX = comp.getDragRelX(x);
        relActiveY = comp.getDragRelY(y);
    }

    private void renderBackground(Graphics2D g, int w, int h, FontMetrics fontMetrics, int mouseX, int mouseY) {
        /*float[] gf = {0, 1};
         Color[] gc = {Color.ORANGE, Color.RED};
         g.setPaint(new RadialGradientPaint(w / 3f, h / 3f, (w + h) / 2f, gf, gc));*/
        g.setPaint(new GradientPaint(0, h, Color.ORANGE, w, 0, Color.RED));
        //g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
    }

    public void save(ObjectOutputStream out) throws IOException {
        out.writeObject(components);
    }

    public void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Remove components
        for (Iterator<SuperCanvasComponent> it = components.iterator(); it.hasNext();) {
            SuperCanvasComponent comp = it.next();
            comp.unsetPanel(this);
            comp.onDelete(true);
            it.remove();
            if (comp == activeEntity) {
                activeEntity = null;
            }
            mouseOver.remove(comp);
        }
        // Load new components
        components.addAll((Collection<? extends SuperCanvasComponent>) in.readObject());
        for (SuperCanvasComponent comp : components) {
            comp.setPanel(this);
        }
        repaint();
    }
}
