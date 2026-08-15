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
public class KickKicksGiftedEventHandler
  extends AbstractMessageHandler<
    KickKicksGiftedEventHandler.KickKicksGiftedEvent
  > {

  private final HistoryItemRepository repository;

  public KickKicksGiftedEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(KickKicksGiftedEvent event) throws IOException {
    var alreadyExists = repository
      .findByOriginId(event.id())
      .filter(item -> "Kick".equals(item.data().system()))
      .isPresent();
    if (alreadyExists) {
      return;
    }
    final HistoryItemData data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "kick-gift",
      event.recipientId(),
      "Kick",
      event.id(),
      event.createdAt(),
      event.senderUsername(),
      null,
      event.giftName() + " " + event.giftType() + " " + event.giftTier(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      null,
      List.of(),
      null,
      event.amount(),
      null,
      HistoryItemData.NOT_DELETED,
      Map.of()
    );
    repository.create(data).join();
  }

  @Serdeable
  public record KickKicksGiftedEvent(
    String id,
    String recipientId,
    String senderUsername,
    String giftName,
    String giftType,
    String giftTier,
    Integer amount,
    Instant createdAt
  ) implements HasRecipientId {}
}
