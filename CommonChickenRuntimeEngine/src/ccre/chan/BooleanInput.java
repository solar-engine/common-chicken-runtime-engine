package ccre.chan;

/**
 * A BooleanInput is a way to get the current state of a boolean input, and to
 * subscribe to notifications of changes in the boolean input's value.
 * BooleanInput is the combination of BooleanInputPoll and BooleanInputProducer.
 *
 * @see BooleanInputPoll
 * @see BooleanInputProducer
 * @author skeggsc
 */
public interface BooleanInput extends BooleanInputPoll, BooleanInputProducer {
}
