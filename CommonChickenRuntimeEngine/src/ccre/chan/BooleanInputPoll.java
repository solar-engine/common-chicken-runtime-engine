package ccre.chan;

/**
 * A BooleanInputPoll is a way to get the current state of a boolean input.
 *
 * @author skeggsc
 */
public interface BooleanInputPoll {

    /**
     * Get the current state of this boolean input.
     *
     * @return The current value.
     */
    public boolean readValue();
}
