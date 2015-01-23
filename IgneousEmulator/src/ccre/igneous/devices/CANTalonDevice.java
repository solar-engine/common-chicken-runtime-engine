/*
 * Copyright 2014-2015 Colby Skeggs
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

/**
 * A virtual CANTalon, which will contain any statuses, diagnostics, or outputs
 * requested by the application.
 * 
 * @author skeggsc
 */
public class CANTalonDevice extends DeviceGroup {

    private BooleanViewDevice enabled;
    private final HashMap<ExtendedMotor.StatusType, FloatControlDevice> statusDevices = new HashMap<ExtendedMotor.StatusType, FloatControlDevice>();
    private final HashMap<ExtendedMotor.DiagnosticType, BooleanControlDevice> diagnosticDevices = new HashMap<ExtendedMotor.DiagnosticType, BooleanControlDevice>();
    private final HashMap<ExtendedMotor.OutputControlMode, FloatViewDevice> outputDevices = new HashMap<ExtendedMotor.OutputControlMode, FloatViewDevice>();
    private DeviceListPanel master;
    private TextualDisplayDevice pidStatus;
    private boolean wasAddedToMaster = false;

    /**
     * Creates a new CANTalonDevice described as name with a specified
     * DeviceListPanel to contain this device.
     * 
     * Make sure to call addToMaster - don't add this directly.
     * 
     * @param name how to describe the CANTalonDevice.
     * @param master the panel that contains this.
     * @see #addToMaster()
     */
    public CANTalonDevice(String name, DeviceListPanel master) {
        add(new HeadingDevice(name));
        this.master = master;
    }

    /**
     * Creates a new CANTalonDevice described as a CAN Talon of the given ID
     * with a specified DeviceListPanel to contain this device.
     * 
     * Make sure to call addToMaster - don't add this directly.
     * 
     * @param id the CAN bus ID of the device.
     * @param master the panel that contains this.
     * @see #addToMaster()
     */
    public CANTalonDevice(int id, DeviceListPanel master) {
        this("CAN Talon " + id, master);
    }

    /**
     * Add this device to its master panel, or do nothing if this has already
     * been done.
     * 
     * @return this CANTalonDevice, for method chaining purposes.
     */
    public synchronized CANTalonDevice addToMaster() {
        if (!wasAddedToMaster) {
            wasAddedToMaster = true;
            master.add(this);
        }
        return this;
    }

    /**
     * Gets the ExtendedMotor interface to pass to the emulated program.
     * 
     * @return the ExtendedMotor of this CANTalon.
     */
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
            case ANY_FAULT:
            case TEMPERATURE_FAULT:
                return getDiagnosticChannel(type).get();
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
