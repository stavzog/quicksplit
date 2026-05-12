package util;

import impl.Room;
import java.util.Collection;

/**
 * Interface to abstract data persistence operations.
 * Implementations (e.g., LocalStorageService, CloudStorageService)
 * handle the specific logic for where data comes from and where it goes.
 */
public interface StorageService {

    /**
     * Configures the target destination/source for this storage service.
     * For local storage, this could be a filename. For cloud storage, a Bin ID.
     *
     * @param target Identifier for the storage location.
     */
    void setTarget(String target);

    /**
     * Saves a collection of rooms to the underlying storage.
     * For distributed storage, this should handle safe merging to prevent data loss.
     *
     * @param rooms The current state of rooms in local memory.
     * @return The synchronized state of rooms after saving (may include newly merged data).
     * @throws Exception if the save operation encounters an error.
     */
    Collection<Room> save(Collection<Room> rooms) throws Exception;

    /**
     * Loads all rooms from the underlying storage.
     *
     * @return A collection of loaded rooms.
     * @throws Exception if the load operation encounters an error.
     */
    Collection<Room> load() throws Exception;

    /**
     * Fetches a specific room by its ID, potentially bypassing a full vault load.
     *
     * @param roomId The unique identifier of the room to fetch.
     * @return The requested Room, or null if it cannot be found.
     * @throws Exception if the fetch operation encounters an error.
     */
    Room fetchRoom(String roomId) throws Exception;
}
