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

import ccre.channel.BooleanInput;
import ccre.channel.DerivedBooleanInput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.log.CCRELogger;
import ccre.log.LogLevel;

/**
 * A finite-state machine. This has a number of named, predefined states, which
 * can be switched between, and can affect the functionality of other parts of
 * the code.
 *
 * Users can be notified when a state is exited or entered, and can switch
 * between states either regarding or disregarding the current state. They can
 * also, of course, determine the current state.
 *
 * @author skeggsc
 */
public class StateMachine {
    private static Logger logger = LoggerFactory.getLogger(StateMachine.class);
    
    private int currentState;
    private final int numberOfStates;
    private final EventStatus onExit = new EventStatus();
    private final EventStatus onEnter = new EventStatus();
    private final String[] stateNames;

    /**
     * Create a new StateMachine with a named defaultState and a list of state
     * names. The names cannot be null or duplicates.
     *
     * @param defaultState the state to initially be in.
     * @param names the names of the states.
     */
    public StateMachine(String defaultState, String... names) {
        checkNamesConsistency(names);
        this.stateNames = names;
        numberOfStates = names.length;
        setState(defaultState);
    }

    /**
     * Create a new StateMachine with an indexed defaultState and a list of
     * state names.
     *
     * @param defaultState the state to initially be in, as an index in the list
     * of names.
     * @param names the names of the states.
     */
    public StateMachine(int defaultState, String... names) {
        checkNamesConsistency(names);
        this.stateNames = names;
        numberOfStates = names.length;
        setState(defaultState);
    }

    /**
     * @return the number of states
     */
    public int getNumberOfStates() {
        return numberOfStates;
    }

