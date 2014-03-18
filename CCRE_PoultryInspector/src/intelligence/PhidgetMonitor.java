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

import ccre.chan.BooleanInput;
import ccre.chan.BooleanOutput;
import ccre.chan.BooleanStatus;
import ccre.chan.FloatInput;
import ccre.chan.FloatStatus;
import ccre.cluck.CluckNode;
import ccre.event.EventConsumer;
import ccre.holders.StringHolder;
import ccre.log.LogLevel;
import ccre.log.Logger;
import com.phidgets.*;
import com.phidgets.event.*;

/**
 * The interface to the Phidget system. Currently, this has hardcoded constants
 * for the number of inputs and outputs. If you need different amounts, it would
 * be a good time to extend this to support arbitrary amounts.
 *
 * @author skeggsc
 */
public class PhidgetMonitor implements IPhidgetMonitor, AttachListener, DetachListener, ErrorListener, InputChangeListener, SensorChangeListener {

    /**
     * The number of binary outputs to expect on the phidget interface.
     */
    public static final int OUTPUT_COUNT = 8;
    /**
     * The number of binary inputs to expect on the phidget interface.
     */
    public static final int INPUT_COUNT = 8;
    /**
     * The number of analog inputs to expect on the phidget interface.
     */
    public static final int ANALOG_COUNT = 8;
    /**
     * The number of lines to expect on the phidget LCD.
     */
    public static final int LCD_LINES = 2;
    /**
     * The number of columns to expect on the phidget LCD.
     */
    public static final int LCD_WIDTH = 20;
    /**
     * The rate to set on the inputs.
     */
    public static final int INPUT_RATE = 20;
    /**
     * The LCD handle for the phidget.
     */
    protected TextLCDPhidget lcd;
    /**
     * The InterfaceKit handle for the phidget.
     */
    protected InterfaceKitPhidget ifa;
    /**
     * The boolean outputs to the phidget.
     */
    public BooleanOutput[] outputs = new BooleanOutput[OUTPUT_COUNT];
    /**
     * The current values for the boolean outputs.
     */
    protected boolean[] outvals = new boolean[OUTPUT_COUNT];
    /**
     * The generated string to fill each row with.
     */
    protected String fillLine;
    /**
     * The lines of the LCD.
     */
    public StringHolder[] lines = new StringHolder[LCD_LINES];
    /**
     * The internal status of whether or not the phidget is attached.
     */
    protected BooleanStatus attachStat = new BooleanStatus();
    /**
     * The external status of whether or not the phidget is attached.
     */
    public BooleanInput isAttached;
    /**
     * The binary inputs that the phidget provides.
     */
    public BooleanInput[] inputs = new BooleanInput[INPUT_COUNT];
    /**
     * The BooleanStatuses behind the phidget's inputs.
     */
    protected BooleanStatus[] inputStats = new BooleanStatus[INPUT_COUNT];
    /**
     * The analog inputs that the phidget provides.
     */
    public FloatInput[] analogs = new FloatInput[ANALOG_COUNT];
    /**
     * The FloatStatuses behind the phidget's input.
     */
    protected FloatStatus[] analogStats = new FloatStatus[ANALOG_COUNT];

