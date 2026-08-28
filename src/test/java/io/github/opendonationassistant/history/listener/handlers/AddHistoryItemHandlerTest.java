package io.github.opendonationassistant.history.listener.handlers;

import static io.github.opendonationassistant.history.listener.handlers.AddHistoryItemHandler.ChangeDonatonCommand;
import static io.github.opendonationassistant.history.listener.handlers.AddHistoryItemHandler.CreateAlertCommand;
import static io.github.opendonationassistant.history.listener.handlers.AddHistoryItemHandler.LinkReelCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.events.goal.GoalFacade;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInDefaultGoalCommand;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInSpecifiedGoalCommand;
import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.HistoryFacade.HistoryMessagingClient;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.history.command.AddHistoryItemApi.AddHistoryItemCommand;
import io.github.opendonationassistant.history.command.AddHistoryItemApi.AddHistoryItemCommand.AlertMedia;
import io.github.opendonationassistant.history.command.AddHistoryItemApi.AddHistoryItemCommand.TargetGoal;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class AddHistoryItemHandlerTest {

  ObjectMapper mapper = ObjectMapper.getDefault();
  HistoryMessagingClient messaging = mock(HistoryMessagingClient.class);
  HistoryItemRepository repository = mock(HistoryItemRepository.class);
  HistoryFacade facade = mock(HistoryFacade.class);
  GoalFacade goalFacade = mock(GoalFacade.class);
  AddHistoryItemHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new AddHistoryItemHandler(
      mapper,
      messaging,
      repository,
      facade,
      goalFacade
    );
    when(repository.findByOriginId(any())).thenReturn(Optional.empty());
    when(repository.create(any())).thenReturn(
      CompletableFuture.<HistoryItem>completedFuture(null)
    );
    when(facade.sendEvent(any())).thenReturn(
      CompletableFuture.<Void>completedFuture(null)
    );
    when(messaging.sendEvent(any(), any(), any(), any())).thenReturn(
      CompletableFuture.<Void>completedFuture(null)
    );
  }

  @Test
  public void testTypeIsAddHistoryItemCommand() {
    assertEquals("AddHistoryItemCommand", handler.type());
    assertEquals(AddHistoryItemCommand.class, handler.payloadClass());
  }

  @Test
  public void testIgnoresCommandWithoutPaymentId() throws IOException {
    handler.handle(
      new AddHistoryItemCommand(
        null,
        "nick",
        "recipient-1",
        new Amount(100, 1, "RUB"),
        "message",
        Instant.now(),
        "ODA",
        null,
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        null,
        null,
        "payment",
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false
      )
    );

    verifyNoInteractions(repository, facade, goalFacade, messaging);
  }

  @Test
  public void testSkipsDuplicatePayment() throws IOException {
    when(repository.findByOriginId("payment-1")).thenReturn(
      Optional.of(mock(HistoryItem.class))
    );

    handler.handle(command(List.of(), false, false, false, false, false));

    verify(repository).findByOriginId("payment-1");
    verify(repository, never()).create(any());
    verifyNoInteractions(facade, goalFacade, messaging);
  }

  @Test
  public void testCreatesHistoryItemWithDefaultValues() throws IOException {
    handler.handle(minimalCommand());

    var data = captureCreatedData();
    assertNotNull(data.id());
    assertEquals("payment", data.type());
    assertEquals("recipient-1", data.recipientId());
    assertEquals("Donate.Stream", data.system());
    assertEquals("payment-1", data.originId());
    assertNotNull(data.timestamp());
    assertEquals("Viewer", data.nickname());
    assertEquals(new Amount(100, 1, "RUB"), data.amount());
    assertNull(data.message());
    assertEquals(List.of(), data.attachments());
    assertEquals(List.of(), data.goals());
    assertEquals(List.of(), data.reelResults());
    assertEquals(List.of(), data.actions());
    assertNull(data.vote());
    assertEquals(List.of(), data.alerts());
    assertNull(data.level());
    assertNull(data.count());
    assertNull(data.levelName());
    assertFalse(data.deleted());
    assertEquals(Map.of(), data.metadata());
  }

  @Test
  public void testCreatesHistoryItemWithProvidedValues() throws IOException {
    handler.handle(command(List.of(), false, false, false, false, false));

    var data = captureCreatedData();
    assertEquals("payment", data.type());
    assertEquals("recipient-1", data.recipientId());
    assertEquals("ODA", data.system());
    assertEquals("payment-1", data.originId());
    assertEquals(Instant.parse("2024-01-02T00:00:00Z"), data.timestamp());
    assertEquals("Nick", data.nickname());
    assertEquals(new Amount(100, 1, "RUB"), data.amount());
    assertEquals("hello", data.message());
    assertEquals(2, data.level());
    assertEquals(3, data.count());
    assertEquals("vip", data.levelName());
  }

  @Test
  public void testCountsPaymentInSpecifiedGoal() throws IOException {
    var goal = new TargetGoal("goal-1", "Goal 1");

    handler.handle(command(List.of(goal), false, false, false, false, false));

    verify(goalFacade).run(
      new CountPaymentInSpecifiedGoalCommand(
        "payment-1",
        "recipient-1",
        "goal-1",
        new Amount(100, 1, "RUB")
      )
    );
    verify(goalFacade, never()).run(any(CountPaymentInDefaultGoalCommand.class));
  }

  @Test
  public void testCountsPaymentInDefaultGoalWhenAddToGoal() throws IOException {
    handler.handle(command(List.of(), true, false, false, false, false));

    verify(goalFacade).run(
      new CountPaymentInDefaultGoalCommand(
        "payment-1",
        "recipient-1",
        new Amount(100, 1, "RUB")
      )
    );
    verify(
      goalFacade,
      never()
    ).run(any(CountPaymentInSpecifiedGoalCommand.class));
  }

  @Test
  public void testSpecifiedGoalWinsWhenGoalsPresentAndAddToGoal()
    throws IOException {
    var goal = new TargetGoal("goal-1", "Goal 1");

    handler.handle(command(List.of(goal), true, false, false, false, false));

    verify(goalFacade).run(
      new CountPaymentInSpecifiedGoalCommand(
        "payment-1",
        "recipient-1",
        "goal-1",
        new Amount(100, 1, "RUB")
      )
    );
    verify(goalFacade, never()).run(any(CountPaymentInDefaultGoalCommand.class));
  }

  @Test
  public void testDoesNotCountGoalsByDefault() throws IOException {
    handler.handle(command(List.of(), false, false, false, false, false));

    verify(goalFacade, never()).run(any(CountPaymentInSpecifiedGoalCommand.class));
    verify(goalFacade, never()).run(any(CountPaymentInDefaultGoalCommand.class));
  }

  @Test
  public void testHandlesNullGoals() throws IOException {
    handler.handle(
      readCommand(
        """
        {
          "paymentId": "payment-1",
          "nickname": "  Nick  ",
          "recipientId": "recipient-1",
          "amount": { "minor": 1, "major": 100, "currency": "RUB" },
          "message": "hello",
          "authorizationTimestamp": "2024-01-02T00:00:00Z",
          "system": "ODA",
          "event": "payment",
          "count": 3,
          "level": 2,
          "levelName": "vip",
          "addToGoal": true
        }
        """
      )
    );

    verify(repository).create(any(HistoryItemData.class));
    verify(goalFacade).run(
      new CountPaymentInDefaultGoalCommand(
        "payment-1",
        "recipient-1",
        new Amount(100, 1, "RUB")
      )
    );
  }

  @Test
  public void testTriggersDonatonChange() throws IOException {
    handler.handle(command(List.of(), false, false, false, true, false));

    verify(facade).sendEvent(
      new ChangeDonatonCommand("recipient-1", new Amount(100, 1, "RUB"), "payment-1")
    );
    verify(facade, times(1)).sendEvent(any());
  }

  @Test
  public void testTriggersAlert() throws IOException {
    handler.handle(alertCommand());

    verify(facade).sendEvent(
      new CreateAlertCommand(
        "payment-1",
        "recipient-1",
        "  Nick  ",
        "hello",
        new Amount(100, 1, "RUB"),
        "https://media.example/alert.png",
        "ODA",
        "payment",
        3,
        "vip"
      )
    );
  }

  @Test
  public void testTriggersReel() throws IOException {
    handler.handle(command(List.of(), false, false, false, false, true));

    verify(facade).sendEvent(
      new LinkReelCommand("recipient-1", "payment-1", new Amount(100, 1, "RUB"))
    );
  }

  @Test
  public void testPublishesHistoryItemEventWhenAddToTop() throws IOException {
    handler.handle(command(List.of(), false, true, false, false, false));

    var data = captureCreatedData();
    var bytes = ArgumentCaptor.forClass(byte[].class);
    verify(messaging).sendEvent(
      eq("recipient"),
      eq("HistoryItemEvent"),
      eq("recipient-1"),
      bytes.capture()
    );

    var event = Objects.requireNonNull(
      mapper.readValue(bytes.getValue(), HistoryItemEvent.class)
    );
    assertEquals(data.id(), event.id());
    assertEquals(data.type(), event.type());
    assertEquals(data.recipientId(), event.recipientId());
    assertEquals(data.system(), event.system());
    assertEquals(data.originId(), event.originId());
    assertEquals(data.timestamp(), event.timestamp());
    assertEquals(data.nickname(), event.nickname());
    assertEquals(data.amount(), event.amount());
    assertEquals(data.message(), event.message());
    assertTrue(event.goals() == null || event.goals().isEmpty());
    assertTrue(event.actions() == null || event.actions().isEmpty());
    assertNull(event.vote());
  }

  @Test
  public void testDoesNotPublishWhenAddToTopIsFalse() throws IOException {
    handler.handle(command(List.of(), false, false, false, false, false));

    verify(messaging, never()).sendEvent(any(), any(), any(), any());
  }

  @Test
  public void testHandlesSerializedCommand() throws IOException {
    var bytes = mapper.writeValueAsBytes(
      command(List.of(), false, false, false, true, false)
    );

    handler.handle(bytes);

    verify(facade).sendEvent(
      new ChangeDonatonCommand("recipient-1", new Amount(100, 1, "RUB"), "payment-1")
    );
  }

  @Test
  public void testFullPipelineWithAllTriggers() throws IOException {
    handler.handle(fullTriggerCommand());

    verify(repository).create(any(HistoryItemData.class));
    verify(goalFacade).run(
      new CountPaymentInSpecifiedGoalCommand(
        "payment-1",
        "recipient-1",
        "goal-1",
        new Amount(100, 1, "RUB")
      )
    );
    verify(goalFacade, never()).run(any(CountPaymentInDefaultGoalCommand.class));
    verify(facade).sendEvent(
      new ChangeDonatonCommand("recipient-1", new Amount(100, 1, "RUB"), "payment-1")
    );
    verify(facade).sendEvent(
      new CreateAlertCommand(
        "payment-1",
        "recipient-1",
        "  Nick  ",
        "hello",
        new Amount(100, 1, "RUB"),
        "https://media.example/alert.png",
        "ODA",
        "payment",
        3,
        "vip"
      )
    );
    verify(facade).sendEvent(
      new LinkReelCommand("recipient-1", "payment-1", new Amount(100, 1, "RUB"))
    );
    verify(messaging).sendEvent(
      eq("recipient"),
      eq("HistoryItemEvent"),
      eq("recipient-1"),
      any(byte[].class)
    );
  }

  private AddHistoryItemCommand minimalCommand() {
    return readCommand(
      """
      {
        "paymentId": "payment-1",
        "nickname": "  Viewer  ",
        "recipientId": "recipient-1",
        "amount": { "minor": 1, "major": 100, "currency": "RUB" },
        "system": "Donate.Stream"
      }
      """
    );
  }

  private AddHistoryItemCommand readCommand(String json) {
    try {
      return Objects.requireNonNull(
        mapper.readValue(
          json.getBytes(StandardCharsets.UTF_8),
          AddHistoryItemCommand.class
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private AddHistoryItemCommand alertCommand() {
    return new AddHistoryItemCommand(
      "payment-1",
      "  Nick  ",
      "recipient-1",
      new Amount(100, 1, "RUB"),
      "hello",
      Instant.parse("2024-01-02T00:00:00Z"),
      "ODA",
      null,
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      new AlertMedia("https://media.example/alert.png"),
      null,
      "payment",
      3,
      2,
      "vip",
      true,
      false,
      false,
      false,
      false
    );
  }

  private AddHistoryItemCommand fullTriggerCommand() {
    return new AddHistoryItemCommand(
      "payment-1",
      "  Nick  ",
      "recipient-1",
      new Amount(100, 1, "RUB"),
      "hello",
      Instant.parse("2024-01-02T00:00:00Z"),
      "ODA",
      null,
      List.of(),
      List.of(),
      List.of(new TargetGoal("goal-1", "Goal 1")),
      List.of(),
      List.of(),
      new AlertMedia("https://media.example/alert.png"),
      null,
      "payment",
      3,
      2,
      "vip",
      true,
      true,
      true,
      true,
      true
    );
  }

  private AddHistoryItemCommand command(
    List<TargetGoal> goals,
    boolean addToGoal,
    boolean addToTop,
    boolean triggerAlert,
    boolean triggerDonaton,
    boolean triggerReel
  ) {
    return new AddHistoryItemCommand(
      "payment-1",
      "  Nick  ",
      "recipient-1",
      new Amount(100, 1, "RUB"),
      "hello",
      Instant.parse("2024-01-02T00:00:00Z"),
      "ODA",
      null,
      List.of(),
      List.of(),
      goals,
      List.of(),
      List.of(),
      null,
      null,
      "payment",
      3,
      2,
      "vip",
      triggerAlert,
      triggerReel,
      triggerDonaton,
      addToGoal,
      addToTop
    );
  }

  private HistoryItemData captureCreatedData() {
    var captor = ArgumentCaptor.forClass(HistoryItemData.class);
    verify(repository).create(captor.capture());
    return captor.getValue();
  }
}
