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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.discrete.DiscreteOutput;
import ccre.discrete.DiscreteType;
import ccre.recording.Recorder;

class RecordedRunLoop extends RunLoop {

    public RecordedRunLoop(Recorder rec) {
        awaiting = rec.createBooleanOutput("SCHED-AWAIT");
        active = rec.createDiscreteOutput("SCHED-DISPATCH", tagType);
    }

    private final BooleanOutput awaiting;
    private final DiscreteOutput<String> active;
    private final Set<String> tags = Collections.synchronizedSet(new HashSet<>(Arrays.asList((String) null)));
    private final DiscreteType<String> tagType = new DiscreteType<String>() {
        @Override
        public Class<String> getType() {
            return String.class;
        }

        @Override
        public String[] getOptions() {
            return tags.toArray(new String[tags.size()]);
        }

        @Override
        public boolean isOption(String value) {
            return tags.contains(value);
        }

        @Override
        public String toString(String value) {
            return value == null ? "idle" : value;
        }

        @Override
        public String getDefaultValue() {
            return null;
        }
    };

    public void add(String tag, EventOutput event, long time) {
        tags.add(tag);
        super.add(tag, event, time);
    }

    @Override
    protected void reportAwaiting(boolean isAwaiting) {
        awaiting.set(isAwaiting);
    }

    @Override
    protected void reportActive(String tag) {
        active.set(tag);
    }
}
