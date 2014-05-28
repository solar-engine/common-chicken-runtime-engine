/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.channel.FloatOutput;
import static ccre.cluck.CluckNode.RMT_BOOLOUTP;
import static ccre.cluck.CluckNode.RMT_BOOLPROD;
import static ccre.cluck.CluckNode.RMT_EVENTINPUT;
import static ccre.cluck.CluckNode.RMT_EVENTOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD;
import static ccre.cluck.CluckNode.RMT_INVOKE;
import static ccre.cluck.CluckNode.RMT_LOGTARGET;
import static ccre.cluck.CluckNode.RMT_OUTSTREAM;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JOptionPane;

/**
 * A class to handle interactions with Entities.
 *
 * @author skeggsc
 */
public class Interactions {

    /**
     * Interact with this Entity - this is called when it is right-clicked.
     *
     * @param ent The entity to interact with.
     * @param remote The entity's remote.
     * @param x The relative mouse X.
     * @param y The relative mouse Y.
     */
    public static void interact(Entity ent, Remote remote, int x, int y) {
        Object co = remote.checkout;
        if (co == null) {
            return;
        }
        switch (remote.type) {
            case RMT_EVENTOUTP:
                ((EventOutput) co).event();
                ent.countStart = System.currentTimeMillis();
                break;
            case RMT_EVENTINPUT:
                // Interacting with this wouldn't mean anything.
                break;
            case RMT_LOGTARGET:
                String msg = JOptionPane.showInputDialog("Enter message to log", "");
                if (msg != null && !msg.isEmpty()) {
                    ((LoggingTarget) co).log(LogLevel.INFO, msg, (String) null);
                }
                break;
            case RMT_BOOLPROD:
                Object o = remote.paired.checkout();
                if (o instanceof BooleanOutput) {
                    ((BooleanOutput) o).set(x < 0);
                }
                break;
            case RMT_BOOLOUTP:
                BooleanOutput bo = (BooleanOutput) co;
                boolean nw = x < 0;
                if (ent.currentValue == null || (Boolean) ent.currentValue != nw || System.currentTimeMillis() - ent.countStart >= 200) {
                    bo.set(nw);
                    ent.currentValue = nw;
                    ent.countStart = System.currentTimeMillis();
                }
                break;
            case RMT_FLOATPROD:
                o = remote.paired.checkout();
                if (o instanceof FloatOutput) {
                    FloatOutput fo = (FloatOutput) o;
                    float f = x / (float) ent.width;
                    if (y < 0) {
                        try {
                            String jop = JOptionPane.showInputDialog("Enter a number", "");
                            f = Float.parseFloat(jop);
                        } catch (NumberFormatException ex) {
                            Logger.warning("Cannot write new value!", ex);
                            break;
                        }
                    }
                    fo.set(f);
                }
                break;
            case RMT_FLOATOUTP:
                FloatOutput fo = (FloatOutput) co;
                float f = x / (float) ent.width;
                if (y < 0) {
                    try {
                        String jop = JOptionPane.showInputDialog("Enter a number", "");
                        f = Float.parseFloat(jop);
                    } catch (NumberFormatException ex) {
                        Logger.warning("Cannot write new value!", ex);
                        break;
                    }
                }
                fo.set(f);
                ent.currentValue = f;
                ent.countStart = System.currentTimeMillis();
                break;
            case RMT_OUTSTREAM:
                OutputStream outs = (OutputStream) co;
                try {
                    String raw = JOptionPane.showInputDialog("Modify value", "*");
                    if (raw == null) {
                        Logger.warning("No value sent.");
                    } else {
                        outs.write((raw + "\n").getBytes());
                        outs.flush();
                    }
                } catch (IOException ex) {
                    Logger.warning("Cannot write new value!", ex);
                }
                break;
            case RMT_INVOKE:
                Logger.info("Cannot interact with RemoteProcedures!");
                break;
        }
    }

}
