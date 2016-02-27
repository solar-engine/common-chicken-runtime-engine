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
package ccre.scheduler;

import ccre.channel.CancelOutput;
import ccre.channel.EventOutput;
import ccre.time.Time;

class FullLoop {
    private final class FixedRateEvent extends CancellableScheduleEntry {
        private long nextScheduleAtNanos;
        private final long periodNanos;
        private final boolean skippable;
        private final String tag;

        public FixedRateEvent(String tag, long firstAtNanos, long periodNanos, boolean skippable, EventOutput o) {
            super(o);
            this.tag = tag;
            this.nextScheduleAtNanos = firstAtNanos;
            this.periodNanos = periodNanos;
            this.skippable = skippable;
        }

        public void schedule() {
            // purposeful comparison order to avoid overflow errors
            if (this.skippable && this.nextScheduleAtNanos - Time.currentTimeNanos() < 0) {
                // skip a bit to deal with load
                this.nextScheduleAtNanos += this.periodNanos;
            }
            scheduleOnce(tag, this.nextScheduleAtNanos, this);
            this.nextScheduleAtNanos += this.periodNanos;
        }

        @Override
        public void event() {
            if (!cancel) {
                this.schedule();
                this.o.event();
            }
        }
    }

    private final class VariableRateEvent extends CancellableScheduleEntry {
        private final long periodNanos;
        private final String tag;

        public VariableRateEvent(String tag, long periodNanos, EventOutput o) {
            super(o);
            this.tag = tag;
            this.periodNanos = periodNanos;
        }

        public void schedule() {
            scheduleOnce(tag, Time.currentTimeNanos() + this.periodNanos, this);
        }

        @Override
        public void event() {
            if (!cancel) {
                this.schedule();
                this.o.event();
            }
        }
    }

    private class CancellableScheduleEntry implements EventOutput, CancelOutput {
        final EventOutput o;
        volatile boolean cancel;

        public CancellableScheduleEntry(EventOutput o) {
            this.o = o;
        }

        @Override
        public void event() {
            if (!cancel) {
                this.o.event();
            }
        }

        @Override
        public void cancel() {
            this.cancel = true;
        }
    }

    private final IRunLoop rl;

    FullLoop(IRunLoop rl) {
        this.rl = rl;
    }

    FullLoop() {
        this.rl = new RunLoop();
    }

    public void scheduleOnce(String tag, long timeAtNanos, EventOutput o) {
        rl.add(tag, o, timeAtNanos);
    }

    public CancelOutput scheduleCancellableOnce(String tag, long timeAtNanos, EventOutput o) {
        CancellableScheduleEntry event = new CancellableScheduleEntry(o);
        scheduleOnce(tag, timeAtNanos, event);
        return event;
    }

    public CancelOutput scheduleFixedRate(String tag, long firstAtNanos, long periodNanos, boolean skippable, EventOutput o) {
        FixedRateEvent event = new FixedRateEvent(tag, firstAtNanos, periodNanos, skippable, o);
        event.schedule();
        return event;
    }

    public CancelOutput scheduleVariableRate(String tag, long periodNanos, EventOutput o) {
        VariableRateEvent event = new VariableRateEvent(tag, periodNanos, o);
        event.schedule();
        return event;
    }

    public void start() {
        rl.start();
    }

    public void terminate() {
        rl.terminate();
    }
}
