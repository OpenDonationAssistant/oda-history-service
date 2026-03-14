package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.history.event.MediaHistoryEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import java.io.IOException;

public class MediaHistoryEventHandler
  extends AbstractMessageHandler<MediaHistoryEvent> {

  private final HistoryItemRepository repository;

  @Inject
  public MediaHistoryEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(MediaHistoryEvent event) throws IOException {
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
}
