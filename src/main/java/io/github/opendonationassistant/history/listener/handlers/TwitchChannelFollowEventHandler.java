package io.github.opendonationassistant.history.listener.handlers;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.twitch.events.TwitchChannelFollowEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class TwitchChannelFollowEventHandler
  extends AbstractMessageHandler<TwitchChannelFollowEvent> {

  private final HistoryItemRepository repository;

  @Inject
  public TwitchChannelFollowEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(TwitchChannelFollowEvent event) throws IOException {
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "follow",
      event.recipientId(),
      "twitch",
      event.id(),
      event.timestamp(),
      event.username(),
      null,
      null,
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      null,
      List.of(),
      null,
      null,
      null
    );
    repository.create(data);
  }
}
