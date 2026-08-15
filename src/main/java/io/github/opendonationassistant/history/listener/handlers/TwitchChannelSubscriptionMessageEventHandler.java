package io.github.opendonationassistant.history.listener.handlers;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.twitch.events.TwitchChannelSubscriptionMessageEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Singleton
public class TwitchChannelSubscriptionMessageEventHandler
  extends AbstractMessageHandler<TwitchChannelSubscriptionMessageEvent> {

  private final HistoryItemRepository repository;

  public TwitchChannelSubscriptionMessageEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(TwitchChannelSubscriptionMessageEvent event)
    throws IOException {
    var alreadyExists = repository
      .findByOriginId(event.id())
      .filter(item -> "Twitch".equals(item.data().system()))
      .isPresent();
    if (alreadyExists) {
      return;
    }
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "subscription",
      event.recipientId(),
      "Twitch",
      event.id(),
      Instant.now(),
      event.username(),
      null,
      event.message() == null ? null : event.message().text(),
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
      Map.of()
    );
    repository.create(data).join();
  }
}
