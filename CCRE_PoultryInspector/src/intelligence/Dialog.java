/*
 * Copyright 2014 Gregor Peach
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

import ccre.util.Utils;
import java.awt.Color;
import java.awt.Graphics;

/**
 * A Dialog that appears on the Intelligence Panel
 *
 * @author peachg
 */
public class Dialog {

    public int x = 400;
    public int y = 100;
    public int xw = 200;
    public int yw = 100;
    public String called;

    public Dialog(String decifer) {
        String[] ars = Utils.split(decifer, '\1');
        called = ars[0];
        String newCall = "";
        int x = 0;
        for (char c : called.toCharArray()) {
            x++;
            newCall += c;
            if (x % 10 == 0) {
                newCall += "\n";
            }
        }
        called = newCall;
    }

    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillRect(x, y, xw, yw);
        g.setColor(Color.BLACK);
        int index = 0;
        int xc = x;
        int yc = y;
        for (String s : called.split("\n")) {
            index++;
            g.drawString(s, xc + 13, yc + 13);
            yc += 10;
        }
        g.drawRect(x - 1, y - 1, xw + 1, yw + 1);
        g.setColor(Color.RED);
        g.fillRect(x + xw - 13, y + 3, 10, 10);
    }
}
