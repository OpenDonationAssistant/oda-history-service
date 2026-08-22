package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

public class DumpHistoryRequestHandlerTest {

  HistoryItemDataRepository repository = mock(HistoryItemDataRepository.class);
  HistoryFacade facade = mock(HistoryFacade.class);
  DumpHistoryRequestHandler handler = new DumpHistoryRequestHandler(
    repository,
    facade
  );

  @Test
  public void testDumpPublishesAllItemsFromTimestamp() {
    var from = Instant.parse("2024-01-01T00:00:00Z");
    var item = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::recipientId), "recipient-1")
      .set(field(HistoryItemData::system), "ODA")
      .set(field(HistoryItemData::type), "payment")
      .set(
        field(HistoryItemData::timestamp),
        Instant.parse("2024-01-02T00:00:00Z")
      )
      .set(field(HistoryItemData::actions), List.of())
      .set(field(HistoryItemData::goals), List.of())
      .set(field(HistoryItemData::metadata), Map.of())
      .create();
    when(
      repository.findByRecipientIdAndTimestampGreaterThanEqualOrderByTimestampAsc(
        any(),
        any()
      )
    ).thenReturn(List.of(item, item));
    when(facade.sendEvent(any())).thenReturn(
      CompletableFuture.completedFuture(null)
    );

    var response = handler.handle(
      new DumpHistoryRequestHandler.DumpHistoryRequest("recipient-1", from)
    );

    assertEquals(2, response.count());
    verify(repository).findByRecipientIdAndTimestampGreaterThanEqualOrderByTimestampAsc(
      "recipient-1",
      from
    );
    verify(facade, times(2)).sendEvent(any(HistoryItemEvent.class));
  }

  @Test
  public void testDumpPublishesNothingWhenNoItems() {
    var from = Instant.parse("2024-01-01T00:00:00Z");
    when(
      repository.findByRecipientIdAndTimestampGreaterThanEqualOrderByTimestampAsc(
        "recipient-1",
        from
      )
    ).thenReturn(List.of());

    var response = handler.handle(
      new DumpHistoryRequestHandler.DumpHistoryRequest("recipient-1", from)
    );

    assertEquals(0, response.count());
    verify(facade, never()).sendEvent(any());
  }
}
