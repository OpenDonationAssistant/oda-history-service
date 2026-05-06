package io.github.opendonationassistant.history.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PrinterTest {

  Printer printer = new Printer(
    mock(HistoryItemDataRepository.class),
    Map.of()
  );
  private ODALogger log = new ODALogger(this);

  @Test
  public void testCreatingCSV() {
    var data = new HistoryItemData(
      "id",
      "type",
      "testuser",
      "system",
      "originId",
      Instant.parse("2022-01-01T00:00:00.000Z"),
      "nickname",
      new Amount(100, 0, "RUB"),
      "message",
      List.of(), //attachments
      List.of(), //goals
      List.of(), //reelResults
      List.of(), //actions
      null, // vote
      List.of(),
      null,
      2,
      "levelName"
    );

    final String result = printer.print(
      new Printer.PrintableData("testuser", List.of(data))
    );
    log.info(result, Map.of());
    assertEquals(
      "Event;System;Timestamp;Nickname;Amount;Message;Goal;Roulette;Actions;LevelName;ItemCount;\ntype;system;2022-01-01T00:00:00Z;\"nickname\";100.0;\"message\";\"\";\"\";\"\";\"levelName\";2;\n",
      result
    );
  }
}
