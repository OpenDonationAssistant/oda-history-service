package io.github.opendonationassistant.history.listener.handlers;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Singleton
public class TwitchChannelRaidEventHandler
  extends AbstractMessageHandler<
    TwitchChannelRaidEventHandler.TwitchChannelRaidEvent
  > {

  private final HistoryItemRepository repository;

  public TwitchChannelRaidEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Serdeable
  public record TwitchChannelRaidEvent(
    String id,
    String recipientId,
    String fromChannelId,
    String fromChannelName,
    Integer viewerCount
  ) {}

  @Override
  public void handle(TwitchChannelRaidEvent event) throws IOException {
    var alreadyExists = repository
      .findByOriginId(event.id())
      .filter(item -> "Twitch".equals(item.data().system()))
      .isPresent();
    if (alreadyExists) {
      return;
    }
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "raid",
      event.recipientId(),
      "Twitch",
      event.id(),
      Instant.now(),
      event.fromChannelName(),
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
      null,
      HistoryItemData.NOT_DELETED,
      Map.of("fromChannelId", event.fromChannelId())
    );
    repository.create(data).join();
  }
}
