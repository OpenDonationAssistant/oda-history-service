package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.instancio.Instancio;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class ReelResultHistoryEventHandlerTest {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  HistoryItemRepository repository;

  @Test
  public void testHandleReelResultEvent(
    @Given String originId,
    @Given String title
  ) throws Exception {
    var historyItemData = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::originId), originId)
      .set(field(HistoryItemData::reelResults), List.of())
      .set(field(HistoryItemData::actions), List.of())
      .create();
    repository.create(historyItemData);

    var jsonEvent =
      """
      {
        "originId":"%s",
        "title":"%s"
      }
      """.formatted(originId, title);
    var message = jsonEvent.getBytes();

    var handler = new ReelResultHistoryEventHandler(objectMapper, repository);
    handler.handle(message);

    var updatedItem = repository.findByOriginId(originId);
    assertTrue(updatedItem.isPresent());
    assertTrue(
      updatedItem
        .get()
        .data()
        .reelResults()
        .stream()
        .anyMatch(rr -> title.equals(rr.title()))
    );
  }
}
