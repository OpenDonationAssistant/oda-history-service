package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.opendonationassistant.history.listener.handlers.PrintHistoryHandler.PrintHistoryCommand;
import io.github.opendonationassistant.history.model.Printer;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.awaitility.Awaitility;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone")
public class PrintHistoryHandlerTest {

  @Inject
  Map<String, Printer.PrintableData> printableData;

  @Inject
  HistoryItemDataRepository repository;

  @Inject
  ObjectMapper objectMapper;

  Model<HistoryItemData> baseModel = Instancio.of(HistoryItemData.class)
    .set(field(HistoryItemData::recipientId), "testuser")
    .set(field(HistoryItemData::actions), List.of())
    .set(field(HistoryItemData::reelResults), List.of())
    .set(field(HistoryItemData::goals), List.of())
    .set(field(HistoryItemData::attachments), List.of())
    .set(field(HistoryItemData::alerts), List.of())
    .set(field(HistoryItemData::vote), null)
    .set(field(HistoryItemData::system), "ODA")
    .set(field(HistoryItemData::type), "payment")
    .toModel();

  private void create(Model<HistoryItemData> model) {
    this.create(model, 1);
  }

  private void create(Model<HistoryItemData> model, Integer count) {
    Instancio.stream(model).limit(count).forEach(repository::save);
  }

  @Test
  public void testLoadingData() throws IOException {
    Printer printer = new Printer(repository, printableData);
    PrintHistoryHandler handler = new PrintHistoryHandler(
      objectMapper,
      printer
    );

    create(baseModel, 10);
    handler.handle(
      new PrintHistoryCommand("testuser", "printId", null, null, null, null)
    );
    Awaitility.await()
      .timeout(10, java.util.concurrent.TimeUnit.SECONDS)
      .until(() -> printer.isReady("testuser", "printId"));
    var data = printableData.get("printId");
    assertNotNull(data);
    assertNotNull(data.data());
    assertEquals(10, data.data().size());
  }

  @Test
  public void testFilteringDates() throws IOException {
    Printer printer = new Printer(repository, printableData);
    PrintHistoryHandler handler = new PrintHistoryHandler(
      objectMapper,
      printer
    );

    create(
      Instancio.of(baseModel)
        .set(
          field(HistoryItemData::timestamp),
          Instant.parse("2023-01-01T00:00:00.000Z")
        )
        .toModel(),
      3
    );

    final Model<HistoryItemData> item = Instancio.of(baseModel)
      .set(
        field(HistoryItemData::timestamp),
        Instant.parse("2025-01-01T00:00:00.000Z")
      )
      .toModel();
    create(Instancio.of(item).toModel());

    create(
      Instancio.of(item)
        .set(field(HistoryItemData::type), "subscription")
        .toModel(),
      2
    );

    create(
      Instancio.of(baseModel)
        .set(field(HistoryItemData::system), "DonateX")
        .toModel(),
      2
    );

    create(
      Instancio.of(baseModel)
        .set(
          field(HistoryItemData::timestamp),
          Instant.parse("2026-01-01T00:00:00.000Z")
        )
        .toModel(),
      2
    );

    handler.handle(
      new PrintHistoryCommand(
        "testuser",
        "printId",
        List.of("ODA"),
        List.of("payment"),
        Instant.parse("2024-01-01T00:00:00.000Z"),
        Instant.parse("2025-06-01T00:00:00.000Z")
      )
    );
    Awaitility.await()
      .timeout(10, java.util.concurrent.TimeUnit.SECONDS)
      .until(() -> printer.isReady("testuser", "printId"));
    var data = printableData.get("printId");
    assertNotNull(data);
    assertNotNull(data.data());
    assertEquals(1, data.data().size());
  }
}