    private static void checkNamesConsistency(String... names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (name == null) {
                throw new NullPointerException();
            }
            for (int j = i + 1; j < names.length; j++) {
                if (name.equals(names[j])) {
                    throw new IllegalArgumentException("Duplicate state name: " + names[i]);
                }
            }
        }
    }

    private int indexOfName(String state) {
        for (int i = 0; i < getNumberOfStates(); i++) {
            if (state.equals(stateNames[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("State name not found: " + state);
    }

    /**
     * Set the state of this machine to the named state.
     *
     * @param state the state to change to.
     */
    public void setState(String state) {
        setState(indexOfName(state));
    }

    /**
     * Set the state of this machine to the indexed state.
     *
     * @param state the state to change to, as an index in the list of state
     * names.
     */
    public void setState(int state) {
        if (state < 0 || state >= getNumberOfStates()) {
            throw new IllegalArgumentException("Invalid state ID: " + state);
        }
        if (state == currentState) {
            return;
        }
        onExit.produce();
        currentState = state;
        onEnter.produce();
    }

    /**
     * Change to the named state when the event occurs.
     *
     * @param state the state to change to.
     * @param when when to change state.
     */
    public void setStateWhen(String state, EventInput when) {
        setStateWhen(indexOfName(state), when);
    }

    /**
     * Change to the indexed state when the event occurs.
     *
     * @param state the state to change to, as an index in the list of state
     * names.
     * @param when when to change state.
     */
    public void setStateWhen(int state, EventInput when) {
        when.send(getStateSetEvent(state));
    }

    /**
     * Get an event that will change the state to the named state.
     *
     * @param state the state to change to.
     * @return the event that changes state.
     */
    public EventOutput getStateSetEvent(String state) {
        return getStateSetEvent(indexOfName(state));
    }

    /**
     * Get an event that will change the state to the indexed state.
     *
     * @param state the state to change to, as an index in the list of state
     * names.
     * @return the event that changes state.
     */
    public EventOutput getStateSetEvent(final int state) {
        if (state < 0 || state >= getNumberOfStates()) {
            throw new IllegalArgumentException("Invalid state ID: " + state);
        }
        return new EventOutput() {
            public void event() {
                if (state == currentState) {
                    return;
                }
                onExit.produce();
                currentState = state;
                onEnter.produce();
            }

            public boolean eventWithRecovery() {
                if (state == currentState) {
                    return false;
                }
                boolean out = onExit.produceWithFailureRecovery();
                currentState = state;
                out |= onEnter.produceWithFailureRecovery();
                return out;
            }
        };
    }

    /**
     * Get the current state.
     *
     * @return the index of the current state.
     */
    public int getState() {
        return currentState;
    }

    /**
     * Get the name of the current state.
     *
     * @return the name of the current state.
     */
    public String getStateName() {
        return stateNames[currentState];
    }

    /**
     * Get the name of the indexed state.
     *
     * @param state the state to look up, as an index in the list of state
     * names.
     * @return the name of the indexed state.
     */
    public String getStateName(int state) {
        if (state < 0 || state >= getNumberOfStates()) {
            throw new IllegalArgumentException("Invalid state ID: " + state);
        }
        return stateNames[state];
    }

    /**
     * Check if the machine is in the named state.
     *
     * @param state the state to check.
     * @return if this machine is in that state.
     */
    public boolean isState(String state) {
        return isState(indexOfName(state));
    }

    /**
     * Check if the machine is in the indexed state.
     *
     * @param state the state to check, as an index in the list of state names.
     * @return if this machine is in that state.
     */
    public boolean isState(int state) {
        if (state < 0 || state >= numberOfStates) {
            throw new IllegalArgumentException("State out of range: " + state);
        }
        return currentState == state;
    }

    /**
     * Return an input representing if the machine is in the named state.
     *
     * @param state the state to check.
     * @return an input for if this machine is in that state.
     */
    public BooleanInput getIsState(String state) {
        return getIsState(indexOfName(state));
    }

    /**
     * Return an input representing if the machine is in the indexed state.
     *
     * @param state the state to check, as an index in the list of state names.
     * @return an input for if this machine is in that state.
     */
    public BooleanInput getIsState(int state) {
        if (state < 0 || state >= getNumberOfStates()) {
            throw new IllegalArgumentException("Invalid state ID: " + state);
        }
        return new DerivedBooleanInput(onEnter) {
            @Override
            protected boolean apply() {
                return currentState == state;
            }
        };
    }

    /**
     * Return an event that moves the machine to the target state if it is in
     * the source state.
     *
     * @param fromState the source state.
     * @param toState the target state.
     * @return the event to conditionally change the machine's state.
     */
    public EventOutput getStateTransitionEvent(String fromState, String toState) {
        return getStateTransitionEvent(indexOfName(fromState), indexOfName(toState));
    }

    /**
     * Return an event that moves the machine to the target state if it is in
     * the source state.
     *
     * @param fromState the source state, as an index in the list of state
     * names.
     * @param toState the target state, as an index in the list of state names.
     * @return the event to conditionally change the machine's state.
     */
    public EventOutput getStateTransitionEvent(int fromState, int toState) {
        return getStateSetEvent(toState).filter(getIsState(fromState));
    }

    /**
     * When the event occurs, move this machine to the target state if it is in
     * the source state.
     *
     * @param fromState the source state.
     * @param toState the target state.
     * @param when when to change state.
     */
    public void transitionStateWhen(String fromState, String toState, EventInput when) {
        transitionStateWhen(indexOfName(fromState), indexOfName(toState), when);
    }

    /**
     * When the event occurs, move this machine to the target state if it is in
     * the source state.
     *
     * @param fromState the source state, as an index in the list of state
     * names.
     * @param toState the target state, as an index in the list of state names.
     * @param when when to change state.
     */
    public void transitionStateWhen(int fromState, int toState, EventInput when) {
        when.send(getStateTransitionEvent(fromState, toState));
    }

    /**
     * Whenever the state changes, log a message constructed from the prefix
     * concatenated with the name of the current state.
     *
     * No space is inserted automatically - include that in the prefix.
     *
     * @param level the logging level at which to log the message.
     * @param prefix the prefix of the message to log.
     */
    public void autologTransitions(final LogLevel level, final String prefix) {
        onEnter.send(new EventOutput() {
            public void event() {
                CCRELogger.logByLevel(logger, level, prefix + getStateName());
            }
        });
    }

    /**
     * Get an event that will fire whenever a new state is entered.
     *
     * @return the event input.
     */
    public EventInput getStateEnterEvent() {
        return onEnter;
    }

    /**
     * Fire output whenever a new state is entered.
     *
     * @param output the event to fire.
     */
    public void onStateEnter(EventOutput output) {
        onEnter.send(output);
    }

    /**
     * Get an event that will fire when the named state is entered.
     *
     * @param state the state to monitor.
     * @return the event input.
     */
    public EventInput onEnterState(String state) {
        return onEnterState(indexOfName(state));
    }

    /**
     * Get an event that will fire when the indexed state is entered.
     *
     * @param state the state to monitor, as an index in the list of state
     * names.
     * @return the event input.
     */
    public EventInput onEnterState(int state) {
        final EventStatus out = new EventStatus();
        onEnterState(state, out);
        return out;
    }

    /**
     * Fire output when the named state is entered.
     *
     * @param state the state to monitor.
     * @param output the event to fire.
     */
    public void onEnterState(String state, final EventOutput output) {
        onEnterState(indexOfName(state), output);
    }

    /**
     * Fire output when the indexed state is entered.
     *
     * @param state the state to monitor, as an index in the list of state
     * names.
     * @param output the event to fire.
     */
    public void onEnterState(int state, final EventOutput output) {
        onEnter.send(output.filter(getIsState(state)));
    }

    /**
     * Get an event that will fire whenever a state is exited, and before the
     * next state is entered.
     *
     * @return the event input.
     */
    public EventInput getStateExitEvent() {
        return onExit;
    }

    /**
     * Fire output whenever a state is exited.
     *
     * @param output the output to fire.
     */
    public void onStateExit(EventOutput output) {
        onExit.send(output);
    }

    /**
     * Get an event that will fire when the named state is exited.
     *
     * @param state the state to monitor.
     * @return the event input.
     */
    public EventInput onExitState(String state) {
        return onExitState(indexOfName(state));
    }

    /**
     * Get an event that will fire when the indexed state is exited.
     *
     * @param state the state to monitor, as an index in the list of state
     * names.
     * @return the event input.
     */
    public EventInput onExitState(int state) {
        final EventStatus out = new EventStatus();
        onExitState(state, out);
        return out;
    }

    /**
     * Fire output when the named state is exited.
     *
     * @param state the state to monitor.
     * @param output the event to fire.
     */
    public void onExitState(String state, final EventOutput output) {
        onExitState(indexOfName(state), output);
    }

    /**
     * Fire output when the indexed state is exited.
     *
     * @param state the state to monitor, as an index in the list of state
     * names.
     * @param output the event to fire.
     */
    public void onExitState(int state, final EventOutput output) {
        onExit.send(output.filter(getIsState(state)));
    }
}
