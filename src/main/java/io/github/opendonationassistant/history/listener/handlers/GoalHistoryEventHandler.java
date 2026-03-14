package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.history.event.GoalHistoryEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData.TargetGoal;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class GoalHistoryEventHandler
  extends AbstractMessageHandler<GoalHistoryEvent> {

  private final HistoryItemRepository repository;

  @Inject
  public GoalHistoryEventHandler(
    ObjectMapper objectMapper,
    HistoryItemRepository repository
  ) {
    super(objectMapper);
    this.repository = repository;
  }

  @Override
  public void handle(GoalHistoryEvent event) throws IOException {
    Optional.ofNullable(event.originId())
      .flatMap(repository::findByOriginId)
      .ifPresent(historyItem ->
        historyItem.addGoal(new TargetGoal(event.goalId(), event.title()))
      );
  }
}
