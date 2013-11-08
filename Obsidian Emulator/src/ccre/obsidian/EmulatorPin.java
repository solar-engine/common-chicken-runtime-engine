/*
 * Copyright 2013 Vincent Miller
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
package ccre.obsidian;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 *
 * @author millerv
 */
public class EmulatorPin extends JPanel implements MouseListener {

    private Mode mode;
    private float val;
    private final int id;
    private final Container sliderPanelTarget;
    private DragSliderPanel dsp = null;
    private final EmulatorLauncher launcher;

    public EmulatorPin(EmulatorLauncher launcher, int id, Container c, String header) {
        this.id = id;
        this.sliderPanelTarget = c;
        this.launcher = launcher;
        setName(header + "_" + id);
        this.val = 0.0f;
        setMode(Mode.UNUSED);
        setPreferredSize(new Dimension(16, 16));
        setFocusable(true);
        addMouseListener(this);
    }

    @Override
    public void addNotify() {
        requestFocusInWindow();
        super.repaint();
        repaint();
        super.addNotify();
    }

    @Override
    public void paintComponent(Graphics g) {
        float colorValue = 0.5F + (val / 2);
        if (mode == Mode.GPIO_IN) {
            g.setColor(new Color(0.0f, colorValue, 0.0f));
        } else if (mode == Mode.GPIO_OUT) {
            g.setColor(new Color(0.0f, 0.0f, colorValue));
        } else if (mode == Mode.PWM) {
            g.setColor(new Color(colorValue, colorValue, 0.0f));
        } else if (mode == Mode.ANALOG_IN) {
            g.setColor(new Color(0.0f, colorValue, colorValue));
        } else {
            g.setColor(new Color(1.0f, 0.0f, 0.0f));
        }
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth(), getHeight());
        g.drawString(new Integer(id).toString(), 0, getHeight());

    }

    public void updateToolTipText() {
        String text = getName() + ": mode=" + mode.name();
        if (mode == Mode.GPIO_OUT || mode == Mode.GPIO_IN) {
            text += ", value=" + getBoolean();
        } else if (mode == Mode.PWM || mode == Mode.ANALOG_IN) {
            text += ", value=" + getFloat();
        }
        setToolTipText(text);
    }

    public void set(boolean val) {
        if (val == (this.val == 1.0f)) {
            return;
        }
        if (val) {
            this.val = 1.0f;
        } else {
            this.val = 0.0f;
        }
        repaint();
        updateToolTipText();
        if (mode == Mode.GPIO_OUT || mode == Mode.PWM) {
            launcher.pinChanged(this);
        }
        if (dsp != null) {
            dsp.updateValue();
        }
    }

    public void set(float val) {
        if (val == this.val) {
            return;
        }
        this.val = val;
        repaint();
        updateToolTipText();
        if (mode == Mode.GPIO_OUT || mode == Mode.PWM) {
            launcher.pinChanged(this);
        }
        if (dsp != null) {
            dsp.updateValue();
        }
    }

    public Mode getMode() {
        return mode;
    }

    public boolean getBoolean() {
        return val == 1.0f;
    }

    public float getFloat() {
        return val;
    }

    public void setMode(Mode m) {
        mode = m;
        updateToolTipText();
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (mode == Mode.GPIO_IN) {
            set(!getBoolean());
        } else if (mode == Mode.ANALOG_IN && dsp == null) {
            dsp = new DragSliderPanel(this);
            sliderPanelTarget.add(dsp);
            sliderPanelTarget.revalidate();
        }
    }
    
    public void removeSliderPanel() {
        sliderPanelTarget.remove(dsp);
        dsp = null;
        sliderPanelTarget.revalidate();
        sliderPanelTarget.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public enum Mode {

        GPIO_OUT, GPIO_IN, PWM, ANALOG_IN, UNUSED
    }
}
