/*
 * Copyright 2014 Colby Skeggs, Gregor Peach (Added Folders)
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

import ccre.concurrency.CollapsingWorkerThread;
import java.awt.EventQueue;
import javax.swing.JScrollBar;

public class RescrollingThread extends CollapsingWorkerThread {

    private JScrollBar scroll;

    public RescrollingThread() {
        super("Rescroller", false);
    }

    @Override
    protected void doWork() throws Throwable {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                scroll.setValue(getScrollBar().getMaximum() - getScrollBar().getVisibleAmount());
            }
        });
    }

    public JScrollBar getScrollBar() {
        return scroll;
    }

    public void setScrollBar(JScrollBar scroll) {
        this.scroll = scroll;
    }

    public boolean shouldRescroll() {
        return scroll.getValue() + scroll.getVisibleAmount() + 16 >= scroll.getMaximum();
    }
}
