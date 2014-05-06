/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatFilter;
import ccre.channel.FloatInput;
import ccre.channel.FloatInputPoll;
import ccre.channel.FloatOutput;
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

        public float get() {
            return value;
        }

        public void send(FloatOutput consum) {
            consum.set(value);
        }

        public void unsend(FloatOutput consum) {
        }
    }

    static class AndBooleansImpl implements BooleanInputPoll {

        private final BooleanInputPoll[] vals;

        AndBooleansImpl(BooleanInputPoll[] vals) {
            this.vals = vals;
        }

        public boolean get() {
            for (BooleanInputPoll val : vals) {
                if (!val.get()) {
                    return false;
                }
            }
            return true;
        }
    }

    static class AndBooleansImpl2 implements BooleanInputPoll {

        private final BooleanInputPoll a;
        private final BooleanInputPoll b;

        AndBooleansImpl2(BooleanInputPoll a, BooleanInputPoll b) {
            this.a = a;
            this.b = b;
        }

        public boolean get() {
            return a.get() && b.get();
        }
    }

    static final class BCF implements FloatInput, BooleanOutput {

        private final float off, on;
        private float cur;
        private CArrayList<FloatOutput> consumers = null;

        BCF(boolean default_, float off, float on) {
            this.off = off;
            this.on = on;
            cur = default_ ? on : off;
        }

        public float get() {
            return cur;
        }

        public void send(FloatOutput consum) {
            if (consumers == null) {
                consumers = new CArrayList<FloatOutput>();
            }
            consumers.add(consum);
            consum.set(cur);
        }

        public void unsend(FloatOutput consum) {
            if (consumers != null) {
                consumers.remove(consum);
            }
        }

        public void set(boolean value) {
            cur = value ? on : off;
            if (consumers != null) {
                for (FloatOutput out : consumers) {
                    out.set(cur);
                }
            }
        }
    }

    static final class BCF2 implements FloatInput, BooleanOutput {

        private final FloatInputPoll off, on;
        private float cur;
        private CArrayList<FloatOutput> consumers = null;

        BCF2(boolean default_, FloatInputPoll off, FloatInputPoll on) {
            this.off = off;
            this.on = on;
            cur = default_ ? on.get() : off.get();
        }

        public float get() {
            return cur;
        }

        public void send(FloatOutput consum) {
            if (consumers == null) {
                consumers = new CArrayList<FloatOutput>();
            }
            consumers.add(consum);
            consum.set(cur);
        }

        public void unsend(FloatOutput consum) {
            if (consumers != null) {
                consumers.remove(consum);
            }
        }

        public void set(boolean value) {
            cur = value ? on.get() : off.get();
            if (consumers != null) {
                for (FloatOutput out : consumers) {
                    out.set(cur);
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

        public void set(boolean value) {
            bout.set(value ? on : off);
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

        public float get() {
            return binp.get() ? on : off;
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

        public float get() {
            return selector.get() ? on.get() : off.get();
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

    static class DebounceImpl implements EventOutput {

        private final EventOutput orig;
        private long nextFire = 0;
        private final int delay;

        DebounceImpl(EventOutput orig, int delay) {
            this.orig = orig;
            this.delay = delay;
        }

        public void event() {
            long now = System.currentTimeMillis();
            if (now < nextFire) {
                return; // Ignore event.
            }
            nextFire = now + delay;
            orig.event();
        }
    }

    static class DZI implements FloatInputPoll {

        private final FloatInputPoll value;
        private final float deadzone;

        DZI(FloatInputPoll value, float deadzone) {
            this.value = value;
            this.deadzone = deadzone;
        }

        public float get() {
            return Utils.deadzone(value.get(), deadzone);
        }
    }

    static class DZO implements FloatOutput {

        private final FloatOutput value;
        private final float deadzone;

        DZO(FloatOutput value, float deadzone) {
            this.value = value;
            this.deadzone = deadzone;
        }

        public void set(float newValue) {
            value.set(Utils.deadzone(newValue, deadzone));
        }
    }

    static class FEC implements EventOutput {

        private final BooleanInputPoll shouldAllow;
        private final boolean requirement;
        private final EventOutput cnsm;

        FEC(BooleanInputPoll shouldAllow, boolean requirement, EventOutput cnsm) {
            this.shouldAllow = shouldAllow;
            this.requirement = requirement;
            this.cnsm = cnsm;
        }

        public void event() {
            if (shouldAllow.get() == requirement) {
                cnsm.event();
            }
        }
    }

    static class FES implements EventOutput {

        private final BooleanInputPoll shouldAllow;
        private final boolean requirement;
        private final EventStatus out;

        FES(BooleanInputPoll shouldAllow, boolean requirement, EventStatus out) {
            this.shouldAllow = shouldAllow;
            this.requirement = requirement;
            this.out = out;
        }

        public void event() {
            if (shouldAllow.get() == requirement) {
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

        public boolean get() {
            return base.get() >= minimum;
        }
    }

    static class FIAM implements BooleanInputPoll {

        private final FloatInputPoll base;
        private final float maximum;

        FIAM(FloatInputPoll base, float maximum) {
            this.base = base;
            this.maximum = maximum;
        }

        public boolean get() {
            return base.get() <= maximum;
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

        public boolean get() {
            float val = base.get();
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

        public boolean get() {
            float val = base.get();
            return val < minimum || val > maximum;
        }
    }

    static class FindRateImpl implements FloatInputPoll {

        private final FloatInputPoll input;
        private float lastValue;

        FindRateImpl(FloatInputPoll input) {
            this.input = input;
            this.lastValue = input.get();
        }

        public synchronized float get() {
            float next = input.get();
            float out = next - lastValue;
            lastValue = next;
            return out;
        }
    }

    static class FindRateCycledImpl implements FloatInputPoll {

        private final FloatInputPoll input;
        float lastValue;

        FindRateCycledImpl(FloatInputPoll input) {
            this.input = input;
            this.lastValue = input.get();
        }

        public FloatInputPoll start(EventInput updateWhen) {
            updateWhen.send(new EventOutput() {
                public void event() {
                    lastValue = input.get();
                }
            });
            return this;
        }

        public synchronized float get() {
            return input.get() - lastValue;
        }
    }

    static class GSEB implements EventOutput {

        private final BooleanOutput out;
        private final boolean value;

        GSEB(BooleanOutput out, boolean value) {
            this.out = out;
            this.value = value;
        }

        public void event() {
            out.set(value);
        }
    }

    static class GSEF implements EventOutput {

        private final FloatOutput out;
        private final float value;

        GSEF(FloatOutput out, float value) {
            this.out = out;
            this.value = value;
        }

        public void event() {
            out.set(value);
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

        public float get() {
            return (base.get() - zero) / range;
        }
    }

    static class OrBooleansImpl implements BooleanInputPoll {

        private final BooleanInputPoll[] vals;

        OrBooleansImpl(BooleanInputPoll[] vals) {
            this.vals = vals;
        }

        public boolean get() {
            for (BooleanInputPoll val : vals) {
                if (val.get()) {
                    return true;
                }
            }
            return false;
        }
    }

    static class OrBooleansImpl2 implements BooleanInputPoll {

        private final BooleanInputPoll a;
        private final BooleanInputPoll b;

        OrBooleansImpl2(BooleanInputPoll a, BooleanInputPoll b) {
            this.a = a;
            this.b = b;
        }

        public boolean get() {
            return a.get() || b.get();
        }
    }

    static class XorBooleansImpl implements BooleanInputPoll {

        private final BooleanInputPoll a;
        private final BooleanInputPoll b;

        XorBooleansImpl(BooleanInputPoll a, BooleanInputPoll b) {
            this.a = a;
            this.b = b;
        }

        public boolean get() {
            return a.get() ^ b.get();
        }
    }

    static class PumpEventImplF implements EventOutput {

        private final FloatOutput out;
        private final FloatInputPoll in;

        PumpEventImplF(FloatOutput out, FloatInputPoll in) {
            this.out = out;
            this.in = in;
        }

        public void event() {
            out.set(in.get());
        }
    }

    static class PumpEventImplB implements EventOutput {

        private final BooleanOutput out;
        private final BooleanInputPoll in;

        PumpEventImplB(BooleanOutput out, BooleanInputPoll in) {
            this.out = out;
            this.in = in;
        }

        public void event() {
            out.set(in.get());
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

        public float get() {
            return alpha.get() ? (beta.get() ? tt : tf) : (beta.get() ? ft : ff);
        }
    }

    static class QuadSelectImpl2 implements FloatInputPoll {

        private final BooleanInputPoll alpha;
        private final BooleanInputPoll beta;
        private final FloatInputPoll tt;
        private final FloatInputPoll tf;
        private final FloatInputPoll ft;
        private final FloatInputPoll ff;

        QuadSelectImpl2(BooleanInputPoll alpha, BooleanInputPoll beta, FloatInputPoll tt, FloatInputPoll tf, FloatInputPoll ft, FloatInputPoll ff) {
            this.alpha = alpha;
            this.beta = beta;
            this.tt = tt;
            this.tf = tf;
            this.ft = ft;
            this.ff = ff;
        }

        public float get() {
            return (alpha.get() ? (beta.get() ? tt : tf) : (beta.get() ? ft : ff)).get();
        }
    }

    static class RampingImpl implements EventOutput {

        private final FloatInputPoll from;
        private final float limit;
        private final FloatOutput target;
        private float last;

        RampingImpl(FloatInputPoll from, float limit, FloatOutput target) {
            this.from = from;
            this.limit = limit;
            this.target = target;
            last = from.get();
        }

        public void event() {
            last = Utils.updateRamping(last, from.get(), limit);
            target.set(last);
        }
    }

    static class WBBI implements BooleanOutput {

        private final boolean target;
        private final EventStatus out;
        private boolean last;

        WBBI(boolean target, EventStatus out) {
            this.target = target;
            this.out = out;
        }

        public void set(boolean value) {
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
