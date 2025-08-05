package io.github.opendonationassistant;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record HistoryCommand(
  String type,
  HistoryItemData partial,
  boolean triggerAlert,
  boolean triggerReel,
  boolean addToTop,
  boolean addToGoal
) {}
