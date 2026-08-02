package io.github.opendonationassistant.history.listener.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.events.twitch.events.TwitchChannelSubscriptionMessageEvent;
import io.github.opendonationassistant.events.twitch.events.TwitchChannelSubscriptionMessageEvent.Message;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone", transactional = false)
public class TwitchChannelSubscriptionMessageEventHandlerTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  HistoryItemRepository repository;

  @Inject
  HistoryItemDataRepository dataRepository;

  @Test
  public void testHandlingSubscriptionMessageEvent() throws IOException {
    var message = new Message("Thank you for the sub!", List.of());
    var event = new TwitchChannelSubscriptionMessageEvent(
      "subscription-message-created",
      "recipient",
      "viewer",
      "1000",
      message,
      12,
      24,
      6
    );

    var handler = new TwitchChannelSubscriptionMessageEventHandler(
      objectMapper,
      repository
    );
    handler.handle(objectMapper.writeValueAsBytes(event));

    var saved = repository.findByOriginId("subscription-message-created");
    assertTrue(saved.isPresent());
    var data = saved.get().data();
    assertEquals("subscription", data.type());
    assertEquals("Twitch", data.system());
    assertEquals("recipient", data.recipientId());
    assertEquals("viewer", data.nickname());
    assertEquals("Thank you for the sub!", data.message());
  }
  
}
