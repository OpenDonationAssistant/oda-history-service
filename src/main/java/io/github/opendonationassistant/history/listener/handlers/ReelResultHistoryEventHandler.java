package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.history.event.ReelResultHistoryEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData.ReelResult;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class ReelResultHistoryEventHandler
  extends AbstractMessageHandler<ReelResultHistoryEvent> {

  private final HistoryItemRepository repository;

  @Inject
  public ReelResultHistoryEventHandler(
    ObjectMapper objectMapper,
    HistoryItemRepository repository
  ) {
    super(objectMapper);
    this.repository = repository;
  }

  @Override
  public void handle(ReelResultHistoryEvent event) throws IOException {
    Optional.ofNullable(event.originId())
      .flatMap(repository::findByOriginId)
      .ifPresent(historyItem ->
        historyItem.addReelResult(new ReelResult(event.title()))
      );
  }
}
