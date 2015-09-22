/*
 * Copyright 2013-2015 Colby Skeggs
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
package ccre.supercanvas.phidget;

import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.util.LineCollectorOutputStream;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.TextLCDPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

/**
 * The interface to the Phidget system. Currently, this has hardcoded constants
 * for the number of inputs and outputs. If you need different amounts, it would
 * be a good time to extend this to support arbitrary amounts.
 *
 * @author skeggsc
 */
public class PhidgetMonitor implements Serializable, AttachListener, DetachListener, ErrorListener, InputChangeListener, SensorChangeListener {
    private static Logger logger = LoggerFactory.getLogger(PhidgetMonitor.class);

    private static final long serialVersionUID = -8665410515221749926L;
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
    private TextLCDPhidget lcd;
    /**
     * The InterfaceKit handle for the phidget.
     */
    private InterfaceKitPhidget ifa;
    /**
     * The boolean outputs to the phidget.
     */
    private final BooleanOutput[] outputs = new BooleanOutput[OUTPUT_COUNT];
    /**
     * The current values for the boolean outputs.
     */
    private final boolean[] outvals = new boolean[OUTPUT_COUNT];
    /**
     * The generated string to fill each row with.
     */
    private final String fillLine;
    /**
     * The lines of the LCD.
     */
    private final String[] lines = new String[LCD_LINES];
    /**
     * The internal status of whether or not the phidget is attached.
     */
    private final BooleanStatus attachStat = new BooleanStatus();
    /**
     * The binary inputs that the Phidget provides.
     */
    private final BooleanStatus[] inputs = new BooleanStatus[INPUT_COUNT];
    /**
     * The analog inputs that the Phidget provides.
     */
    private final FloatStatus[] analogs = new FloatStatus[ANALOG_COUNT];
    /**
     * Are the channels from this currently shared over Cluck?
     */
    private boolean isShared = false;

