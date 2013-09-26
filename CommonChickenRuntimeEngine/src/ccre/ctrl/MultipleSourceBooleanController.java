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
package ccre.ctrl;

import ccre.chan.BooleanInput;
import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanInputProducer;
import ccre.chan.BooleanOutput;
import ccre.event.EventConsumer;
import ccre.util.CArrayList;

/**
 * A controller that combines a series of registered BooleanOutputs or
 * BooleanInputs to create a single output or input line.
 *
 * This is a BooleanInput representing the current value, and an EventConsumer
 * such that once the event is fired, the output will be correct.
 *
 * @author skeggsc
 */
public class MultipleSourceBooleanController implements BooleanInput, EventConsumer {

    public static final boolean AND = true;
    public static final boolean OR = false;
    
    /**
     * Create a new MultipleSourceBooleanController, either as an AND operation
     * over its boolean set or an OR operation
     *
     * @param isAndOperation if an AND operation should be used. an OR operation
     * is used otherwise. The constants AND and OR can be used for nicer-looking
     * code.
     */
    public MultipleSourceBooleanController(boolean isAndOperation) {
        isAnd = isAndOperation;
    }
    /**
     * The list of polled inputs that are read during the update method.
     */
    protected final CArrayList<BooleanInputPoll> ipl = new CArrayList<BooleanInputPoll>();
    /**
     * The list of current values for the asynchronously-updated inputs. Any
     * elements in this list MUST be either Boolean.TRUE or Boolean.FALSE! Even
     * the result of new Boolean(true) is not allowed!
     */
    protected final CArrayList<Boolean> bcur = new CArrayList<Boolean>();
    /**
     * The list of consumers to be notified when the value changes.
     */
    protected final CArrayList<BooleanOutput> consumers = new CArrayList<BooleanOutput>();
    /**
     * The current value of the result.
     */
    protected boolean lastValue = false;
    /**
     * If the operation is an AND operation as opposed to an OR operation.
     */
    protected final boolean isAnd;

    /**
     * Get one BooleanOutput that can be written to in order to update its
     * element of the boolean set.
     *
     * @param dflt the default value before anything is written.
     * @return the BooleanOutput that can be written to.
     */
    public synchronized BooleanOutput getOutput(boolean dflt) {
        final int cur = bcur.size();
        bcur.add(dflt);
        update();
        return new BooleanOutputElement(cur);
    }

    /**
     * Place the specified BooleanInput as an element in the boolean set.
     *
     * @param inp the boolean to include.
     */
    public synchronized void addInput(BooleanInput inp) {
        inp.addTarget(getOutput(inp.readValue()));
        update();
    }

    /**
     * Place the specified BooleanInputProducer as an element in the boolean
     * set.
     *
     * @param inp the boolean to include.
     */
    public synchronized void addInput(BooleanInputProducer inp, boolean default_) {
        inp.addTarget(getOutput(default_));
        update();
    }

    /**
     * Place the specified BooleanInput as an element in the boolean set. Since
     * it is a polling boolean, its value will only be polled when another
     * element changes.
     *
     * @param inp the boolean to include.
     */
    public synchronized void addInput(BooleanInputPoll inp) {
        ipl.add(inp);
        update();
    }

    /**
     * Update the output from the current state.
     */
    protected void update() {
        boolean valOut;
        if (isAnd) {
            if (bcur.contains(Boolean.FALSE)) {
                valOut = false;
            } else {
                valOut = true;
                for (BooleanInputPoll p : ipl) {
                    if (!p.readValue()) {
                        valOut = false;
                        break;
                    }
                }
            }
        } else {
            if (bcur.contains(Boolean.TRUE)) {
                valOut = true;
            } else {
                valOut = false;
                for (BooleanInputPoll p : ipl) {
                    if (p.readValue()) {
                        valOut = true;
                        break;
                    }
                }
            }
        }
        if (valOut != lastValue) {
            lastValue = valOut;
            notifyConsumers();
        }
    }

    private void notifyConsumers() {
        for (BooleanOutput cnsm : consumers) {
            cnsm.writeValue(lastValue);
        }
    }

    public boolean readValue() {
        return lastValue;
    }

    public void addTarget(BooleanOutput output) {
        consumers.add(output);
    }

    public boolean removeTarget(BooleanOutput output) {
        return consumers.remove(output);
    }

    public void eventFired() {
        update();
    }

    private class BooleanOutputElement implements BooleanOutput {

        private final int cur;

        public BooleanOutputElement(int cur) {
            this.cur = cur;
        }

        public void writeValue(boolean value) {
            bcur.set(cur, value);
            update();
        }
    }
}
