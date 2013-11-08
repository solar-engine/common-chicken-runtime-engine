/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccre.obsidian;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import javax.swing.JPanel;

/**
 *
 * @author MillerV
 */
public class DragPanel extends JPanel {

    private Point anchor;

    public DragPanel() {
        init();
    }

    private void init() {
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                int anchorX = anchor.x;
                int anchorY = anchor.y;

                Point parentOnScreen = getParent().getLocationOnScreen();
                Point mouseOnScreen = evt.getLocationOnScreen();
                Point position = new Point(mouseOnScreen.x - parentOnScreen.x - anchorX, mouseOnScreen.y - parentOnScreen.y - anchorY);
                setLocation(position);
            }
        });
        
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                anchor = evt.getPoint();
            }
        });
    }
}
