package ccre.saver;

/**
 * A fake storage provider that works, except that it doesn't save or load
 * anything to disk. This is used if a proper provider cannot be found.
 *
 * @author skeggsc
 */
public class FakeStorageProvider extends StorageProvider {

    @Override
    protected StorageSegment open(String name) {
        return new HashMappedStorageSegment() {
            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
    }
}
