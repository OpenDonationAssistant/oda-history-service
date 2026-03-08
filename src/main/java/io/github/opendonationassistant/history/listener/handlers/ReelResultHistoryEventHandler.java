package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.events.MessageHandler;
import io.github.opendonationassistant.events.history.event.ReelResultHistoryEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData.ReelResult;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import java.io.IOException;

public class ReelResultHistoryEventHandler implements MessageHandler {

  private final ObjectMapper objectMapper;
  private final HistoryItemRepository repository;

  @Inject
  public ReelResultHistoryEventHandler(
    ObjectMapper objectMapper,
    HistoryItemRepository repository
  ) {
    this.objectMapper = objectMapper;
    this.repository = repository;
  }

  @Override
  public void handle(byte[] message) throws IOException {
    final var event = objectMapper.readValue(
      message,
      ReelResultHistoryEvent.class
    );
    if (event == null ||  event.originId() == null) {
      return;
    }
    repository
      .findByOriginId(event.originId())
      .ifPresent(historyItem ->
        historyItem.addReelResult(new ReelResult(event.title()))
      );
  }

  @Override
  public String type() {
    return "ReelResultHistoryEvent";
  }
}
