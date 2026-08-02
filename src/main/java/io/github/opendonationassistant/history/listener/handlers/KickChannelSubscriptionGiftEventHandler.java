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
public class KickChannelSubscriptionGiftEventHandler
  extends AbstractMessageHandler<
    KickChannelSubscriptionGiftEventHandler.KickChannelSubscriptionGiftEvent
  > {

  private final HistoryItemRepository repository;

  public KickChannelSubscriptionGiftEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(KickChannelSubscriptionGiftEvent event)
    throws IOException {
    var alreadyExists = repository
      .findByOriginId(event.id())
      .filter(item -> "Kick".equals(item.data().system()))
      .isPresent();
    if (alreadyExists) {
      return;
    }
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "subscription-gift",
      event.recipientId(),
      "Kick",
      event.id(),
      event.createdAt(),
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
  public record KickChannelSubscriptionGiftEvent(
    String id,
    String recipientId,
    String username,
    Integer amount,
    Instant createdAt
  ) implements HasRecipientId {}
}
