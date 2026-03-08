package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.MessageHandler;
import io.github.opendonationassistant.events.history.event.MediaHistoryEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Map;

public class MediaHistoryEventHandler implements MessageHandler {

  private final ODALogger log = new ODALogger(this);
  private final ObjectMapper objectMapper;
  private final HistoryItemRepository repository;

  @Inject
  public MediaHistoryEventHandler(
    ObjectMapper objectMapper,
    HistoryItemRepository repository
  ) {
    this.objectMapper = objectMapper;
    this.repository = repository;
  }

  @Override
  public void handle(byte[] message) throws IOException {
    var event = objectMapper.readValue(message, MediaHistoryEvent.class);
    if (event == null) {
      return;
    }
    log.debug("Received MediaHistoryEvent", Map.of("notification", event));
    repository
      .findById(event.originId())
      .ifPresent(historyItem ->
        historyItem.addMedia(
          new HistoryItemData.Attachment(
            event.mediaId(),
            event.url(),
            event.title(),
            event.thumbnail()
          )
        )
      );
  }

  @Override
  public String type() {
    return "MediaHistoryEvent";
  }
}
