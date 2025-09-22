package com.log.generator.model;

public enum LogScenario {
    USER_LOGIN("User authentication and login events"),
    DATABASE_OPERATION("Database queries, connections, and transactions"),
    API_REQUEST("HTTP API requests and responses"),
    ERROR_HANDLING("Application errors and exceptions"),
    SECURITY_EVENT("Security-related events and alerts"),
    PERFORMANCE_METRIC("Performance monitoring and metrics"),
    SYSTEM_STARTUP("Application startup and initialization"),
    CACHE_OPERATION("Cache hits, misses, and operations"),
    FILE_OPERATION("File system operations and I/O"),
    BUSINESS_LOGIC("Business process execution and workflows");

    private final String description;

    LogScenario(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
