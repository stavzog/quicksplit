package util;

import java.util.Random;

/**
 * Utility class for generating human-readable room IDs.
 * Generates strings in the format "adjective-noun-number" (e.g., "speedy-badger-42").
 */
public class IDGenerator {

    private static final String[] ADJECTIVES = {
        "speedy", "clever", "mighty", "brave", "happy", "swift",
        "bright", "gentle", "quick", "bold", "steady", "sharp",
        "approved", "dancing", "sleepy", "golden", "silent"
    };

    private static final String[] NOUNS = {
        "badger", "eagle", "otter", "falcon", "panda", "tiger",
        "wolf", "fox", "deer", "lynx", "owl", "buggle",
        "rocket", "koala", "dolphin", "spirit", "cloud"
    };

    private static final Random RANDOM = new Random();

    /**
     * Generates a random, human-friendly ID.
     * @return a string like "speedy-badger-42"
     */
    public static String generate() {
        String adj = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        int num = RANDOM.nextInt(99) + 1;
        return String.format("%s-%s-%d", adj, noun, num);
    }
}
