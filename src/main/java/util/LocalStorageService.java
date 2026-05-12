package util;

import impl.CustomJsonSerializer;
import impl.Room;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service for handling local file-based persistence.
 * Implements the StorageService interface to provide a unified data boundary.
 */
public class LocalStorageService implements StorageService {

    private String filename;

    @Override
    public void setTarget(String target) {
        if (target != null && !target.endsWith(".json")) {
            this.filename = target + ".json";
        } else {
            this.filename = target;
        }
    }

    @Override
    public Collection<Room> save(Collection<Room> rooms) throws Exception {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalStateException("Storage target (filename) is not set.");
        }

        String json = CustomJsonSerializer.serializeRooms(rooms);
        Files.writeString(Paths.get(filename), json);

        // For local storage, the state doesn't change after saving, so we just return the input
        return rooms;
    }

    @Override
    public Collection<Room> load() throws Exception {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalStateException("Storage target (filename) is not set.");
        }

        Path path = Paths.get(filename);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        String json = Files.readString(path);
        return CustomJsonSerializer.deserializeRooms(json);
    }

    @Override
    public Room fetchRoom(String roomId) throws Exception {
        Collection<Room> allRooms = load();
        for (Room room : allRooms) {
            if (room.getRoomId().equals(roomId)) {
                return room;
            }
        }
        return null; // Room not found in this file
    }
}
