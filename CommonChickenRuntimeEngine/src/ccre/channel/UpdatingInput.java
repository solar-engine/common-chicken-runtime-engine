/*
 * Copyright 2015 Cel Skeggs
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
package ccre.channel;

import java.io.Serializable;

/**
 * A representation of something that can update and might want to tell
 * listeners about this fact.
 *
 * @author skeggsc
 */
public interface UpdatingInput extends Serializable {

    /**
     * Register the specified listener. Once this is called, then whenever the
     * represented data source updates, the specified EventOutput will be fired.
     *
     * This returns an EventOutput to deregister the EventOutput registered by
     * this method. DO NOT FIRE THIS EVENT MORE THAN ONCE: UNDEFINED BEHAVIOR
     * MAY RESULT. You do not have to fire this at all, of course, and will
     * usually just ignore it.
     *
     * Once deregistered, the EventOutput will receive no further notifications
     * and no further reference will be held to it as part of the registration,
     * which may make it eligible for garbage collection.
     *
     * @param notify the EventOutput to fire when this UpdatingInput updates.
     * @return an EventOutput that deregisters the registered EventOutput. DO
     * NOT FIRE THIS RETURNED EVENT MORE THAN ONCE: UNDEFINED BEHAVIOR MAY
     * RESULT.
     */
    public CancelOutput onUpdate(EventOutput notify);
}