    /**
     * Create a new PhidgetMonitor.
     */
    public PhidgetMonitor() {
        isAttached = attachStat;
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            final int cur = i;
            outputs[i] = new BooleanOutput() {
                @Override
                public void writeValue(boolean bln) {
                    outvals[cur] = bln;
                    updateBooleanOutput(cur);
                }
            };
        }
        char[] fillchars = new char[LCD_WIDTH];
        for (int j = 0; j < LCD_WIDTH; j++) {
            fillchars[j] = '.';
        }
        String fillstr = new String(fillchars);
        for (int j = 0; j < LCD_WIDTH; j++) {
            fillchars[j] = ' ';
        }
        fillLine = new String(fillchars);
        for (int i = 0; i < LCD_LINES; i++) {
            StringHolder strh = new StringHolder(fillstr, false);
            final int cur = i;
            strh.whenModified(new EventConsumer() {
                @Override
                public void eventFired() {
                    updateStringOutput(cur);
                }
            });
            lines[i] = strh;
            strh.notifyChanged();
        }
        for (int i = 0; i < INPUT_COUNT; i++) {
            BooleanStatus stat = new BooleanStatus();
            inputs[i] = stat;
            inputStats[i] = stat;
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            FloatStatus stat = new FloatStatus();
            analogs[i] = stat;
            analogStats[i] = stat;
        }
        try {
            lcd = new TextLCDPhidget();
            ifa = new InterfaceKitPhidget();
            lcd.addAttachListener(this);
            lcd.addDetachListener(this);
            lcd.addErrorListener(this);
            ifa.addAttachListener(this);
            ifa.addDetachListener(this);
            ifa.addErrorListener(this);
            ifa.addInputChangeListener(this);
            ifa.addSensorChangeListener(this);
            lcd.openAny();
            ifa.openAny();
            lcd.setBacklight(true);
            lcd.setContrast(100);
        } catch (PhidgetException ex) {
            Logger.log(LogLevel.SEVERE, "Could not initialize Phidget", ex);
        }
    }

    /**
     * Share all the inputs and outputs and the current attachment state over
     * the network.
     *
     * @param node the node to share on.
     */
    public void share(CluckNode node) {
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            node.publish("phidget-bo" + i, outputs[i]);
        }
        for (int i = 0; i < LCD_LINES; i++) {
            node.publish("phidget-lcd" + i, lines[i].getOutput());
        }
        node.publish("phidget-attached", isAttached);
        for (int i = 0; i < INPUT_COUNT; i++) {
            node.publish("phidget-bi" + i, inputs[i]);
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            node.publish("phidget-ai" + i, analogs[i]);
        }
    }

    public void displayClosing() {
        try {
            lcd.setDisplayString(0, "Poultry Inspector is");
            lcd.setDisplayString(1, "     now closed.    ");
        } catch (PhidgetException ex) {
            Logger.log(LogLevel.SEVERE, "Cannot update string output to Phidget", ex);
        }
    }

    public void connectionDown() {
        lines[0].set("  Connection lost.  ");
        lines[1].set("       Sorry.       ");
    }

    public void connectionUp() {
        if ("  Connection lost.  ".equals(lines[0].get())) {
            lines[0].set("  .  .  .  .  .  .  ");
        }
        if ("       Sorry.       ".equals(lines[1].get())) {
            lines[1].set("  .  .  .  .  .  .  ");
        }
    }

    private void updateStringOutput(int line) {
        try {
            if (lcd != null) {
                lcd.setDisplayString(line, lines[line].get().replace('\r', ' ').concat(fillLine).substring(0, LCD_WIDTH));
            }
        } catch (PhidgetException ex) {
            if (ex.getErrorNumber() == PhidgetException.EPHIDGET_NOTATTACHED) {
                Logger.log(LogLevel.WARNING, "Phidget not attached!");
            } else {
                Logger.log(LogLevel.SEVERE, "Cannot update string output to Phidget", ex);
            }
        }
    }

    private void updateBooleanOutput(int cur) {
        try {
            ifa.setOutputState(cur, outvals[cur]);
        } catch (PhidgetException ex) {
            Logger.log(LogLevel.SEVERE, "Cannot update boolean output to Phidget", ex);
        }
    }

    @Override
    public void attached(AttachEvent ae) {
        Phidget p = ae.getSource();
        if (p == lcd) {
            try {
                if (lcd.getColumnCount() != LCD_WIDTH) {
                    Logger.severe("LCD column count mismatch: " + lcd.getColumnCount() + " instead of " + LCD_WIDTH);
                }
                if (lcd.getRowCount() != LCD_LINES) {
                    Logger.severe("LCD row count mismatch: " + lcd.getRowCount() + " instead of " + LCD_LINES);
                }
                lcd.setBacklight(true);
                lcd.setContrast(100);
                for (int i = 0; i < LCD_LINES; i++) {
                    updateStringOutput(i);
                }
            } catch (PhidgetException ex) {
                Logger.log(LogLevel.SEVERE, "Error on LCD attach", ex);
            }
        } else if (p == ifa) {
            try {
                if (ifa.getInputCount() != INPUT_COUNT) {
                    Logger.severe("Interface input count mismatch: " + ifa.getInputCount() + " instead of " + INPUT_COUNT);
                }
                if (ifa.getSensorCount() != ANALOG_COUNT) {
                    Logger.severe("Interface analog count mismatch: " + ifa.getSensorCount() + " instead of " + ANALOG_COUNT);
                }
                for (int i = 0; i < OUTPUT_COUNT; i++) {
                    updateBooleanOutput(i);
                }
                for (int i = 0; i < INPUT_COUNT; i++) {
                    inputStats[i].writeValue(ifa.getInputState(i));
                }
                for (int i = 0; i < ANALOG_COUNT; i++) {
                    float moved = (ifa.getSensorValue(i) - 500) / 500.0f;
                    if (moved < -1 || moved > 1) {
                        Logger.warning("Sensor out of range: " + moved);
                    }
                    analogStats[i].writeValue(moved);
                }
            } catch (PhidgetException ex) {
                Logger.log(LogLevel.SEVERE, "Error on Interface attach", ex);
            }
        } else {
            Logger.log(LogLevel.WARNING, "Attach of unknown phidget!");
        }
        recalculateAttached();
    }

    @Override
    public void detached(DetachEvent ae) {
        recalculateAttached();
    }

    private void recalculateAttached() {
        try {
            attachStat.writeValue(lcd.isAttached() && ifa.isAttached());
        } catch (PhidgetException ex) {
            Logger.log(LogLevel.WARNING, "Could not recalculate attachment status!", ex);
        }
    }

    @Override
    public void inputChanged(InputChangeEvent ae) {
        inputStats[ae.getIndex()].writeValue(ae.getState());
    }

    @Override
    public void sensorChanged(SensorChangeEvent ae) {
        int val = ae.getValue();
        // val: 0 to 1000, val-500: -500 to 500, (val-500)/500.0f: -1f to 1f
        float moved = (val - 500) / 500.0f;
        if (moved < -1 || moved > 1) {
            Logger.warning("Sensor out of range: " + moved);
        }
        analogStats[ae.getIndex()].writeValue(moved);
    }

    @Override
    public void error(ErrorEvent ae) {
        Logger.log(LogLevel.SEVERE, "Phidget Reported Error: " + ae, ae.getException());
    }
}
