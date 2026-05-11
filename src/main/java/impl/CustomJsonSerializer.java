package impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import mjson.Json;

/**
 * Custom JSON Serializer to handle the simplified room schema using mjson.
 * This version uses String-based Key-Value pairs for users (UUIDs) to ensure
 * data integrity during synchronization across distributed clients.
 */
public class CustomJsonSerializer {

    /**
     * Serializes a collection of rooms into a JSON string.
     */
    public static String serializeRooms(Collection<Room> rooms) {
        Json roomsArray = Json.array();
        for (Room room : rooms) {
            roomsArray.add(serializeRoom(room));
        }
        Json root = Json.object().set("rooms", roomsArray);
        return root.toString();
    }

    /**
     * Serializes a single room into a JSON object.
     * Uses an object mapping for users: { "uuid-string": "Name" }
     */
    private static Json serializeRoom(Room room) {
        Json usersObject = Json.object();
        for (Map.Entry<String, String> entry : room.getUsers().entrySet()) {
            usersObject.set(entry.getKey(), entry.getValue());
        }

        Json transactionsArray = Json.array();
        for (Transaction t : room.getTransactions()) {
            Json tJson = Json.object()
                .set("userId", t.getPayerId())
                .set("amount", t.getAmount())
                .set("originalCurrency", t.getOriginalCurrency())
                .set("originalAmount", t.getOriginalAmount())
                .set("exchangeRate", t.getExchangeRate())
                .set("description", t.getDescription());
            transactionsArray.add(tJson);
        }

        return Json.object()
            .set("roomId", room.getRoomId())
            .set("users", usersObject)
            .set("transactions", transactionsArray);
    }

    /**
     * Deserializes a JSON string into a list of Room objects.
     */
    public static List<Room> deserializeRooms(String jsonString) {
        Json root = Json.read(jsonString);
        Json roomsArray = root.at("rooms");
        List<Room> result = new ArrayList<>();

        if (roomsArray == null || !roomsArray.isArray()) {
            return result;
        }

        for (Json roomJson : roomsArray.asJsonList()) {
            result.add(deserializeRoom(roomJson));
        }
        return result;
    }

    /**
     * Deserializes a single room JSON object into a Room instance.
     */
    private static Room deserializeRoom(Json roomJson) {
        String roomId = roomJson.at("roomId").asString();
        Room room = new Room(roomId);

        // Deserialize Users Map (Keys are String UUIDs)
        Json usersJson = roomJson.at("users");
        if (usersJson != null && usersJson.isObject()) {
            for (Map.Entry<String, Json> entry : usersJson
                .asJsonMap()
                .entrySet()) {
                room.addUser(entry.getKey(), entry.getValue().asString());
            }
        }

        // Deserialize Transactions
        Json transactionsArray = roomJson.at("transactions");
        if (transactionsArray != null && transactionsArray.isArray()) {
            for (Json tJson : transactionsArray.asJsonList()) {
                Transaction t = new Transaction(
                    tJson.at("userId").asString(),
                    tJson.at("amount").asDouble(),
                    tJson.at("originalCurrency").asString(),
                    tJson.at("originalAmount").asDouble(),
                    tJson.at("exchangeRate").asDouble(),
                    tJson.at("description").asString()
                );
                room.logExpense(t);
            }
        }

        return room;
    }
}
