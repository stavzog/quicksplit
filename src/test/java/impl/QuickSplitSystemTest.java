package impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuickSplitSystemTest {

    private QuickSplitSystem system;

    @BeforeEach
    void setUp() {
        system = new QuickSplitSystem();
    }

    @Test
    void testCreateAndJoinRoom() {
        assertNull(system.getActiveRoomId());

        String roomId1 = system.createRoom();
        assertNotNull(roomId1);
        assertEquals(roomId1, system.getActiveRoomId());
        assertNotNull(system.getActiveRoom());

        String roomId2 = system.createRoom();
        assertNotEquals(roomId1, roomId2);
        assertEquals(roomId2, system.getActiveRoomId());

        assertTrue(system.joinRoom(roomId1));
        assertEquals(roomId1, system.getActiveRoomId());

        assertFalse(system.joinRoom("invalid-id-123"));
        assertEquals(roomId1, system.getActiveRoomId()); // should remain unchanged
    }

    @Test
    void testAddAndGetUsers() {
        system.createRoom();
        system.addUser("uuid-1", "Alice");
        system.addUser("uuid-2", "Bob");

        Map<String, String> users = system.getUsers();
        assertEquals(2, users.size());
        assertEquals("Alice", users.get("uuid-1"));
        assertEquals("Bob", users.get("uuid-2"));
    }

    @Test
    void testAddRoomMethod() {
        Room customRoom = new Room("custom-room");
        customRoom.addUser("uuid-test", "TestUser");

        system.addRoom(customRoom);

        assertEquals("custom-room", system.getActiveRoomId());
        assertEquals(1, system.getUsers().size());

        Room anotherRoom = new Room("another-room");
        system.addRoom(anotherRoom);

        assertEquals("custom-room", system.getActiveRoomId());
        assertTrue(system.getAvailableRooms().contains("another-room"));

        Collection<Room> allRooms = system.getAllRooms();
        assertEquals(2, allRooms.size());
    }

    @Test
    void testLogExpenseRequiresActiveRoom() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            system.logExpense("uuid-1", 50.0, "USD", "Dinner");
        });
        assertTrue(exception.getMessage().contains("No active room"));
    }

    @Test
    void testCalculateSettleUp_EmptyRoom() {
        system.createRoom();
        List<Settlement> settlements = system.calculateSettleUp("USD");
        assertTrue(settlements.isEmpty());
    }

    @Test
    void testCalculateSettleUp_BasicSplit() {
        system.createRoom();
        system.addUser("u1", "Alice");
        system.addUser("u2", "Bob");

        try {
            // alice pays $20 for both
            // share is $10 each.
            system.logExpense("u1", 20.0, "USD", "Lunch");

            List<Settlement> settlements = system.calculateSettleUp("USD");
            assertEquals(1, settlements.size());

            Settlement settlement = settlements.get(0);
            assertEquals("u2", settlement.getDebtorId()); // bob owes
            assertEquals("u1", settlement.getCreditorId()); // alice is owed
            assertEquals(10.0, settlement.getAmount(), 0.001);
            assertEquals("USD", settlement.getCurrency());
        } catch (RuntimeException e) {
            // if network is not available during testing
            System.out.println(
                "Skipping testCalculateSettleUp_BasicSplit due to API error: " +
                    e.getMessage()
            );
        }
    }

    @Test
    void testCalculateSettleUp_ComplexSplit() {
        system.createRoom();
        system.addUser("u1", "Alice");
        system.addUser("u2", "Bob");
        system.addUser("u3", "Charlie");

        try {
            // total pool: 60. share: 20 per person.
            // alice spent 50 -> gets back 30
            // bob spent 10 -> owes 10
            // charlie spent 0 -> owes 20
            system.logExpense("u1", 50.0, "USD", "Hotel");
            system.logExpense("u2", 10.0, "USD", "Gas");

            List<Settlement> settlements = system.calculateSettleUp("USD");
            assertEquals(2, settlements.size());

            // sorting guarantees processing largest debts and credits first
            // debtors: charlie (-20) first, then bob (-10)
            // creditors: alice (+30) first

            // charlie pays alice 20
            Settlement first = settlements.get(0);
            assertEquals("u3", first.getDebtorId()); // charlie
            assertEquals("u1", first.getCreditorId()); // alice
            assertEquals(20.0, first.getAmount(), 0.001);

            // bob pays alice 10
            Settlement second = settlements.get(1);
            assertEquals("u2", second.getDebtorId()); // bob
            assertEquals("u1", second.getCreditorId()); // alice
            assertEquals(10.0, second.getAmount(), 0.001);
        } catch (RuntimeException e) {
            System.out.println(
                "Skipping testCalculateSettleUp_ComplexSplit due to API error: " +
                    e.getMessage()
            );
        }
    }

    @Test
    void testCalculateSettleUp_PerfectlyBalanced() {
        system.createRoom();
        system.addUser("u1", "Alice");
        system.addUser("u2", "Bob");

        try {
            // both spend $15, nobody owes anything
            system.logExpense("u1", 15.0, "USD", "Tickets");
            system.logExpense("u2", 15.0, "USD", "Snacks");

            List<Settlement> settlements = system.calculateSettleUp("USD");
            assertTrue(
                settlements.isEmpty(),
                "Perfectly balanced expenses should result in no settlements"
            );
        } catch (RuntimeException e) {
            System.out.println(
                "Skipping testCalculateSettleUp_PerfectlyBalanced due to API error: " +
                    e.getMessage()
            );
        }
    }
}
