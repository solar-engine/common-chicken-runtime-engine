/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian;

import java.awt.Color;
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

    public EmulatorPin(int id, float val) {
        this.id = id;
        setMode(Mode.GPIO_IN);
        set(val);
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
        } else {
            g.setColor(new Color(1.0f, 0.0f, 0.0f));
        }
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth(), getHeight());
        g.drawString(new Integer(id).toString(), 0, getHeight());

    }
    
    public void updateToolTipText() {
        setToolTipText("Pin " + id + ": mode=" + mode.name() + ", value=" + val);
    }

    public void set(boolean val) {
        if (val) {
            this.val = 1.0f;
        } else {
            this.val = 0.0f;
        }
        repaint();
        updateToolTipText();
    }

    public void set(float val) {
        this.val = val;
        repaint();
        updateToolTipText();
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
        }
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

        GPIO_OUT, GPIO_IN, PWM, UNUSED
    }
}
