/*
 * Copyright 2015 Colby Skeggs
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.time.Time;

/**
 * A fake implementation of ExtendedMotor that is suitable for use when
 * communications could not be established initially with the device.
 *
 * This will provide lots of stubbed-out functionality and will log warning
 * messages whenever it is interacted with.
 *
 * @author skeggsc
 */
public class CommunicationFailureExtendedMotor extends ExtendedMotor implements FloatOutput {
    private static Logger logger = LoggerFactory.getLogger(CommunicationFailureExtendedMotor.class);

    private final String message;

    /**
     * Create a new CommunicationFailureExtendedMotor with a message to include
     * whenever the issue is reported.
     *
     * @param message the message to include.
     */
    public CommunicationFailureExtendedMotor(String message) {
        this.message = message;
    }

    @Override
    public void enable() throws ExtendedMotorFailureException {
        throw new ExtendedMotorFailureException("Communications failed: " + message);
    }

    @Override
    public void disable() throws ExtendedMotorFailureException {
        throw new ExtendedMotorFailureException("Communications failed: " + message);
    }

    @Override
    public BooleanOutput asEnable() {
        return new BooleanOutput() {
            public void set(boolean value) {
                logger.warn("Motor control (enable/disable) failed: " + message);
            }
        };
    }

    @Override
    public FloatOutput asMode(OutputControlMode mode) {
        logger.error("Could not access mode of Extended Motor: " + message);
        return this;
    }

    @Override
    public FloatInput asStatus(StatusType type, EventInput updateOn) {
        logger.error("Could not access status of Extended Motor: " + message);
        return FloatInput.zero;
    }

    @Override
    public Object getDiagnostics(DiagnosticType type) {
        if (type == DiagnosticType.COMMS_FAULT || type == DiagnosticType.ANY_FAULT) {
            return true;
        } else if (type.isBooleanDiagnostic) {
            return false;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasInternalPID() {
        return false;
    }

    @Override
    public void setInternalPID(float P, float I, float D) throws ExtendedMotorFailureException {
        logger.warn("Tried to set PID on Extended Motor with failed comms: " + message);
    }

    private long nextWarning = 0;

    public void set(float value) {
        long now = Time.currentTimeMillis();
        if (now > nextWarning) {
            logger.warn("Could not modify Extended Motor value - failed comms: " + message);
            nextWarning = now + 3000;
        }
    }
}
