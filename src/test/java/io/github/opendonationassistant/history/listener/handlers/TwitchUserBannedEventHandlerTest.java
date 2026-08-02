package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.events.twitch.events.TwitchUserBannedEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone", transactional = false)
public class TwitchUserBannedEventHandlerTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  HistoryItemRepository repository;

  @Inject
  HistoryItemDataRepository dataRepository;

  @Test
  public void testHandlingPermanentBanEvent() throws IOException {
    var event = new TwitchUserBannedEvent(
      "user-banned-created",
      "recipient",
      "baduser",
      true
    );

    var handler = new TwitchUserBannedEventHandler(objectMapper, repository);
    handler.handle(objectMapper.writeValueAsBytes(event));

    var saved = repository.findByOriginId("user-banned-created");
    assertTrue(saved.isPresent());
    var data = saved.get().data();
    assertEquals("ban", data.type());
    assertEquals("Twitch", data.system());
    assertEquals("recipient", data.recipientId());
    assertEquals("baduser", data.nickname());
    assertEquals("Permanent ban for baduser", data.message());
  }

  @Test
  public void testHandlingTemporaryBanEvent() throws IOException {
    var event = new TwitchUserBannedEvent(
      "user-banned-temporary",
      "recipient",
      "baduser",
      false
    );

    var handler = new TwitchUserBannedEventHandler(objectMapper, repository);
    handler.handle(objectMapper.writeValueAsBytes(event));

    var saved = repository.findByOriginId("user-banned-temporary");
    assertTrue(saved.isPresent());
    assertEquals("ban", saved.get().data().type());
    assertEquals("Temporary ban for baduser", saved.get().data().message());
  }

}
