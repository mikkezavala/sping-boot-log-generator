package com.log.generator.model;

public enum LogLevel {
    TRACE(0.05),
    DEBUG(0.15),
    INFO(0.30),
    WARN(0.15),
    ERROR(0.35);

    private final double probability;

    LogLevel(double probability) {
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }
}
