package io.github.opendonationassistant.goal;

import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("goal")
public record GoalData(String id, String title) {}
