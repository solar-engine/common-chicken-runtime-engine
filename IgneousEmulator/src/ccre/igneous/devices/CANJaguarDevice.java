/*
 * Copyright 2014 Colby Skeggs
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
package ccre.igneous.devices;

import java.util.HashMap;

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
import ccre.ctrl.ExtendedMotor;
import ccre.ctrl.ExtendedMotorFailureException;
import ccre.igneous.DeviceGroup;
import ccre.igneous.DeviceListPanel;

public class CANJaguarDevice extends DeviceGroup {

    private BooleanViewDevice enabled;
    private final HashMap<ExtendedMotor.StatusType, FloatControlDevice> statusDevices = new HashMap<ExtendedMotor.StatusType, FloatControlDevice>();
    private final HashMap<ExtendedMotor.DiagnosticType, BooleanControlDevice> diagnosticDevices = new HashMap<ExtendedMotor.DiagnosticType, BooleanControlDevice>();
    private final HashMap<ExtendedMotor.OutputControlMode, FloatViewDevice> outputDevices = new HashMap<ExtendedMotor.OutputControlMode, FloatViewDevice>();
    private DeviceListPanel master;
    private TextualDisplayDevice pidStatus;
    private boolean wasAddedToMaster = false;

    public CANJaguarDevice(String name, DeviceListPanel master) {
        add(new HeadingDevice(name));
        this.master = master;
    }

    public CANJaguarDevice(int id, DeviceListPanel master) {
        this("CAN Jaguar " + id, master);
    }

    public synchronized CANJaguarDevice addToMaster() {
        if (!wasAddedToMaster) {
            wasAddedToMaster = true;
            master.add(this);
        }
        return this;
    }

    public ExtendedMotor getMotor() {
        return value;
    }

    private final ExtendedMotor value = new ExtendedMotor() {

        public void enable() throws ExtendedMotorFailureException {
            if (enabled == null) {
                enabled = add(new BooleanViewDevice("ENABLED"));
            }
            enabled.set(true);
        }

        public void disable() throws ExtendedMotorFailureException {
            if (enabled == null) {
                enabled = add(new BooleanViewDevice("ENABLED"));
            }
            enabled.set(false);
        }

        public BooleanOutput asEnable() throws ExtendedMotorFailureException {
            if (enabled == null) {
                enabled = add(new BooleanViewDevice("ENABLED"));
            }
            return enabled;
        }

        public FloatOutput asMode(OutputControlMode mode) throws ExtendedMotorFailureException {
            if (!outputDevices.containsKey(mode)) {
                outputDevices.put(mode, add(new FloatViewDevice(mode.toString())));
            }
            return outputDevices.get(mode);
        }

        public FloatInputPoll asStatus(StatusType type) throws ExtendedMotorFailureException {
            if (!statusDevices.containsKey(type)) {
                statusDevices.put(type, add(new FloatControlDevice(type.toString())));
            }
            return statusDevices.get(type);
        }

        public Object getDiagnostics(DiagnosticType type) {
            switch (type) {
            case BUS_VOLTAGE_FAULT:
            case CURRENT_FAULT:
            case GATE_DRIVER_FAULT:
            case TEMPERATURE_FAULT:
                return getDiagnosticChannel(type).get();
            case GENERIC_FAULT_MASK:
            case CAN_JAGUAR_FAULTS:
                return (getDiagnosticChannel(DiagnosticType.CURRENT_FAULT).get() ? 1 : 0) | (getDiagnosticChannel(DiagnosticType.TEMPERATURE_FAULT).get() ? 2 : 0) |
                        (getDiagnosticChannel(DiagnosticType.BUS_VOLTAGE_FAULT).get() ? 4 : 0) | (getDiagnosticChannel(DiagnosticType.GATE_DRIVER_FAULT).get() ? 8 : 0);
            default:
                return null;
            }
        }

        public BooleanInputPoll getDiagnosticChannel(DiagnosticType type) {
            if (type.isBooleanDiagnostic) {
                if (!diagnosticDevices.containsKey(type)) {
                    diagnosticDevices.put(type, add(new BooleanControlDevice(type.toString())));
                }
                return diagnosticDevices.get(type);
            } else {
                return null;
            }
        }

        public boolean hasInternalPID() {
            return true;
        }

        public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
            if (pidStatus == null) {
                pidStatus = add(new TextualDisplayDevice("...", 50));
            }
            pidStatus.set("PID P=" + P + " I=" + I + " D=" + D);
        }
    };
}
