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

import java.util.HashMap;

import ccre.channel.CancelOutput;
import ccre.channel.EventOutput;
import ccre.verifier.FlowPhase;
import ccre.channel.EventCell;
import ccre.channel.EventInput;

/**
 * An event channel that can be overridden for specific behaviors of a behavior
 * arbitrator.
 *
 * @see #attach(Behavior, EventInput)
 * @author skeggsc
 */
public final class ArbitratedEvent implements EventInput {

    private final BehaviorArbitrator behaviorChain;
    private final EventInput general;
    private final HashMap<Behavior, EventInput> activation = new HashMap<>();
    private CancelOutput cancellator;
    private final EventCell cell = new EventCell();

    ArbitratedEvent(BehaviorArbitrator behaviorChain, EventInput base) {
        this.behaviorChain = behaviorChain;
        this.general = base;
        cancellator = CancelOutput.nothing;
        this.update();
        behaviorChain.onActiveUpdate.send(this::update);
    }

    @FlowPhase
    synchronized void update() {
        cancellator.cancel();
        EventInput input = activation.get(this.behaviorChain.active);
        if (input == null) {
            input = general;
        }
        cancellator = input.send(cell);
    }

    /**
     * Attaches <code>input</code> such that it will be used instead of the
     * default source when the behavior arbitrator has <code>behavior</code>
     * active.
     *
     * This will be overridden by a higher-priority behavior, even if that
     * behavior does not override this event channel.
     *
     * @param behavior the behavior to attach to
     * @param input the input to use to override the default
     */
    public void attach(Behavior behavior, EventInput input) {
        if (behavior == null || input == null) {
            throw new NullPointerException();
        }
        if (behavior.parent != behaviorChain) {
            throw new IllegalArgumentException("Attached behavior for different BehaviorChain!");
        }
        if (activation.containsKey(behavior)) {
            throw new IllegalArgumentException("Behavior already added: " + behavior);
        }
        activation.put(behavior, input);
        update();
    }

    @Override
    public CancelOutput onUpdate(EventOutput notify) {
        return cell.onUpdate(notify);
    }
}
