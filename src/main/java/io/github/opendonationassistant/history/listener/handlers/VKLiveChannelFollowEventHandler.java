package io.github.opendonationassistant.history.listener.handlers;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Singleton
public class VKLiveChannelFollowEventHandler
  extends AbstractMessageHandler<
    VKLiveChannelFollowEventHandler.VKLiveChannelFollowEvent
  > {

  private final HistoryItemRepository repository;

  public VKLiveChannelFollowEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(VKLiveChannelFollowEvent event) throws IOException {
    var alreadyExists = repository
      .findByOriginId(event.id())
      .filter(item -> "VKLive".equals(item.data().system()))
      .isPresent();
    if (alreadyExists) {
      return;
    }
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "follow",
      event.recipientId(),
      "VKLive",
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
      HistoryItemData.NOT_DELETED
    );
    repository.create(data).join();
  }

  @Serdeable
  public record VKLiveChannelFollowEvent(
    String id,
    String recipientId,
    String username,
    Instant timestamp
  ) implements HasRecipientId {}
}
