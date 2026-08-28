package io.github.opendonationassistant.history.listener.handlers;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemData.ActionRequest;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    var item1 = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::id), "item-1")
      .set(field(HistoryItemData::type), "payment")
      .set(field(HistoryItemData::recipientId), "recipient-1")
      .set(field(HistoryItemData::system), "ODA")
      .set(field(HistoryItemData::originId), "origin-1")
      .set(
        field(HistoryItemData::timestamp),
        Instant.parse("2024-01-02T00:00:00Z")
      )
      .set(field(HistoryItemData::nickname), "nick-1")
      .set(field(HistoryItemData::amount), new Amount(100, 1, "RUB"))
      .set(field(HistoryItemData::message), "message-1")
      .set(field(HistoryItemData::actions), List.of())
      .set(field(HistoryItemData::goals), List.of())
      .set(field(HistoryItemData::vote), null)
      .set(field(HistoryItemData::metadata), Map.of())
      .create();
    var item2 = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::id), "item-2")
      .set(field(HistoryItemData::type), "follow")
      .set(field(HistoryItemData::recipientId), "recipient-1")
      .set(field(HistoryItemData::system), "Twitch")
      .set(field(HistoryItemData::originId), null)
      .set(
        field(HistoryItemData::timestamp),
        Instant.parse("2024-01-03T00:00:00Z")
      )
      .set(field(HistoryItemData::nickname), null)
      .set(field(HistoryItemData::amount), new Amount(200, 2, "USD"))
      .set(field(HistoryItemData::message), null)
      .set(field(HistoryItemData::actions), List.of())
      .set(field(HistoryItemData::goals), List.of())
      .set(field(HistoryItemData::vote), null)
      .set(field(HistoryItemData::metadata), Map.of())
      .create();
    when(
      repository.findByRecipientIdAndTypeInAndTimestampGreaterThanEqualOrderByTimestampAsc(
        any(),
        any(),
        any()
      )
    ).thenReturn(List.of(item1, item2));
    when(facade.sendEvent(any())).thenReturn(
      CompletableFuture.completedFuture(null)
    );

    var response = handler.handle(
      new DumpHistoryRequestHandler.DumpHistoryRequest(
        "recipient-1",
        from,
        List.of("payment", "follow")
      )
    );

    assertEquals(2, response.count());
    verify(
      repository
    ).findByRecipientIdAndTypeInAndTimestampGreaterThanEqualOrderByTimestampAsc(
      "recipient-1",
      List.of("payment", "follow"),
      from
    );
    verify(facade).sendEvent(argThat(payload -> matches(item1, payload)));
    verify(facade).sendEvent(argThat(payload -> matches(item2, payload)));
    verify(facade, times(2)).sendEvent(any(HistoryItemEvent.class));
  }

  @Test
  public void testDumpPublishesNothingWhenNoItems() {
    var from = Instant.parse("2024-01-01T00:00:00Z");
    when(
      repository.findByRecipientIdAndTypeInAndTimestampGreaterThanEqualOrderByTimestampAsc(
        any(),
        any(),
        any()
      )
    ).thenReturn(List.of());

    var response = handler.handle(
      new DumpHistoryRequestHandler.DumpHistoryRequest(
        "recipient-1",
        from,
        List.of("payment")
      )
    );

    assertEquals(0, response.count());
    verify(facade, never()).sendEvent(any());
  }

  private boolean matches(HistoryItemData item, HasRecipientId payload) {
    // TODO hardcode values instead
    if (!(payload instanceof HistoryItemEvent event)) {
      return false;
    }
    return (
      event.id().equals(item.id()) &&
      event.type().equals(item.type()) &&
      event.recipientId().equals(item.recipientId()) &&
      event.system().equals(item.system()) &&
      Objects.equals(event.originId(), item.originId()) &&
      event.timestamp().equals(item.timestamp()) &&
      Objects.equals(event.nickname(), item.nickname()) &&
      Objects.equals(event.amount(), item.amount()) &&
      Objects.equals(event.message(), item.message()) &&
      event
        .goals()
        .equals(item.goals().stream().map(it -> it.goalId()).toList()) &&
      event
        .actions()
        .equals(
          item
            .actions()
            .stream()
            .map(DumpHistoryRequestHandlerTest::toAction)
            .toList()
        ) &&
      Objects.equals(
        event.vote(),
        Optional.ofNullable(item.vote())
          .map(v -> new HistoryItemEvent.Vote(v.id(), v.name(), v.isNew()))
          .orElse(null)
      )
    );
  }

  private static HistoryItemEvent.ActionRequest toAction(ActionRequest a) {
    return new HistoryItemEvent.ActionRequest(
      a.id(),
      a.actionId(),
      a.name(),
      a.amount(),
      a.payload()
    );
  }
}
