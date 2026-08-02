package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.events.twitch.events.TwitchChannelCheerEvent;
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
public class TwitchChannelCheerEventHandlerTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  HistoryItemRepository repository;

  @Inject
  HistoryItemDataRepository dataRepository;

  @Test
  public void testHandlingCheerEvent() throws IOException {
    var event = new TwitchChannelCheerEvent(
      "cheer-message-created",
      "recipient",
      "viewer",
      "Cheer cheer100 for the stream!",
      "100"
    );

    var handler = new TwitchChannelCheerEventHandler(objectMapper, repository);
    handler.handle(objectMapper.writeValueAsBytes(event));

    var saved = repository.findByOriginId("cheer-message-created");
    assertTrue(saved.isPresent());
    var data = saved.get().data();
    assertEquals("cheer", data.type());
    assertEquals("Twitch", data.system());
    assertEquals("recipient", data.recipientId());
    assertEquals("viewer", data.nickname());
    assertEquals("Cheer cheer100 for the stream!", data.message());
  }

  @Test
  public void testHandlingCheerEventWithoutUsername() throws IOException {
    var event = new TwitchChannelCheerEvent(
      "cheer-message-no-user",
      "recipient",
      null,
      "Cheer cheer100",
      "100"
    );

    var handler = new TwitchChannelCheerEventHandler(objectMapper, repository);
    handler.handle(objectMapper.writeValueAsBytes(event));

    var saved = repository.findByOriginId("cheer-message-no-user");
    assertTrue(saved.isPresent());
    assertEquals(null, saved.get().data().nickname());
  }

}
