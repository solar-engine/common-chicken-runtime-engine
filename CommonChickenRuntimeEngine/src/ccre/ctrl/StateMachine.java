package ccre.ctrl;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanInputPoll;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.channel.FloatOutput;
import ccre.log.LogLevel;
import ccre.log.Logger;

public class StateMachine {
    private int currentState;
    public final int numberOfStates;
    private final EventStatus onExit = new EventStatus();
    private final EventStatus onEnter = new EventStatus();
    private final String[] stateNames;
    
    public StateMachine(String defaultState, String... names) {
        checkNamesConsistency(names);
        this.stateNames = names;
        numberOfStates = names.length;
        setState(defaultState); 
    }
    
    public StateMachine(int defaultState, String... names) {
        checkNamesConsistency(names);
        this.stateNames = names;
        numberOfStates = names.length;
        setState(defaultState);
    }

    private static void checkNamesConsistency(String... names) {
        for (int i=0; i<names.length; i++) {
            String name = names[i];
            if (name == null) {
                throw new NullPointerException();
            }
            for (int j=i+1; j<names.length; j++) {
                if (name.equals(names[j])) {
                    throw new IllegalArgumentException("Duplicate state name: " + names[i]);
                }
            }
        }
    }
    
    private int indexOfName(String state) {
        for (int i=0; i < numberOfStates; i++) {
            if (state.equals(stateNames[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("State name not found: " + state);
    }
    
    public void setState(String state) {
        setState(indexOfName(state));
    }

    public void setState(int state) {
        if (state < 0 || state >= numberOfStates) {
            throw new IllegalArgumentException("Invalid state ID: " + state);
        }
        if (state == currentState) {
            return;
        }
        onExit.produce();
        currentState = state;
        onEnter.produce();
    }
    
    public void setStateWhen(String state, EventInput when) {
        setStateWhen(indexOfName(state), when);
    }
    
    public void setStateWhen(int state, EventInput when) {
        when.send(getStateSetEvent(state));
    }

    public EventOutput getStateSetEvent(String state) {
        return getStateSetEvent(indexOfName(state));
    }
    
    public EventOutput getStateSetEvent(final int state) {
        if (state < 0 || state >= numberOfStates) {
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
        };
    }
    
    public int getState() {
        return currentState;
    }
    
    public String getStateName() {
        return stateNames[currentState];
    }
    
    public String getStateName(int state) {
        if (state < 0 || state >= numberOfStates) {
            throw new IllegalArgumentException("Invalid state ID: " + state);
        }
        return stateNames[state];
    }
    
    public boolean isState(String state) {
        return isState(indexOfName(state));
    }
    
    public boolean isState(int state) {
        return currentState == state;
    }

    public BooleanInputPoll getIsState(String state) {
        return getIsState(indexOfName(state));
    }
    
    public BooleanInputPoll getIsState(final int state) {
        if (state < 0 || state >= numberOfStates) {
            throw new IllegalArgumentException("Invalid state ID: " + state);
        }
        return new BooleanInputPoll() {
            public boolean get() {
                return currentState == state;
            }
        };
    }
    
    public BooleanInput getIsStateDyn(String state) {
        return getIsStateDyn(indexOfName(state));
    }
    
    public BooleanInput getIsStateDyn(int state) {
        return BooleanMixing.createDispatch(getIsState(state), onEnter);
    }
    
    public EventOutput getStateTransitionEvent(String fromState, String toState) {
        return getStateTransitionEvent(indexOfName(fromState), indexOfName(toState));
    }
    
    public EventOutput getStateTransitionEvent(int fromState, int toState) {
        return EventMixing.filterEvent(getIsState(fromState), true, getStateSetEvent(toState));
    }
    
    public void transitionStateWhen(String fromState, String toState, EventInput when) {
        transitionStateWhen(indexOfName(fromState), indexOfName(toState), when);
    }
    
    public void transitionStateWhen(int fromState, int toState, EventInput when) {
        when.send(getStateTransitionEvent(fromState, toState));
    }
    
    public void autologTransitions(final LogLevel level, final String prefix) {
        onEnter.send(new EventOutput() {
            public void event() {
                Logger.log(level, prefix + getStateName());
            }
        });
    }
    
    public EventInput getStateEnterEvent() {
        return onEnter;
    }
    
    public void onStateEnter(EventOutput output) {
        onEnter.send(output);
    }

    public EventInput onEnterState(String state) {
        return onEnterState(indexOfName(state));
    }
    
    public EventInput onEnterState(int state) {
        final EventStatus out = new EventStatus();
        onEnterState(state, out);
        return out;
    }
    
    public void onEnterState(String state, final EventOutput output) {
        onEnterState(indexOfName(state), output);
    }
    
    public void onEnterState(int state, final EventOutput output) {
        onEnter.send(EventMixing.filterEvent(getIsState(state), true, output));
    }
    
    public void setOnEnterState(String state, BooleanOutput output, boolean value) {
        setOnEnterState(indexOfName(state), output, value);
    }
    
    public void setOnEnterState(int state, BooleanOutput output, boolean value) {
        onEnterState(state, BooleanMixing.getSetEvent(output, value));
    }

    public void setTrueOnEnterState(String state, BooleanOutput output) {
        setTrueOnEnterState(indexOfName(state), output);
    }
    
    public void setTrueOnEnterState(int state, BooleanOutput output) {
        setOnEnterState(state, output, true);
    }

    public void setFalseOnEnterState(String state, BooleanOutput output) {
        setFalseOnEnterState(indexOfName(state), output);
    }
    
    public void setFalseOnEnterState(int state, BooleanOutput output) {
        setOnEnterState(state, output, false);
    }

    public void setOnEnterState(String state, FloatOutput output, float value) {
        setOnEnterState(indexOfName(state), output, value);
    }
    
    public void setOnEnterState(int state, FloatOutput output, float value) {
        onEnterState(state, FloatMixing.getSetEvent(output, value));
    }
    
    public EventInput getStateExitEvent() {
        return onExit;
    }
    
    public void onStateExit(EventOutput output) {
        onExit.send(output);
    }

    public EventInput onExitState(String state) {
        return onExitState(indexOfName(state));
    }
    
    public EventInput onExitState(int state) {
        final EventStatus out = new EventStatus();
        onExitState(state, out);
        return out;
    }
    
    public void onExitState(String state, final EventOutput output) {
        onExitState(indexOfName(state), output);
    }
    
    public void onExitState(int state, final EventOutput output) {
        onExit.send(EventMixing.filterEvent(getIsState(state), true, output));
    }

    public void setOnExitState(String state, BooleanOutput output, boolean value) {
        setOnExitState(indexOfName(state), output, value);
    }

    public void setOnExitState(int state, BooleanOutput output, boolean value) {
        onExitState(state, BooleanMixing.getSetEvent(output, value));
    }
    
    public void setTrueOnExitState(String state, BooleanOutput output) {
        setTrueOnExitState(indexOfName(state), output);
    }
    
    public void setTrueOnExitState(int state, BooleanOutput output) {
        setOnExitState(state, output, true);
    }

    public void setFalseOnExitState(String state, BooleanOutput output) {
        setFalseOnExitState(indexOfName(state), output);
    }
    
    public void setFalseOnExitState(int state, BooleanOutput output) {
        setOnExitState(state, output, false);
    }

    public void setOnExitState(String state, FloatOutput output, float value) {
        setOnExitState(indexOfName(state), output, value);
    }
    
    public void setOnExitState(int state, FloatOutput output, float value) {
        onExitState(state, FloatMixing.getSetEvent(output, value));
    }
}
