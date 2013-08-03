package ccre.chan;

/**
 * A FloatInputPoll is a way to get the current state of a float input.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @author skeggsc
 */
public interface FloatInputPoll {

    /**
     * Get the current state of this float input.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @return The current value.
     */
    public float readValue();
}
