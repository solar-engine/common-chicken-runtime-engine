/*
 * Copyright 2013 Colby Skeggs
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
package ccre.device;

import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanOutput;
import ccre.chan.BooleanStatus;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.chan.FloatStatus;
import ccre.ctrl.DriverImpls;
import ccre.ctrl.Mixing;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.util.CHashMap;
import ccre.util.Utils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * The main Cel interpreter. (Chicken Express Language).
 * This is a tiny domain-specific-language for quickly writing Software.
 *
 * @author skeggsc
 */
public class CelInterpreter {
    
    protected CHashMap<String, Object> vars = new CHashMap<String, Object>();
    private String[] line;
    private DeviceRegistry tree;
    protected CHashMap<String, Object> opened = new CHashMap<String, Object>();
    protected boolean running = false;
    protected EventSource defaultPeriodic = null, alwaysPeriodic = null;
    
    protected Object getTree(String name) throws InterpreterException, DeviceException {
        DeviceHandle<? extends Object> h = tree.getHandle(name);
        Object o = h.open();
        opened.put(name, o);
        return o;
    }
    
    protected void putVar(String name, Object value) throws InterpreterException {
        if (name.equals("_")) { // Null variable
            return;
        }
        if (vars.containsKey(name)) {
            throw new InterpreterException("Variable already exists: " + name);
        }
        vars.put(name, value);
    }
    
    protected Object getVar(String name) throws InterpreterException {
        if (name.equals("_")) {
            throw new InterpreterException("Cannot read from the null variable.");
        }
        Object out = vars.get(name);
        if (out == null) {
            throw new InterpreterException("Variable does not exist: " + name);
        }
        return out;
    }
    
    protected String gStr(int i) throws InterpreterException {
        if (i >= line.length) {
            if (i == 0) {
                return "";
            } else {
                throw new InterpreterException("Incomplete command: " + line[0]);
            }
        }
        return line[i];
    }
    
    protected int gInt(int i) throws InterpreterException {
        if (i >= line.length) {
            throw new InterpreterException("Incomplete command: " + line[0]);
        }
        try {
            return Integer.parseInt(line[i]);
        } catch (NumberFormatException ex) {
            throw new InterpreterException(ex.getMessage());
        }
    }
    
    protected float gFloat(int i) throws InterpreterException {
        if (i >= line.length) {
            throw new InterpreterException("Incomplete command: " + line[0]);
        }
        try {
            return Float.parseFloat(line[i]);
        } catch (NumberFormatException ex) {
            throw new InterpreterException(ex.getMessage());
        }
    }
    
