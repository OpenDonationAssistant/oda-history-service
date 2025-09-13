package io.github.opendonationassistant.goal;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("goal")
public record GoalData(
  @Id String id,
  @Nullable String title,
  Boolean isDefault
) {}
