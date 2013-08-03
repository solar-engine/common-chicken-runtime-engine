package ccre.chan;

/**
 * A BooleanOutput is an interface for anything that can be turned on or off. It
 * can be set to true or false.
 *
 * @see BooleanInput
 * @author skeggsc
 */
public interface BooleanOutput {

    /**
     * Set the boolean value of this output. In other words, turn it on or off.
     *
     * @param value The new value to send to this output.
     */
    public void writeValue(boolean value);
}
