package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.history.listener.handlers.KickChannelSubscriptionGiftEventHandler.KickChannelSubscriptionGiftEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.StreamSupport;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone", transactional = false)
public class KickChannelSubscriptionGiftEventHandlerTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  HistoryItemRepository repository;

  @Inject
  HistoryItemDataRepository dataRepository;

  @Test
  public void testHandlingKickSubscriptionGiftEvent() throws IOException {
    var event = new KickChannelSubscriptionGiftEvent(
      "kick-gift-created",
      "recipient",
      "viewer",
      5,
      Instant.parse("2026-01-01T00:00:00Z")
    );

    var handler = new KickChannelSubscriptionGiftEventHandler(
      objectMapper,
      repository
    );
    handler.handle(objectMapper.writeValueAsBytes(event));

    var saved = repository.findByOriginId("kick-gift-created");
    assertTrue(saved.isPresent());
    var data = saved.get().data();
    assertEquals("subscription-gift", data.type());
    assertEquals("Kick", data.system());
    assertEquals("recipient", data.recipientId());
    assertEquals("viewer", data.nickname());
    assertEquals(Instant.parse("2026-01-01T00:00:00Z"), data.timestamp());
  }

}
