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
import java.awt.Cursor;
import java.awt.Point;

/**
 * 
 * @author millerv
 */
public class DragSliderPanel extends javax.swing.JPanel {

    EmulatorPin pin;
    //private Point location;
    
    /**
     * Creates new form DragSliderPanel
     * @param pin
     */
    public DragSliderPanel(EmulatorPin pin) {
        this.pin = pin;
        initComponents();
        setBackground(new Color(0.0f, 0.5f, 0.5f));
        setFocusable(true);
    }
    
    /*
    public Point snapToGrid(Point p) {
        Point position = new Point((int)(p.getX() + getWidth()), (int)(p.getY() + getHeight()));
        
        int rows = getParent().getHeight() / this.getHeight();
        int cols = getParent().getWidth() / this.getWidth();
        
        int row = (int)(((double)(position.getY()) / getParent().getHeight()) * rows);
        int col = (int)(((double)(position.getX()) / getParent().getWidth()) * cols);
        
        return new Point(col * getWidth(), row * getHeight());
    }
    */

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        valueSlider = new javax.swing.JSlider();
        valueDisplay = new javax.swing.JTextField();
        removeButton = new javax.swing.JButton();

        setToolTipText("");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        valueSlider.setMaximum(10);
        valueSlider.setMinimum(-10);
        valueSlider.setValue(0);
        valueSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                valueSliderStateChanged(evt);
            }
        });

        valueDisplay.setEditable(false);
        valueDisplay.setText("0.0");

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(valueSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(valueDisplay)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(removeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(valueSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(valueDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void valueSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_valueSliderStateChanged
        float value = valueSlider.getValue() / 10.0f;
        valueDisplay.setText(value + "");
        pin.set(value);
        if (pin.getMode() == EmulatorPin.Mode.ANALOG_IN) {
            setBackground(new Color(0.0f, 0.5f + value / 2, 0.5f + value / 2));
        }
    }//GEN-LAST:event_valueSliderStateChanged

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Container parent = getParent();
        parent.remove(this);
        parent.revalidate();
        parent.repaint();
    }//GEN-LAST:event_removeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField valueDisplay;
    private javax.swing.JSlider valueSlider;
    // End of variables declaration//GEN-END:variables
}
