package com.log.generator.utils;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomDataGenerator {

    private RandomDataGenerator() {
    }

    public static String generateRandomIP() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.nextInt(1, 256)
            + "."
            + random.nextInt(0, 256)
            + "."
            + random.nextInt(0, 256)
            + "."
            + random.nextInt(1, 256);
    }

    public static int getRandomStatusCode() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int[] statusCodes = {200, 201, 400, 401, 403, 404, 500, 502, 503};
        return statusCodes[random.nextInt(statusCodes.length)];
    }

    public static String getRandomSeverity() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String[] severities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        return severities[random.nextInt(severities.length)];
    }

    public static String getRandomFileType() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String[] fileTypes = {"PDF", "CSV", "JSON", "XML", "TXT", "LOG", "ZIP"};
        return fileTypes[random.nextInt(fileTypes.length)];
    }

    public static String generateRandomId(int length) {
        return java.util.UUID.randomUUID().toString().substring(0, Math.min(length, 36));
    }

    public static String generateRequestId() {
        return generateRandomId(8);
    }

    public static String generateThreadId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "thread-" + random.nextInt(1, 21);
    }

    public static String generateInstanceId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "i-" + Long.toHexString(random.nextLong()).substring(0, 8);
    }

    public static String generateDatacenter() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "dc-" + (char) ('a' + random.nextInt(0, 4));
    }

    public static String generateServiceVersion() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "2.1." + random.nextInt(0, 10);
    }

    public static String generateEnvironment() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.nextBoolean() ? "production" : "staging";
    }

    public static int generatePort() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return 8080 + random.nextInt(0, 100);
    }
}
