package ccre.channel;

public interface CancelOutput {
    public static final CancelOutput nothing = () -> {
    };

    public void cancel();

    public default CancelOutput combine(CancelOutput other) {
        return () -> {
            cancel();
            other.cancel();
        };
    }
}
