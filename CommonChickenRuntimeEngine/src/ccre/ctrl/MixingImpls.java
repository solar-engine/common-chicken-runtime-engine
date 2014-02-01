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

import ccre.chan.BooleanInputPoll;
import ccre.chan.BooleanInputProducer;
import ccre.chan.BooleanOutput;
import ccre.chan.FloatFilter;
import ccre.chan.FloatInput;
import ccre.chan.FloatInputPoll;
import ccre.chan.FloatOutput;
import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.Logger;
import ccre.util.CArrayList;
import ccre.util.Utils;

/**
 * Contains implementation details of much of Mixing - the internal one-use
 * classes go here.
 *
 * @author skeggsc
 */
class MixingImpls {

    static class Always implements FloatInput {

        private final float value;

        Always(float value) {
            this.value = value;
        }

        public float readValue() {
            return value;
        }

        public void addTarget(FloatOutput consum) {
            consum.writeValue(value);
        }

        public boolean removeTarget(FloatOutput consum) {
            Logger.warning("Faked removeTarget for Mixing.always(" + value + ")");
            return true; // Faked!
        }
    }

    static class AndBooleansImpl implements BooleanInputPoll {

        private final BooleanInputPoll[] vals;

        public AndBooleansImpl(BooleanInputPoll[] vals) {
            this.vals = vals;
        }

        public boolean readValue() {
            for (BooleanInputPoll val : vals) {
                if (!val.readValue()) {
                    return false;
                }
            }
            return true;
        }
    }

    static class AndBooleansImpl2 implements BooleanInputPoll {

        private final BooleanInputPoll a;
        private final BooleanInputPoll b;

        public AndBooleansImpl2(BooleanInputPoll a, BooleanInputPoll b) {
            this.a = a;
            this.b = b;
        }

        public boolean readValue() {
            return a.readValue() && b.readValue();
        }
    }

    static final class BCF implements FloatInput, BooleanOutput {

        private final BooleanInputProducer binp;
        private final float off, on;
        private float cur;
        private CArrayList<FloatOutput> consumers = null;

        BCF(BooleanInputProducer binp, boolean default_, float off, float on) {
            this.binp = binp;
            this.off = off;
            this.on = on;
            binp.addTarget(this);
            cur = default_ ? on : off;
        }

        public float readValue() {
            return cur;
        }

        public void addTarget(FloatOutput consum) {
            if (consumers == null) {
                consumers = new CArrayList<FloatOutput>();
            }
            consumers.add(consum);
            consum.writeValue(cur);
        }

        public boolean removeTarget(FloatOutput consum) {
            if (consumers != null) {
                consumers.remove(consum);
                return true;
            }
            return false;
        }

        public void writeValue(boolean value) {
            cur = value ? on : off;
            if (consumers != null) {
                for (FloatOutput out : consumers) {
                    out.writeValue(cur);
                }
            }
        }
    }

    static final class BCF2 implements FloatInput, BooleanOutput {

        private final BooleanInputProducer binp;
        private final FloatInputPoll off, on;
        private float cur;
        private CArrayList<FloatOutput> consumers = null;

        BCF2(BooleanInputProducer binp, boolean default_, FloatInputPoll off, FloatInputPoll on) {
            this.binp = binp;
            this.off = off;
            this.on = on;
            binp.addTarget(this);
            cur = default_ ? on.readValue() : off.readValue();
        }

        public float readValue() {
            return cur;
        }

        public void addTarget(FloatOutput consum) {
            if (consumers == null) {
                consumers = new CArrayList<FloatOutput>();
            }
            consumers.add(consum);
            consum.writeValue(cur);
        }

        public boolean removeTarget(FloatOutput consum) {
            if (consumers != null) {
                consumers.remove(consum);
                return true;
            }
            return false;
        }

        public void writeValue(boolean value) {
            cur = value ? on.readValue() : off.readValue();
            if (consumers != null) {
                for (FloatOutput out : consumers) {
                    out.writeValue(cur);
                }
            }
        }
    }

    static final class BSF implements BooleanOutput {

        private final float off;
        private final float on;
        private final FloatOutput bout;

        BSF(FloatOutput bout, float off, float on) {
            this.bout = bout;
            this.off = off;
            this.on = on;
        }

        public void writeValue(boolean value) {
            bout.writeValue(value ? on : off);
        }
    }

    static class BSF2 implements FloatInputPoll {

        private final BooleanInputPoll binp;
        private final float off;
        private final float on;

        BSF2(BooleanInputPoll binp, float off, float on) {
            this.binp = binp;
            this.off = off;
            this.on = on;
        }

        public float readValue() {
            return binp.readValue() ? on : off;
        }
    }

