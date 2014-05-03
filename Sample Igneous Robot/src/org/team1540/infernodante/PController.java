/*
 * Copyright 2013-2014 Colby Skeggs, Casey Currey-Wilson
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

import ccre.channel.*;
import ccre.cluck.CluckGlobals;
import ccre.ctrl.Mixing;
import ccre.holders.TuningContext;

public class PController implements EventOutput {

    private static final float HIGH_SPEED = 1.0f;
    private static final float MEDIUM_SPEED = 0.35f;
    private static final float LOW_SPEED = 0.08f;
    public static final TuningContext context = new TuningContext(CluckGlobals.getNode(), "armTuning").publishSavingEvent("armTuning");
    public static final FloatStatus STABLE_RANGE = context.getFloat("arm-stable", 0.17f);
    public static final FloatStatus DEADZONE_RANGE = context.getFloat("arm-deadzone", 0.1f);
    private static final float DEF_ARM_PICKUP_PRESET = Inferno.IS_COMPETITION_ROBOT ? 4.22f : 5.111649321f;
    private static final float DEF_ARM_DROP_PRESET = Inferno.IS_COMPETITION_ROBOT ? 2.38f : 3.107605071f;
    private static final float DEF_ARM_DRIVE_PRESET = Inferno.IS_COMPETITION_ROBOT ? 0.571f : 4.777641946f;
    private static final float DEF_ARM_LOAD_PRESET = Inferno.IS_COMPETITION_ROBOT ? 0.571f : 3.693807046f;
    // These are not used in this file, but are in Inferno.java
    public static final FloatStatus ARM_PICKUP_PRESET = context.getFloat("arm-pickup", DEF_ARM_PICKUP_PRESET);
    public static final FloatStatus ARM_DROP_PRESET = context.getFloat("arm-drop", DEF_ARM_DROP_PRESET);
    public static final FloatStatus ARM_DRIVE_PRESET = context.getFloat("arm-drive", DEF_ARM_DRIVE_PRESET);
    public static final FloatStatus ARM_LOAD_PRESET = context.getFloat("arm-load", DEF_ARM_LOAD_PRESET);
    public BooleanStatus enabled = new BooleanStatus();
    public BooleanInputPoll suspendOnceStable = Mixing.alwaysFalse;
    public BooleanInputPoll isBrakeDeactivated = Mixing.alwaysFalse;
    public FloatStatus setpoint = new FloatStatus();
    private final FloatInputPoll source;
    private final FloatOutput output;
    private final FloatInputPoll disabledSource;

    public PController(FloatInputPoll source, FloatOutput output, FloatInputPoll disabledSource) {
        this.source = source;
        this.disabledSource = disabledSource;
        this.output = output;
    }

    public void setSetpointWhen(FloatInputPoll fin, EventInput source) {
        Mixing.pumpWhen(source, fin, setpoint);
    }

    public boolean isStable() {
        return enabled.get() && Math.abs(source.get() - setpoint.get()) < STABLE_RANGE.get();
    }

    protected boolean isBrakeRange() {
        return enabled.get() && Math.abs(source.get() - setpoint.get()) < 2 * STABLE_RANGE.get();
    }

    public void event() {
        if (!enabled.get()) {
            output.set(disabledSource.get());
        } else {
            if (!isBrakeDeactivated.get() || (isStable() && suspendOnceStable.get())) {
                output.set(0);
                return;
            }
            // postive UP negative DOWN
            float v = setpoint.get() - source.get(), absV = Math.abs(v);
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
            if (!(isStable() && suspendOnceStable.get()) && absV > DEADZONE_RANGE.get()) {
                output.set(goalSpeed);
            } else {
                output.set(0);
            }
        }
    }

    public void updateWhen(EventInput evt) {
        evt.send(this);
    }

    public void disableWhen(EventInput when) {
        enabled.setFalseWhen(when);
    }

    public void enableWhen(EventInput when) {
        enabled.setTrueWhen(when);
    }
}
