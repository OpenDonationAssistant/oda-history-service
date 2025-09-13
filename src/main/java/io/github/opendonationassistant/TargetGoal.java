package io.github.opendonationassistant;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TargetGoal(String goalId, String goalTitle) {}