    static class BooleanSelectFloatImpl implements FloatInputPoll {

        private final BooleanInputPoll selector;
        private final FloatInputPoll on;
        private final FloatInputPoll off;

        BooleanSelectFloatImpl(BooleanInputPoll selector, FloatInputPoll on, FloatInputPoll off) {
            this.selector = selector;
            this.on = on;
            this.off = off;
        }

        public float readValue() {
            return selector.readValue() ? on.readValue() : off.readValue();
        }
    }

    static class DeadzoneImpl extends FloatFilter {

        private final float deadzone;

        DeadzoneImpl(float deadzone) {
            this.deadzone = deadzone;
        }

        @Override
        public float filter(float input) {
            return Utils.deadzone(input, deadzone);
        }
    }

    static class DebounceImpl implements EventConsumer {

        private final EventConsumer orig;
        private long nextFire = 0;
        private final int delay;

        DebounceImpl(EventConsumer orig, int delay) {
            this.orig = orig;
            this.delay = delay;
        }

        public void eventFired() {
            long now = System.currentTimeMillis();
            if (now < nextFire) {
                return; // Ignore event.
            }
            nextFire = now + delay;
            orig.eventFired();
        }
    }

    static class DZI implements FloatInputPoll {

        private final FloatInputPoll value;
        private final float deadzone;

        DZI(FloatInputPoll value, float deadzone) {
            this.value = value;
            this.deadzone = deadzone;
        }

        public float readValue() {
            return Utils.deadzone(value.readValue(), deadzone);
        }
    }

    static class DZO implements FloatOutput {

        private final FloatOutput value;
        private final float deadzone;

        DZO(FloatOutput value, float deadzone) {
            this.value = value;
            this.deadzone = deadzone;
        }

        public void writeValue(float newValue) {
            value.writeValue(Utils.deadzone(newValue, deadzone));
        }
    }

    static class FEC implements EventConsumer {

        private final BooleanInputPoll shouldAllow;
        private final boolean requirement;
        private final EventConsumer cnsm;

        FEC(BooleanInputPoll shouldAllow, boolean requirement, EventConsumer cnsm) {
            this.shouldAllow = shouldAllow;
            this.requirement = requirement;
            this.cnsm = cnsm;
        }

        public void eventFired() {
            if (shouldAllow.readValue() == requirement) {
                cnsm.eventFired();
            }
        }
    }

    static class FES implements EventConsumer {

        private final BooleanInputPoll shouldAllow;
        private final boolean requirement;
        private final Event out;

        FES(BooleanInputPoll shouldAllow, boolean requirement, Event out) {
            this.shouldAllow = shouldAllow;
            this.requirement = requirement;
            this.out = out;
        }

        public void eventFired() {
            if (shouldAllow.readValue() == requirement) {
                out.produce();
            }
        }
    }

    static class FIAL implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float minimum;

        FIAL(FloatInputPoll base, float minimum) {
            this.base = base;
            this.minimum = minimum;
        }

