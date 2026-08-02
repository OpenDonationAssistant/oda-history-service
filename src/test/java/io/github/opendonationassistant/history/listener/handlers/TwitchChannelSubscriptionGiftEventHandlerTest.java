package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.events.twitch.events.TwitchChannelSubscriptionGiftEvent;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone", transactional = false)
public class TwitchChannelSubscriptionGiftEventHandlerTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  HistoryItemRepository repository;

  @Inject
  HistoryItemDataRepository dataRepository;

  @Test
  public void testHandlingSubscriptionGiftEvent() throws IOException {
    var event = new TwitchChannelSubscriptionGiftEvent(
      "gift-created",
      "recipient",
      "gifter",
      "1000",
      5,
      10
    );

    var handler = new TwitchChannelSubscriptionGiftEventHandler(
      objectMapper,
      repository
    );
    handler.handle(objectMapper.writeValueAsBytes(event));

    var saved = repository.findByOriginId("gift-created");
    assertTrue(saved.isPresent());
    var data = saved.get().data();
    assertEquals("subscription-gift", data.type());
    assertEquals("Twitch", data.system());
    assertEquals("recipient", data.recipientId());
    assertEquals("gifter", data.nickname());
  }
}