    protected void execute() throws InterpreterException, DeviceException {
        String cmd = gStr(0);
        if (cmd.length() == 0) {
            return; // Null command. Do nothing.
        }
        switch (cmd.charAt(0)) {
            case 'A':
                if ("Axes".equals(cmd)) {
                    int j = gInt(1);
                    for (int i = 2; i < line.length; i++) {
                        putVar(line[i], getTree("joysticks/" + j + "/axis" + (i - 1)));
                    }
                    return;
                }
                break;
            case 'B':
                if ("Buttons".equals(cmd)) {
                    int j = gInt(1);
                    for (int i = 2; i < line.length; i++) {
                        if (line[i].charAt(0) == '+') {
                            BooleanInputPoll btn = (BooleanInputPoll) getTree("joysticks/" + j + "/button" + (i - 1));
                            putVar(line[i].substring(1), Mixing.whenBooleanBecomes(btn, true, getDefaultPeriodic()));
                        } else {
                            putVar(line[i], getTree("joysticks/" + j + "/button" + (i - 1)));
                        }
                    }
                    return;
                } else if ("BoolVar".equals(cmd)) {
                    BooleanStatus stat = new BooleanStatus();
                    putVar(gStr(1), stat);
                    for (int i = 2; i <= line.length; i++) {
                        stat.addTarget((BooleanOutput) getVar(line[i]));
                    }
                    return;
                }
                break;
            case 'E':
                if ("ExtTankDrive".equals(cmd)) {
                    DriverImpls.createExtendedSynchTankDriver(getDefaultPeriodic(), (FloatInputPoll) getVar(gStr(1)), (FloatInputPoll) getVar(gStr(2)), (FloatInputPoll) getVar(gStr(3)), (FloatOutput) getVar(gStr(4)), (FloatOutput) getVar(gStr(5)));
                    return;
                }
                break;
            case 'F':
                if ("FloatVar".equals(cmd)) {
                    FloatStatus stat = new FloatStatus();
                    putVar(gStr(1), stat);
                    for (int i = 2; i <= line.length; i++) {
                        stat.addTarget((FloatOutput) getVar(line[i]));
                    }
                    return;
                }
                break;
            case 'G':
                if ("GetTree".equals(cmd)) {
                    putVar(gStr(1), getTree(gStr(2)));
                    return;
                }
                break;
            case 'L':
                if ("LCDFloat".equals(cmd)) {
                    final PrintStream lline = new PrintStream((OutputStream) getTree("dslcd/line" + gInt(1)));
                    final FloatInputPoll inp = (FloatInputPoll) getVar(gStr(2));
                    final String prefix = gStr(3) + ": ";
                    getAlwaysPeriodic().addListener(new EventConsumer() {
                        public void eventFired() {
                            lline.println(prefix + inp.readValue());
                        }
                    });
                    return;
                } else if ("LCDBoolean".equals(cmd)) {
                    final PrintStream lline = new PrintStream((OutputStream) getTree("dslcd/line" + gInt(1)));
                    final BooleanInputPoll inp = (BooleanInputPoll) getVar(gStr(2));
                    final String prefix = gStr(3) + ": ";
                    getAlwaysPeriodic().addListener(new EventConsumer() {
                        public void eventFired() {
                            lline.println(prefix + inp.readValue());
                        }
                    });
                    return;
                }
                break;
            case 'S':
                if ("SetFalse".equals(cmd)) {
                    Mixing.setWhen((EventSource) getVar(gStr(2)), (BooleanOutput) getVar(gStr(1)), false);
                    return;
                } else if ("SetTrue".equals(cmd)) {
                    Mixing.setWhen((EventSource) getVar(gStr(2)), (BooleanOutput) getVar(gStr(1)), true);
                    return;
                } else if ("Set".equals(cmd)) {
                    Mixing.setWhen((EventSource) getVar(gStr(2)), (FloatOutput) getVar(gStr(1)), gFloat(3));
                    return;
                }
                break;
            case 'T':
                if ("TankDrive".equals(cmd)) {
                    DriverImpls.createSynchTankDriver(getDefaultPeriodic(), (FloatInputPoll) getVar(gStr(1)), (FloatInputPoll) getVar(gStr(2)), (FloatOutput) getVar(gStr(3)), (FloatOutput) getVar(gStr(4)));
                    return;
                }
                break;
            case 'C':
                try { // Relay, Switch
                    ((OutputStream) getTree("pneumatics/compressorConf")).write((gInt(1) + " " + gInt(2) + "\n").getBytes());
                } catch (IOException ex) {
                    Logger.log(LogLevel.WARNING, "Cannot start compressor", ex);
                    throw new InterpreterException("Cannot start compressor!");
                }
                break;
        }
        throw new InterpreterException("No such command: " + cmd);
    }
    
    public synchronized void execute(DeviceRegistry tree, String program) throws InterpreterException, DeviceException {
        if (running) {
            throw new IllegalStateException("Already running!");
        }
        running = true;
        defaultPeriodic = null;
        alwaysPeriodic = null;
        boolean exitNormally = false;
        try {
            String[] parts = Utils.split(program, '\n');
            vars.clear();
            this.tree = tree;
            for (String sline : parts) {
                if (sline.length() == 0) {
                    continue;
                }
                String[] cpts = Utils.split(sline, ' ');
                this.line = cpts;
                execute();
            }
            exitNormally = true;
        } finally {
            if (!exitNormally) {
                try {
                    cancel();
                } catch (DeviceException ex) { // Already an exception! Don't complicate things.
                }
            }
        }
    }
    
    public synchronized void cancel() throws DeviceException {
        if (!running) {
            return;
        }
        running = false;
        DeviceException first = null;
        for (String key : opened) {
            Object val = opened.get(key);
            try {
                tree.getHandle(key).close(val);
            } catch (DeviceException ex) {
                if (first == null) {
                    first = ex;
                }
                Logger.log(LogLevel.WARNING, "Could not shut down CelInterpreter.", ex);
            }
        }
        opened.clear();
        if (first == null) {
            throw first;
        }
    }
    
    private EventSource getDefaultPeriodic() throws InterpreterException, DeviceException {
        if (defaultPeriodic == null) {
            defaultPeriodic = (EventSource) getTree("modes/teleop/during");
        }
        return defaultPeriodic;
    }
    
    private EventSource getAlwaysPeriodic() throws InterpreterException, DeviceException {
        if (alwaysPeriodic == null) {
            alwaysPeriodic = (EventSource) getTree("modes/always");
        }
        return alwaysPeriodic;
    }
}
