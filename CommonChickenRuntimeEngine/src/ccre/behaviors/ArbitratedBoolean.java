/*
 * Copyright 2016 Colby Skeggs
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
import ccre.channel.BooleanCell;
import ccre.channel.BooleanInput;

/**
 * A boolean channel that can be overridden for specific behaviors of a behavior
 * arbitrator.
 *
 * @see #attach(Behavior, BooleanInput)
 * @author skeggsc
 */
public final class ArbitratedBoolean implements BooleanInput {

    private final BehaviorArbitrator behaviorChain;
    private final BooleanInput general;
    private final HashMap<Behavior, BooleanInput> activation = new HashMap<>();
    private CancelOutput cancellator;
    private final BooleanCell cell = new BooleanCell();

    ArbitratedBoolean(BehaviorArbitrator behaviorChain, BooleanInput base) {
        this.behaviorChain = behaviorChain;
        this.general = base;
        cancellator = CancelOutput.nothing;
        this.update();
        behaviorChain.onActiveUpdate.send(this::update);
    }

    synchronized void update() {
        cancellator.cancel();
        BooleanInput input = activation.get(this.behaviorChain.active);
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
    public void attach(Behavior behavior, BooleanInput input) {
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

    @Override
    public boolean get() {
        return cell.get();
    }
}
