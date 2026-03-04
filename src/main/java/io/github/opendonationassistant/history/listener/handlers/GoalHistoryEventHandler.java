package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.events.MessageHandler;
import io.github.opendonationassistant.events.history.event.GoalHistoryEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData.TargetGoal;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import java.io.IOException;

public class GoalHistoryEventHandler implements MessageHandler {

  private final ObjectMapper objectMapper;
  private final HistoryItemRepository repository;

  @Inject
  public GoalHistoryEventHandler(
    ObjectMapper objectMapper,
    HistoryItemRepository repository
  ) {
    this.objectMapper = objectMapper;
    this.repository = repository;
  }

  @Override
  public void handle(byte[] message) throws IOException {
    final var event = objectMapper.readValue(message, GoalHistoryEvent.class);
    if (event == null) {
      return;
    }
    repository
      .findByOriginId(event.originId())
      .ifPresent(historyItem ->
        historyItem.addGoal(new TargetGoal(event.goalId(), event.title()))
      );
  }

  @Override
  public String type() {
    return "GoalHistoryEvent";
  }
}
