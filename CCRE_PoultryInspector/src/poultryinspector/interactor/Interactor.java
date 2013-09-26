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
package poultryinspector.interactor;

import ccre.cluck.CluckGlobals;
import ccre.log.LogLevel;

/**
 * The class that handles interactions with objects on the network. It
 * dispatches to the various Forms declared in this package.
 *
 * @author skeggsc
 */
public class Interactor {

    /**
     * Interact with the object in the specified channel.
     * @param str the channel name.
     */
    public static void interact(String str) {
        if (str == null) {
            return;
        }
        int spt = str.indexOf(':');
        if (spt == -1) {
            return;
        }
        String type = str.substring(0, spt);
        String name = str.substring(spt + 1);
        if (type.isEmpty()) {
            return;
        }
        switch (type.charAt(0)) {
            case 'E':
                if ("EC".equals(type)) {
                    EventConsumerForm.create(name, CluckGlobals.encoder.subscribeEventConsumer(name));
                } else if ("ES".equals(type)) {
                    EventSourceForm.create(name, CluckGlobals.encoder.subscribeEventSource(name));
                }
                break;
            case 'L':
                if ("LT".equals(type)) {
                    LoggingTargetForm.create(name, CluckGlobals.encoder.subscribeLoggingTarget(LogLevel.FINEST, name));
                }
                break;
            case 'B':
                if ("BI".equals(type)) {
                    BooleanInputForm.create(name, CluckGlobals.encoder.subscribeBooleanInputProducer(name, false));
                } else if ("BO".equals(type)) {
                    BooleanOutputForm.create(name, CluckGlobals.encoder.subscribeBooleanOutput(name));
                }
                break;
            case 'F':
                if ("FI".equals(type)) {
                    FloatInputForm.create(name, CluckGlobals.encoder.subscribeFloatInputProducer(name, 0));
                } else if ("FO".equals(type)) {
                    FloatOutputForm.create(name, CluckGlobals.encoder.subscribeFloatOutput(name));
                }
                break;
            case 'S':
                if ("STR".equals(type)) {
                    StringForm.create(name, CluckGlobals.encoder.subscribeStringHolder(name, "Waiting for data..."));
                }
                break;
            case 'T':
                if ("TUNE".equals(type)) {
                    TuneForm.create(name, CluckGlobals.encoder.subscribeTunableFloat(name));
                }
                break;
        }
    }
}
