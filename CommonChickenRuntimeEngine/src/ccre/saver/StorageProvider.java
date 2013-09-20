package ccre.saver;

import ccre.log.LogLevel;
import ccre.log.Logger;

/**
 * The holder for the current storage provider, and the superclass for any
 * storage providers.
 *
 * @author skeggsc
 */
public abstract class StorageProvider {

    /**
     * The active storage provider.
     */
    protected static StorageProvider provider;

    /**
     * If a provider is not yet registered, register the default provider.
     */
    public static void initProvider() {
        if (provider == null) {
            try {
                provider = (StorageProvider) Class.forName("ccre.saver.DefaultStorageProvider").newInstance();
            } catch (InstantiationException ex) {
                provider = new FakeStorageProvider();
                Logger.log(LogLevel.WARNING, "No throwable printing provider!", ex);
            } catch (IllegalAccessException ex) {
                provider = new FakeStorageProvider();
                Logger.log(LogLevel.WARNING, "No throwable printing provider!", ex);
            } catch (ClassNotFoundException ex) {
                provider = new FakeStorageProvider();
                Logger.log(LogLevel.WARNING, "No throwable printing provider!", ex);
            }
        }
    }

    /**
     * Open a StorageSegment for the specified name. The name of a
     * StorageSegment must contain only letters, numbers, currency symbols, and
     * underscores.
     *
     * @param name the name of the storage segment.
     * @return the StorageSegment that has been opened.
     */
    public static StorageSegment openStorage(String name) {
        initProvider();
        for (char c : name.toCharArray()) {
            if (!(Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isDigit(c) || c == '$' || c == '_')) {
                throw new IllegalArgumentException("Storage names must only contain 'a-zA-Z0-9$_'");
            }
        }
        return provider.open(name);
    }

    /**
     * Open a StorageSegment under the specific name. The name of a
     * StorageSegment must contain only letters, numbers, currency symbols, and
     * underscores.
     *
     * @param name the name of the storage segment.
     * @return the StorageSegment that has been opened.
     */
    protected abstract StorageSegment open(String name);
}
