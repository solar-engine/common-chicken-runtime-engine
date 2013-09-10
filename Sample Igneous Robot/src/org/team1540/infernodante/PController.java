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
    public static final TuningContext context = new TuningContext(CluckGlobals.encoder, "armTuning");
    static {
        context.publishSavingEvent("armTuning");
    }
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
