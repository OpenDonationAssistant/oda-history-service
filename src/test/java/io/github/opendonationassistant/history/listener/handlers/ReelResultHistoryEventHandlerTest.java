package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.instancio.Instancio;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone", transactional = false)
@ExtendWith(InstancioExtension.class)
public class ReelResultHistoryEventHandlerTest {

  private ODALogger log = new ODALogger(this);

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
      .set(field(HistoryItemData::metadata), Map.of())
      .create();
    repository
      .create(historyItemData)
      .thenRun(() -> {
        var jsonEvent =
          """
          {
            "originId":"%s",
            "title":"%s"
          }
          """.formatted(originId, title);
        var message = jsonEvent.getBytes();

        var handler = new ReelResultHistoryEventHandler(
          objectMapper,
          repository
        );
        try {
          handler.handle(message);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        var updatedItem = repository.findByOriginId(originId);
        assertTrue(
          updatedItem.isPresent(),
          "Updated HistoryItem should be present"
        );
        log.debug(
          "Updated HistoryItem",
          Map.of("updatedItem", updatedItem.get().data())
        );
        assertTrue(
          updatedItem
            .get()
            .data()
            .reelResults()
            .stream()
            .anyMatch(rr -> title.equals(rr.title())),
          "Reel result should be present"
        );
      })
      .join();
  }
}
