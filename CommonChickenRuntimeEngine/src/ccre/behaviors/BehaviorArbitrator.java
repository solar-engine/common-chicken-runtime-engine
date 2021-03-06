/*
 * Copyright 2016 Cel Skeggs
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
package ccre.behaviors;

import java.util.ArrayList;
import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.EventCell;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.cluck.Cluck;
import ccre.discrete.DerivedDiscreteInput;
import ccre.discrete.DiscreteInput;
import ccre.discrete.DiscreteType;
import ccre.rconf.RConf;
import ccre.rconf.RConfable;
import ccre.verifier.FlowPhase;
import ccre.verifier.SetupPhase;

/**
 * A Behavior Arbitrator has a prioritized list of behavior states, which it
 * arbitrates between. Each behavior can be independently attempting to be
 * active, and the highest-priority active behavior gets to run at any moment.
 *
 * Channels can be attached to the arbitrator such that they are controlled with
 * a specific input channel in each behavior state. This allows easy control of
 * peripherals or other subsystems based on dataflow chosen by the current
 * behavior.
 *
 * Behavior states are added via {@link #addBehavior(String, BooleanInput)}.
 * Later calls to that method create behaviors with a higher priority than
 * previous calls.
 *
 * @see Behavior
 * @see ArbitratedBoolean
 * @see ArbitratedEvent
 * @see ArbitratedFloat
 * @author skeggsc
 */
public class BehaviorArbitrator implements RConfable {
    private final ArrayList<Behavior> behaviors = new ArrayList<>();
    private final EventCell onActiveUpdateCell = new EventCell();
    final EventInput onActiveUpdate = onActiveUpdateCell;
    Behavior active;
    private final String name;

    /**
     * Creates a BehaviorArbitrator with a given name, such as the name of the
     * subsystem that it's in.
     *
     * @param name the name of the arbitrator, used
     */
    public BehaviorArbitrator(String name) {
        this.name = name;
    }

    /**
     * Publishes this BehaviorArbitrator over RConf to the PoultryInspector.
     * This allows for easy inspection of the current state of the
     * BehaviorArbitrator.
     *
     * The publishing name will be based on the subsystem name given when this
     * BehaviorArbitrator was created.
     *
     * @return this instance, for method chaining.
     */
    @SetupPhase
    public BehaviorArbitrator publish() {
        Cluck.publishRConf(this.name + " Behavior", this);
        return this;
    }

    @FlowPhase
    private synchronized void checkUpdate() {
        Behavior target = null;
        for (Behavior b : behaviors) {
            if (b.request.get()) {
                target = b;
            }
        }
        setActiveBehavior(target);
    }

    @FlowPhase
    private synchronized void setActiveBehavior(Behavior behavior) {
        if (active == behavior) {
            return;
        }
        active = behavior;
        onActiveUpdateCell.event();
    }

    /**
     * Provides an arbitrated Float channel controlled by this behavior.
     *
     * @param base the input to use when no behavior is active, or if the
     * currently active behavior hasn't provided an input for this channel.
     * @return the arbitrated Float channel
     * @see #addFloat()
     */
    @SetupPhase
    public ArbitratedFloat addFloat(FloatInput base) {
        if (base == null) {
            throw new NullPointerException();
        }
        return new ArbitratedFloat(this, base);
    }

    /**
     * Provides an arbitrated Float channel controlled by this behavior.
     *
     * If no behavior is active, or if the active behavior hasn't provided an
     * input for this channel, it will be tied to zero.
     *
     * @return the arbitrated Float channel
     * @see #addFloat(FloatInput)
     */
    @SetupPhase
    public ArbitratedFloat addFloat() {
        return addFloat(FloatInput.zero);
    }

    /**
     * Provides an arbitrated Boolean channel controlled by this behavior.
     *
     * @param base the input to use when no behavior is active, or if the
     * currently active behavior hasn't provided an input for this channel.
     * @return the arbitrated Boolean channel
     * @see #addBoolean()
     */
    @SetupPhase
    public ArbitratedBoolean addBoolean(BooleanInput base) {
        if (base == null) {
            throw new NullPointerException();
        }
        return new ArbitratedBoolean(this, base);
    }

    /**
     * Provides an arbitrated Boolean channel controlled by this behavior.
     *
     * If no behavior is active, or if the active behavior hasn't provided an
     * input for this channel, it will be tied to false.
     *
     * @return the arbitrated Boolean channel
     * @see #addBoolean(BooleanInput)
     */
    @SetupPhase
    public ArbitratedBoolean addBoolean() {
        return addBoolean(BooleanInput.alwaysFalse);
    }

    /**
     * Provides an arbitrated Event channel controlled by this behavior.
     *
     * @param base the input to use when no behavior is active, or if the
     * currently active behavior hasn't provided an input for this channel.
     * @return the arbitrated Event channel
     * @see #addEvent()
     */
    @SetupPhase
    public ArbitratedEvent addEvent(EventInput base) {
        if (base == null) {
            throw new NullPointerException();
        }
        return new ArbitratedEvent(this, base);
    }

