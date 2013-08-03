package ccre.chan;

/**
 * A FloatOutput is an interface for anything that can be set to an analog
 * value.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @see FloatInput
 * @author skeggsc
 */
public interface FloatOutput {

    /**
     * Set the float value of this output.
     *
     * By convention, most float inputs and outputs have states that range from
     * -1.0f to 1.0f.
     *
     * @param value The new value to send to this output.
     */
    public void writeValue(float value);
}
