/*
 * Copyright 2013-2016 Cel Skeggs
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
package ccre.discrete;

import java.io.Serializable;
import java.util.Objects;

import ccre.channel.AbstractUpdatingInput;

/**
 * A virtual node that is both a DiscreteOutput and a DiscreteInput. You can
 * modify its value, read its value, and subscribe to changes in its value.
 *
 * @author skeggsc
 * @param <E> the type of the discrete data
 */
public class DiscreteCell<E> extends AbstractUpdatingInput implements Serializable, DiscreteIO<E> {

    private static final long serialVersionUID = -2654542899148596828L;
    private final DiscreteType<E> type;
    private E value;

    /**
     * Create a new BooleanCell with the value of false.
     *
     * @param type the discrete type for this cell
     */
    public DiscreteCell(DiscreteType<E> type) {
        this.type = type;
        this.value = type.getDefaultValue();
    }

    /**
     * Create a new BooleanCell with a specified value.
     *
     * @param type the discrete type for this cell
     * @param default_ The default value.
     */
    public DiscreteCell(DiscreteType<E> type, E default_) {
        this.type = type;
        if (!type.isOption(default_)) {
            throw new IllegalArgumentException("Option is not a valid instance of type: " + type + ", instance " + default_);
        }
        this.value = default_;
    }

    @Override
    public DiscreteType<E> getType() {
        return type;
    }

    /**
     * Create a new BooleanCell with the value of false that automatically
     * updates all of the specified BooleanOutputs with the current state of
     * this BooleanCell. This is the same as creating a new BooleanCell and then
     * adding all of the BooleanOutputs as targets.
     *
     * @param type the discrete type for this cell
     * @param targets The BooleanOutputs to automatically update.
     * @see DiscreteCell#send(ccre.discrete.DiscreteOutput)
     */
    @SafeVarargs
    public DiscreteCell(DiscreteType<E> type, DiscreteOutput<E>... targets) {
        this.type = type;
        if (type == null) {
            throw new NullPointerException();
        }
        for (DiscreteOutput<E> out : targets) {
            if (!type.equals(out.getType())) {
                throw new IllegalArgumentException("Discrete type mismatch in compound DiscreteCell constructor");
            }
            send(out);
        }
    }

    public final synchronized void set(E value) {
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            perform();
        }
    }

    public final synchronized E get() {
        return value;
    }
}
