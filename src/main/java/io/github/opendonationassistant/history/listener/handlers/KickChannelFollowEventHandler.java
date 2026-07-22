package io.github.opendonationassistant.history.listener.handlers;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Singleton
public class KickChannelFollowEventHandler
  extends AbstractMessageHandler<
    KickChannelFollowEventHandler.KickChannelFollowEvent
  > {

  private final HistoryItemRepository repository;

  @Inject
  public KickChannelFollowEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(KickChannelFollowEvent event) throws IOException {
    var alreadyExists = repository
      .findByOriginId(event.id())
      .filter(item -> "kick".equals(item.data().system()))
      .isPresent();
    if (alreadyExists) {
      return;
    }
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "follow",
      event.recipientId(),
      "kick",
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
      null,
      false
    );
    repository.create(data);
  }

  @Serdeable
  public record KickChannelFollowEvent(
    String id,
    String recipientId,
    String username,
    Instant timestamp
  ) {}
}