        public boolean readValue() {
            return base.readValue() >= minimum;
        }
    }

    static class FIAM implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float maximum;

        FIAM(FloatInputPoll base, float maximum) {
            this.base = base;
            this.maximum = maximum;
        }

        public boolean readValue() {
            return base.readValue() <= maximum;
        }
    }

    static class FIIR implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float minimum;
        private final float maximum;

        FIIR(FloatInputPoll base, float minimum, float maximum) {
            this.base = base;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public boolean readValue() {
            float val = base.readValue();
            return val >= minimum && val <= maximum;
        }
    }

    static class FIOR implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float minimum;
        private final float maximum;

        FIOR(FloatInputPoll base, float minimum, float maximum) {
            this.base = base;
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public boolean readValue() {
            float val = base.readValue();
            return val < minimum || val > maximum;
        }
    }

    static class FindRateImpl implements FloatInputPoll {

        private final FloatInputPoll input;
        private float lastValue;

        FindRateImpl(FloatInputPoll input) {
            this.input = input;
            this.lastValue = input.readValue();
        }

        public synchronized float readValue() {
            float next = input.readValue();
            float out = next - lastValue;
            lastValue = next;
            return out;
        }
    }

    static class FindRateCycledImpl implements FloatInputPoll {

        private final FloatInputPoll input;
        private float lastValue;

        FindRateCycledImpl(FloatInputPoll input) {
            this.input = input;
            this.lastValue = input.readValue();
        }

        public FloatInputPoll start(EventSource updateWhen) {
            updateWhen.addListener(new EventConsumer() {
                public void eventFired() {
                    lastValue = input.readValue();
                }
            });
            return this;
        }

        public synchronized float readValue() {
            return input.readValue() - lastValue;
        }
    }

    static class FloatsEqualImpl implements BooleanInputPoll {

        private final FloatInputPoll a;
        private final FloatInputPoll b;

        public FloatsEqualImpl(FloatInputPoll a, FloatInputPoll b) {
            this.a = a;
            this.b = b;
        }

        public boolean readValue() {
            return a.readValue() == b.readValue();
        }
    }

    static class GSEB implements EventConsumer {

        private final BooleanOutput out;
        private final boolean value;

        GSEB(BooleanOutput out, boolean value) {
            this.out = out;
            this.value = value;
        }

        public void eventFired() {
            out.writeValue(value);
        }
    }

    static class GSEF implements EventConsumer {

        private final FloatOutput out;
        private final float value;

        GSEF(FloatOutput out, float value) {
            this.out = out;
            this.value = value;
        }

        public void eventFired() {
            out.writeValue(value);
        }
    }

    static class LimitImpl extends FloatFilter {

        private final float minimum;
        private final float maximum;

        LimitImpl(float minimum, float maximum) {
            if (maximum < minimum) {
                throw new IllegalArgumentException("Maximum is smaller than minimum!");
            }
            this.minimum = minimum;
            this.maximum = maximum;
        }

        @Override
        public float filter(float input) {
            if (input < minimum) {
                return minimum;
            } else if (input > maximum) {
                return maximum;
            } else {
                return input;
            }
        }
    }

    static class NFI implements FloatInputPoll {

        private final FloatInputPoll base;
        private final float zero;
        private final float range;

        NFI(FloatInputPoll base, float zero, float range) {
            this.base = base;
            this.zero = zero;
            this.range = range;
        }

        public float readValue() {
            return (base.readValue() - zero) / range;
        }
    }

    static class OrBooleansImpl implements BooleanInputPoll {

        private final BooleanInputPoll[] vals;

        public OrBooleansImpl(BooleanInputPoll[] vals) {
            this.vals = vals;
        }

        public boolean readValue() {
            for (BooleanInputPoll val : vals) {
                if (val.readValue()) {
                    return true;
                }
            }
            return false;
        }
    }

    static class OrBooleansImpl2 implements BooleanInputPoll {

        private final BooleanInputPoll a;
        private final BooleanInputPoll b;

        public OrBooleansImpl2(BooleanInputPoll a, BooleanInputPoll b) {
            this.a = a;
            this.b = b;
        }

        public boolean readValue() {
            return a.readValue() || b.readValue();
        }
    }

    static class PumpEventImplF implements EventConsumer {

        private final FloatOutput out;
        private final FloatInputPoll in;

        public PumpEventImplF(FloatOutput out, FloatInputPoll in) {
            this.out = out;
            this.in = in;
        }

        public void eventFired() {
            out.writeValue(in.readValue());
        }
    }

    static class PumpEventImplB implements EventConsumer {

        private final BooleanOutput out;
        private final BooleanInputPoll in;

        public PumpEventImplB(BooleanOutput out, BooleanInputPoll in) {
            this.out = out;
            this.in = in;
        }

        public void eventFired() {
            out.writeValue(in.readValue());
        }
    }

    static class QuadSelectImpl implements FloatInputPoll {

        private final BooleanInputPoll alpha;
        private final BooleanInputPoll beta;
        private final float tt;
        private final float tf;
        private final float ft;
        private final float ff;

        QuadSelectImpl(BooleanInputPoll alpha, BooleanInputPoll beta, float tt, float tf, float ft, float ff) {
            this.alpha = alpha;
            this.beta = beta;
            this.tt = tt;
            this.tf = tf;
            this.ft = ft;
            this.ff = ff;
        }

        public float readValue() {
            return alpha.readValue() ? (beta.readValue() ? tt : tf) : (beta.readValue() ? ft : ff);
        }
    }

    static class RampingImpl implements EventConsumer {

        private final FloatInputPoll from;
        private final float limit;
        private final FloatOutput target;

        public RampingImpl(FloatInputPoll from, float limit, FloatOutput target) {
            this.from = from;
            this.limit = limit;
            this.target = target;
            last = from.readValue();
        }
        public float last;

        public void eventFired() {
            last = Utils.updateRamping(last, from.readValue(), limit);
            target.writeValue(last);
        }
    }

    static class WBBI implements BooleanOutput {

        private final boolean target;
        private final Event out;

        WBBI(boolean target, Event out) {
            this.target = target;
            this.out = out;
        }
        protected boolean last;

        public void writeValue(boolean value) {
            if (value == last) {
                return;
            }
            last = value;
            if (value == target) {
                out.produce();
            }
        }
    }
}
