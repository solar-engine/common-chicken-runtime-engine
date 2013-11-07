/*
 * Copyright 2013 Colby Skeggs, Casey Currey-Wilson
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
package org.team1540.infernodante;

import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanStatus;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.chan.FloatStatus;
import ccre.cluck.CluckGlobals;
import ccre.ctrl.Mixing;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.holders.TuningContext;

public class PController implements EventConsumer {

    private static final float HIGH_SPEED = 1.0f;
    private static final float MEDIUM_SPEED = 0.35f;
    private static final float LOW_SPEED = 0.08f;
    public static final TuningContext context = new TuningContext(CluckGlobals.node, "armTuning").publishSavingEvent("armTuning");
    public static final FloatStatus STABLE_RANGE = context.getFloat("arm-stable", 0.17f);
    public static final FloatStatus DEADZONE_RANGE = context.getFloat("arm-deadzone", 0.1f);
    private static final float DEF_ARM_PICKUP_PRESET = Inferno.IS_COMPETITION_ROBOT ? 4.22f : 5.111649321f;
    private static final float DEF_ARM_DROP_PRESET = Inferno.IS_COMPETITION_ROBOT ? 2.38f : 3.107605071f;
    private static final float DEF_ARM_DRIVE_PRESET = Inferno.IS_COMPETITION_ROBOT ? 0.571f : 4.777641946f;
    private static final float DEF_ARM_LOAD_PRESET = Inferno.IS_COMPETITION_ROBOT ? 0.571f : 3.693807046f;
    // These are not used in this file, but are in Inferno.java
    public static final FloatStatus ARM_PICKUP_PRESET = context.getFloat("arm-pickup", DEF_ARM_PICKUP_PRESET, "arm-potentiometer");
    public static final FloatStatus ARM_DROP_PRESET = context.getFloat("arm-drop", DEF_ARM_DROP_PRESET, "arm-potentiometer");
    public static final FloatStatus ARM_DRIVE_PRESET = context.getFloat("arm-drive", DEF_ARM_DRIVE_PRESET, "arm-potentiometer");
    public static final FloatStatus ARM_LOAD_PRESET = context.getFloat("arm-load", DEF_ARM_LOAD_PRESET, "arm-potentiometer");
    public BooleanStatus enabled = new BooleanStatus();
    public BooleanInputPoll suspendOnceStable = Mixing.alwaysFalse;
    public BooleanInputPoll isBrakeDeactivated = Mixing.alwaysFalse;
    public FloatStatus setpoint = new FloatStatus();
    private FloatInputPoll source;
    private FloatOutput output;
    private FloatInputPoll disabledSource;

    public PController(FloatInputPoll source, FloatOutput output, FloatInputPoll disabledSource) {
        this.source = source;
        this.disabledSource = disabledSource;
        this.output = output;
    }

    public void setSetpointWhen(FloatInputPoll fin, EventSource source) {
        Mixing.pumpWhen(source, fin, setpoint);
    }

    public boolean isStable() {
        return enabled.readValue() && Math.abs(source.readValue() - setpoint.readValue()) < STABLE_RANGE.readValue();
    }

    protected boolean isBrakeRange() {
        return enabled.readValue() && Math.abs(source.readValue() - setpoint.readValue()) < 2 * STABLE_RANGE.readValue();
    }

    public void eventFired() {
        if (!enabled.readValue()) {
            output.writeValue(disabledSource.readValue());
        } else {
            if (!isBrakeDeactivated.readValue() || (isStable() && suspendOnceStable.readValue())) {
                output.writeValue(0);
                return;
            }
            // postive UP negative DOWN
            float v = setpoint.readValue() - source.readValue(), absV = Math.abs(v);
            if (absV <= 0.01) {
                return;
            }
            float goalSpeed;
            if (absV < 0.16) {
                goalSpeed = LOW_SPEED;
            } else if (absV < 0.5) {
                goalSpeed = MEDIUM_SPEED;
            } else {
                goalSpeed = HIGH_SPEED;
            }
            if (v > 0) {
                goalSpeed = -goalSpeed;
            }
            if (!Inferno.IS_COMPETITION_ROBOT) {
                goalSpeed = -goalSpeed;
            }
            if (!(isStable() && suspendOnceStable.readValue()) && absV > DEADZONE_RANGE.readValue()) {
                output.writeValue(goalSpeed);
            } else {
                output.writeValue(0);
            }
        }
    }

    public void updateWhen(EventSource evt) {
        evt.addListener(this);
    }

    public void disableWhen(EventSource when) {
        when.addListener(enabled.getSetFalseEvent());
    }

    public void enableWhen(EventSource when) {
        when.addListener(enabled.getSetTrueEvent());
    }
}