    /**
     * Provides an arbitrated Event channel controlled by this behavior.
     *
     * If no behavior is active, or if the active behavior hasn't provided an
     * input for this channel, it will never fire.
     *
     * @return the arbitrated Event channel
     * @see #addEvent(EventInput)
     */
    @SetupPhase
    public ArbitratedEvent addEvent() {
        return addEvent(EventInput.never);
    }

    /**
     * Provides a BooleanInput that is true exactly when there are no active
     * behaviors - that is, no behaviors attempted to run.
     *
     * @return a BooleanInput
     */
    @SetupPhase
    public BooleanInput getIsInactive() {
        return new DerivedBooleanInput(onActiveUpdateCell) {
            @Override
            protected boolean apply() {
                return active == null;
            }
        };
    }

    /**
     * Provides a BooleanInput that is true exactly when <code>behavior</code>
     * is the active behavior - that is, it attempted to run, and no
     * higher-priority behaviors attempted to run.
     *
     * @param behavior the behavior to monitor
     * @return a BooleanInput
     */
    @SetupPhase
    public BooleanInput getIsActive(Behavior behavior) {
        if (behavior == null) {
            throw new NullPointerException();
        }
        return new DerivedBooleanInput(onActiveUpdateCell) {
            @Override
            protected boolean apply() {
                return active == behavior;
            }
        };
    }

    /**
     * Creates a registered behavior state for this behavior arbitrator, with
     * <code>name</code> as the name. The behavior will attempt to run while the
     * <code>request</code> parameter is set to true.
     *
     * Later calls to that method create behaviors with a higher priority than
     * previous calls.
     *
     * @param name the name for the behavior state, as displayed to the user
     * over RConf.
     * @param request when this behavior should be trying to activate.
     * @return the created Behavior.
     */
    @SetupPhase
    public Behavior addBehavior(String name, BooleanInput request) {
        if (name == null || request == null) {
            throw new NullPointerException();
        }
        request.onChange(this::checkUpdate);
        Behavior behavior = new Behavior(this, request, name);
        behaviors.add(behavior);
        checkUpdate();
        return behavior;
    }

    /**
     * Provides an EventInput that fires whenever the active behavior changes.
     *
     * @return the behavior change event.
     */
    @SetupPhase
    public EventInput onBehaviorChange() {
        return this.onActiveUpdate;
    }

    /**
     * Fires <code>event</code> whenever the active behavior changes.
     * 
     * @param event the event to fire
     */
    @SetupPhase
    public void onBehaviorChange(EventOutput event) {
        this.onActiveUpdate.send(event);
    }

    private final DiscreteType<Behavior> discreteType = new DiscreteType<Behavior>() {
        @Override
        public Class<Behavior> getType() {
            return Behavior.class;
        }

        @Override
        public Behavior[] getOptions() {
            Behavior[] bhs = new Behavior[behaviors.size() + 1];
            System.arraycopy(behaviors.toArray(), 0, bhs, 1, bhs.length - 1);
            return bhs;
        }

        @Override
        public boolean isOption(Behavior e) {
            return e == null || e.parent == BehaviorArbitrator.this;
        }

        @Override
        public String toString(Behavior e) {
            return e == null ? "none" : e.getName();
        }

        @Override
        public Behavior getDefaultValue() {
            return null;
        }
    };

    /**
     * Gets the current active behavior as a discrete input.
     *
     * @return the active behavior.
     */
    @SetupPhase
    public DiscreteInput<Behavior> getActiveBehavior() {
        return new DerivedDiscreteInput<Behavior>(discreteType, onActiveUpdate) {
            @Override
            protected Behavior apply() {
                return active;
            }
        };
    }

    /**
     * Gets the name of this behavior arbitrator.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "[BehaviorArbitrator " + name + "]";
    }

    @Override
    public RConf.Entry[] queryRConf() {
        ArrayList<RConf.Entry> ents = new ArrayList<>();
        ents.add(RConf.title("Behaviors for " + name));
        for (Behavior b : behaviors) {
            if (b == active) {
                ents.add(RConf.string("  Active: " + b.getName()));
            } else if (b.request.get()) {
                ents.add(RConf.string(" Standby: " + b.getName()));
            } else {
                ents.add(RConf.string("Inactive: " + b.getName()));
            }
        }
        if (active == null) {
            ents.add(RConf.string("No active behavior"));
        } else {
            ents.add(RConf.string("Active behavior"));
        }
        ents.add(RConf.autoRefresh(1000));
        return ents.toArray(new RConf.Entry[ents.size()]);
    }

    @Override
    public boolean signalRConf(int field, byte[] data) {
        return false;
    }
}
