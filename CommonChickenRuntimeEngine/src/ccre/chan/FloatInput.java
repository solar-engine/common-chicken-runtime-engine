package ccre.chan;

/**
 * A FloatInput is a way to get the current state of a float input, and to
 * subscribe to notifications of changes in the float input's value. FloatInput
 * is the combination of FloatInputPoll and FloatInputProducer.
 *
 * By convention, most float inputs and outputs have states that range from
 * -1.0f to 1.0f.
 *
 * @see FloatInputPoll
 * @see FloatInputProducer
 * @author skeggsc
 */
public interface FloatInput extends FloatInputPoll, FloatInputProducer {
}