    /**
     * Create a new PhidgetMonitor.
     */
    public PhidgetMonitor() {
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            final int cur = i;
            outputs[i] = new BooleanOutput() {
                @Override
                public void set(boolean bln) {
                    outvals[cur] = bln;
                    updateBooleanOutput(cur);
                }
            };
        }
        char[] fillchars = new char[LCD_WIDTH];
        Arrays.fill(fillchars, '.');
        String fillstr = new String(fillchars);
        Arrays.fill(fillchars, ' ');
        fillLine = new String(fillchars);
        for (int i = 0; i < LCD_LINES; i++) {
            lines[i] = fillstr;
            updateStringOutput(i);
        }
        for (int i = 0; i < INPUT_COUNT; i++) {
            inputs[i] = new BooleanStatus();
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            analogs[i] = new FloatStatus();
        }
        try {
            lcd = new TextLCDPhidget();
            ifa = new InterfaceKitPhidget();
        } catch (PhidgetException ex) {
            logger.error("Could not initialize Phidget!", ex);
        }
    }

    /**
     * Share the contents of the phidget. Be careful - no error checking if you
     * use this wrong.
     */
    public void share() {
        isShared = true;
        lcd.addAttachListener(this);
        lcd.addDetachListener(this);
        lcd.addErrorListener(this);
        ifa.addAttachListener(this);
        ifa.addDetachListener(this);
        ifa.addErrorListener(this);
        ifa.addInputChangeListener(this);
        ifa.addSensorChangeListener(this);
        try {
            lcd.openAny();
            ifa.openAny();
            if (lcd.isAttached()) {
                lcd.setBacklight(true);
                lcd.setContrast(100);
            }
        } catch (PhidgetException ex) {
            logger.error("Could not initialize Phidget!", ex);
        }
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            Cluck.publish("phidget-bo" + i, outputs[i]);
        }
        for (int i = 0; i < LCD_LINES; i++) {
            final int cur = i;
            Cluck.publish("phidget-lcd" + i, new LineCollectorOutputStream() {
                @Override
                protected void collect(String param) {
                    lines[cur] = param;
                }
            });
        }
        Cluck.publish("phidget-attached", (BooleanInput) attachStat);
        for (int i = 0; i < INPUT_COUNT; i++) {
            Cluck.publish("phidget-bi" + i, (BooleanInput) inputs[i]);
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            Cluck.publish("phidget-ai" + i, (FloatInput) analogs[i]);
        }
        Cluck.getNode().notifyNetworkModified();
    }

    /**
     * Unshare the contents of the phidget. Be careful - no error checking if
     * you use this wrong.
     */
    public void unshare() {
        isShared = false;
        lcd.removeAttachListener(this);
        lcd.removeDetachListener(this);
        lcd.removeErrorListener(this);
        ifa.removeAttachListener(this);
        ifa.removeDetachListener(this);
        ifa.removeErrorListener(this);
        ifa.removeInputChangeListener(this);
        ifa.removeSensorChangeListener(this);
        attachStat.set(false);
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            Cluck.getNode().removeLink("phidget-bo" + i);
        }
        for (int i = 0; i < LCD_LINES; i++) {
            Cluck.getNode().removeLink("phidget-lcd" + i);
        }
        Cluck.getNode().removeLink("phidget-attached");
        for (int i = 0; i < INPUT_COUNT; i++) {
            Cluck.getNode().removeLink("phidget-bi" + i);
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            Cluck.getNode().removeLink("phidget-ai" + i);
        }
        Cluck.getNode().notifyNetworkModified();
    }

    private void updateStringOutput(int line) {
        try {
            if (lcd != null) {
                lcd.setDisplayString(line, lines[line].replace('\r', ' ').concat(fillLine).substring(0, LCD_WIDTH));
            }
        } catch (PhidgetException ex) {
            if (ex.getErrorNumber() == PhidgetException.EPHIDGET_NOTATTACHED) {
                logger.warn("Phidget not attached!");
            } else {
                logger.error("Cannot update string output to Phidget: " + ex);
            }
        }
    }

    private void updateBooleanOutput(int cur) {
        try {
            ifa.setOutputState(cur, outvals[cur]);
        } catch (PhidgetException ex) {
            logger.error("Cannot update boolean output to Phidget: " + ex);
        }
    }

    /**
     * Called when any relevant Phidget device is attached.
     *
     * @param ae The attachment event.
     */
    @Override
    public void attached(AttachEvent ae) {
        Phidget p = ae.getSource();
        if (p == lcd) {
            try {
                onAttachLCD();
            } catch (PhidgetException ex) {
                logger.error("Error on LCD attach", ex);
            }
        } else if (p == ifa) {
            try {
                onAttachInterface();
            } catch (PhidgetException ex) {
                logger.error("Error on Interface attach: " + ex);
            }
        } else {
            logger.warn("Attach of unknown phidget!");
        }
        recalculateAttached();
    }

    private void onAttachInterface() throws PhidgetException {
        if (ifa.getInputCount() != INPUT_COUNT) {
            logger.error("Interface input count mismatch: " + ifa.getInputCount() + " instead of " + INPUT_COUNT);
        }
        if (ifa.getSensorCount() != ANALOG_COUNT) {
            logger.error("Interface analog count mismatch: " + ifa.getSensorCount() + " instead of " + ANALOG_COUNT);
        }
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            updateBooleanOutput(i);
        }
        for (int i = 0; i < INPUT_COUNT; i++) {
            inputs[i].set(ifa.getInputState(i));
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            float moved = (ifa.getSensorValue(i) - 500) / 500.0f;
            if (moved < -1 || moved > 1) {
                logger.warn("Sensor out of range: " + moved);
            }
            analogs[i].set(moved);
        }
    }

    private void onAttachLCD() throws PhidgetException {
        if (lcd.getColumnCount() != LCD_WIDTH) {
            logger.error("LCD column count mismatch: " + lcd.getColumnCount() + " instead of " + LCD_WIDTH);
        }
        if (lcd.getRowCount() != LCD_LINES) {
            logger.error("LCD row count mismatch: " + lcd.getRowCount() + " instead of " + LCD_LINES);
        }
        lcd.setBacklight(true);
        lcd.setContrast(100);
        for (int i = 0; i < LCD_LINES; i++) {
            updateStringOutput(i);
        }
    }

    /**
     * Called when any relevant Phidget device is detached.
     *
     * @param ae The detachment event.
     */
    @Override
    public void detached(DetachEvent ae) {
        recalculateAttached();
    }

    private void recalculateAttached() {
        try {
            attachStat.set(lcd.isAttached() && ifa.isAttached());
        } catch (PhidgetException ex) {
            logger.warn("Could not recalculate attachment status: " + ex);
        }
    }

    /**
     * Called when any relevant Phidget boolean input changes.
     *
     * @param ae The input change event.
     */
    @Override
    public void inputChanged(InputChangeEvent ae) {
        inputs[ae.getIndex()].set(ae.getState());
    }

    /**
     * Called when any relevant Phidget float sensor changes.
     *
     * @param ae The sensor change event.
     */
    @Override
    public void sensorChanged(SensorChangeEvent ae) {
        int val = ae.getValue();
        // val: 0 to 1000, val-500: -500 to 500, (val-500)/500.0f: -1f to 1f
        float moved = (val - 500) / 500.0f;
        if (moved < -1 || moved > 1) {
            logger.warn("Sensor out of range: " + moved);
        }
        analogs[ae.getIndex()].set(moved);
    }

    /**
     * Called when any relevant Phidget error occurs.
     *
     * @param ae The error event.
     */
    @Override
    public void error(ErrorEvent ae) {
        logger.error("Phidget Reported Error: " + ae);
    }

    private Object writeReplace() {
        return new SerializedMonitor(isShared);
    }

    private static class SerializedMonitor implements Serializable {

        private static final long serialVersionUID = -6097101016789921164L;
        private boolean isShared;

        private SerializedMonitor(boolean isShared) {
            this.isShared = isShared;
        }

        private Object readResolve() {
            PhidgetMonitor out = new PhidgetMonitor();
            if (isShared) {
                out.share();
            }
            return out;
        }
    }
}
