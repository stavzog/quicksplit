package impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomJsonSerializerTest {

    private Room testRoom;
    private final String roomId = "test-room-123";
    private final String aliceId = "u1-alice-uuid";
    private final String bobId = "u2-bob-uuid";

    @BeforeEach
    void setUp() {
        testRoom = new Room(roomId);
        testRoom.addUser(aliceId, "Alice");
        testRoom.addUser(bobId, "Bob");
    }

    @Test
    void testSerializeAndDeserializeRoom() {
        // alice pays 50 for dinner
        Transaction t1 = new Transaction(
            aliceId,
            50.0,
            "USD",
            50.0,
            1.0,
            "dinner"
        );
        testRoom.logExpense(t1);

        // serialize
        String jsonString = CustomJsonSerializer.serializeRooms(
            Collections.singletonList(testRoom)
        );
        assertNotNull(jsonString);
        assertTrue(jsonString.contains(roomId));
        assertTrue(jsonString.contains("Alice"));
        assertTrue(jsonString.contains("Dinner"));

        // deserialize
        List<Room> deserializedRooms = CustomJsonSerializer.deserializeRooms(
            jsonString
        );
        assertEquals(1, deserializedRooms.size());

        Room loadedRoom = deserializedRooms.get(0);
        assertEquals(roomId, loadedRoom.getRoomId());

        Map<String, String> users = loadedRoom.getUsers();
        assertEquals(2, users.size());
        assertEquals("Alice", users.get(aliceId));
        assertEquals("Bob", users.get(bobId));

        List<Transaction> transactions = loadedRoom.getTransactions();
        assertEquals(1, transactions.size());

        Transaction loadedTx = transactions.get(0);
        assertEquals(t1.getTransactionId(), loadedTx.getTransactionId());
        assertEquals(aliceId, loadedTx.getPayerId());
        assertEquals(50.0, loadedTx.getAmount());
        assertEquals("Dinner", loadedTx.getDescription());
    }

    @Test
    void testSerializationOfMultipleRooms() {
        Room secondRoom = new Room("second-room");
        secondRoom.addUser("u3", "Charlie");

        List<Room> roomList = List.of(testRoom, secondRoom);
        String jsonString = CustomJsonSerializer.serializeRooms(roomList);

        List<Room> deserialized = CustomJsonSerializer.deserializeRooms(
            jsonString
        );
        assertEquals(2, deserialized.size());

        assertTrue(
            deserialized.stream().anyMatch(r -> r.getRoomId().equals(roomId))
        );
        assertTrue(
            deserialized
                .stream()
                .anyMatch(r -> r.getRoomId().equals("second-room"))
        );
    }

    @Test
    void testEmptyRoomSerialization() {
        Room emptyRoom = new Room("empty");
        String jsonString = CustomJsonSerializer.serializeRooms(
            Collections.singletonList(emptyRoom)
        );

        List<Room> result = CustomJsonSerializer.deserializeRooms(jsonString);
        assertEquals(1, result.size());
        assertEquals("empty", result.get(0).getRoomId());
        assertTrue(result.get(0).getUsers().isEmpty());
        assertTrue(result.get(0).getTransactions().isEmpty());
    }

    @Test
    void testMalformedJsonHandling() {
        // mjson throws a runtimeException for bad json
        assertThrows(RuntimeException.class, () -> {
            CustomJsonSerializer.deserializeRooms(
                "{ rooms: [ { invalid json ] }"
            );
        });
    }

    @Test
    void testTransactionIdPersistence() {
        // ensure that transaction ids are persistent
        Transaction t = new Transaction(
            aliceId,
            10.0,
            "USD",
            10.0,
            1.0,
            "Coffee"
        );
        String originalId = t.getTransactionId();
        testRoom.logExpense(t);

        String json = CustomJsonSerializer.serializeRooms(
            Collections.singletonList(testRoom)
        );
        Room loaded = CustomJsonSerializer.deserializeRooms(json).get(0);

        assertEquals(
            originalId,
            loaded.getTransactions().get(0).getTransactionId()
        );
    }

    @Test
    void testRoomMergeLogic() {
        // create update room with a new transaction and a new user
        Room updateRoom = new Room(roomId);
        updateRoom.addUser(aliceId, "Alice");
        updateRoom.addUser("u3-charlie", "Charlie");

        Transaction existingTx = new Transaction(
            aliceId,
            20.0,
            "USD",
            20.0,
            1.0,
            "Old"
        );
        testRoom.logExpense(existingTx);

        Transaction newTx = new Transaction(
            "u3-charlie",
            30.0,
            "USD",
            30.0,
            1.0,
            "New"
        );
        updateRoom.logExpense(existingTx); // duplicate
        updateRoom.logExpense(newTx); // new

        // merge
        testRoom.merge(updateRoom);

        // check merged users
        assertEquals(3, testRoom.getUsers().size());
        assertEquals("Charlie", testRoom.getUsers().get("u3-charlie"));

        // check merged transactions
        assertEquals(2, testRoom.getTransactions().size());
        assertTrue(
            testRoom
                .getTransactions()
                .stream()
                .anyMatch(t -> t.getDescription().equals("New"))
        );
    }
}
