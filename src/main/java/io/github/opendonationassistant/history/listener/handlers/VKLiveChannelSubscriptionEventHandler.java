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
import java.util.Map;

@Singleton
public class VKLiveChannelSubscriptionEventHandler
  extends AbstractMessageHandler<
    VKLiveChannelSubscriptionEventHandler.VKLiveChannelSubscriptionEvent
  > {

  private final HistoryItemRepository repository;

  public VKLiveChannelSubscriptionEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(VKLiveChannelSubscriptionEvent event)
    throws IOException {
    var alreadyExists = repository
      .findByOriginId(event.id())
      .filter(item -> "VKLive".equals(item.data().system()))
      .isPresent();
    if (alreadyExists) {
      return;
    }
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "subscription",
      event.recipientId(),
      "VKLive",
      event.id(),
      event.renewedAt(),
      event.username(),
      null,
      "Subscription tier " + event.tier(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      null,
      List.of(),
      null,
      event.amount().intValue(),
      null,
      HistoryItemData.NOT_DELETED,
      Map.of()
    );
    repository.create(data).join();
  }

  @Serdeable
  public record VKLiveChannelSubscriptionEvent(
    String id,
    String recipientId,
    String username,
    String tier,
    Long amount,
    Instant renewedAt
  ) implements HasRecipientId {}
}