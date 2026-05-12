package util;

import impl.CustomJsonSerializer;
import impl.Room;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for communicating with the JSONBin.io API.
 * Handles the low-level HTTP networking required to push and pull room data,
 * and encapsulates the cloud-specific merge logic.
 */
public class CloudStorageService implements StorageService {

    private static final String API_BASE_URL = "https://api.jsonbin.io/v3/b/";
    private static final String ACCESS_KEY =
        "$2a$10$Dv6aBszMHyEwenDeGfsh7.LbtIiKOOD4hb5ZRUs2pVlqMkcGdPTLO";
    private static final String DEFAULT_BIN_ID = "6a026cf6250b1311c3379279";

    private final HttpClient httpClient;
    private String binId;

    public CloudStorageService() {
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.binId = DEFAULT_BIN_ID;
    }

    @Override
    public void setTarget(String target) {
        if (target != null && !target.trim().isEmpty()) {
            this.binId = target;
        } else {
            this.binId = DEFAULT_BIN_ID;
        }
    }

    @Override
    public Collection<Room> save(Collection<Room> localRooms) throws Exception {
        Collection<Room> cloudRooms = load();

        Map<String, Room> mergedMap = new HashMap<>();
        for (Room cloudRoom : cloudRooms) {
            mergedMap.put(cloudRoom.getRoomId(), cloudRoom);
        }

        // merge local rooms with cloud rooms
        for (Room localRoom : localRooms) {
            if (mergedMap.containsKey(localRoom.getRoomId())) {
                mergedMap.get(localRoom.getRoomId()).merge(localRoom);
            } else {
                mergedMap.put(localRoom.getRoomId(), localRoom);
            }
        }

        Collection<Room> mergedRooms = mergedMap.values();

        String jsonToPush = CustomJsonSerializer.serializeRooms(mergedRooms);

        // push to cloud
        updateBinString(jsonToPush);

        return mergedRooms;
    }

    @Override
    public Collection<Room> load() throws Exception {
        String json = fetchBinString();
        return CustomJsonSerializer.deserializeRooms(json);
    }

    @Override
    public Room fetchRoom(String roomId) throws Exception {
        // JSONPath to filter the rooms array where roomId matches the requested ID
        String jsonPath = "$.rooms[?(@.roomId == '" + roomId + "')]";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + binId + "/latest"))
            .header("X-Access-Key", ACCESS_KEY)
            .header("X-Bin-Meta", "false")
            .header("X-JSON-Path", jsonPath)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Failed to fetch cloud room data. HTTP Status: " +
                    response.statusCode() +
                    " - " +
                    response.body()
            );
        }

        String jsonResponse = response.body();
        if (
            jsonResponse == null ||
            jsonResponse.trim().isEmpty() ||
            jsonResponse.trim().equals("[]")
        ) {
            return null; // Room not found
        }

        // The JSONPath query returns an array of matches.
        // We wrap it in the expected "rooms" object format for the importer.
        String formattedJson = "{\"rooms\":" + jsonResponse + "}";
        List<Room> parsedRooms = CustomJsonSerializer.deserializeRooms(
            formattedJson
        );

        if (parsedRooms != null && !parsedRooms.isEmpty()) {
            return parsedRooms.get(0);
        }

        return null;
    }

    /**
     * Helper method to fetch the raw JSON from the current bin.
     */
    private String fetchBinString() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + binId + "/latest"))
            .header("X-Access-Key", ACCESS_KEY)
            .header("X-Bin-Meta", "false") // Returns only the data, not metadata
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Failed to fetch cloud data. HTTP Status: " +
                    response.statusCode() +
                    " - " +
                    response.body()
            );
        }

        String body = response.body();
        if (
            body == null ||
            body.trim().isEmpty() ||
            body.trim().equals("[]") ||
            body.trim().equals("{}")
        ) {
            return "{\"rooms\": []}";
        }

        return body;
    }

    /**
     * Helper method to update the current bin with raw JSON.
     */
    private void updateBinString(String jsonContent) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_BASE_URL + binId))
            .header("X-Access-Key", ACCESS_KEY)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(jsonContent))
            .build();

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Failed to update cloud data. HTTP Status: " +
                    response.statusCode() +
                    " - " +
                    response.body()
            );
        }
    }
}
