package io.github.opendonationassistant;

import static io.github.opendonationassistant.HistoryItemDataTestDataBuilder.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.events.PaymentNotificationSender;
import io.github.opendonationassistant.events.alerts.AlertSender;
import io.github.opendonationassistant.events.goal.GoalFacade;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInDefaultGoalCommand;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInSpecifiedGoalCommand;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CommandListenerTest {

  HistoryItemRepository repository = mock(HistoryItemRepository.class);
  PaymentNotificationSender notificationSender = mock(
    PaymentNotificationSender.class
  );
  AlertSender alertSender = mock(AlertSender.class);
  GoalFacade goalFacade = mock(GoalFacade.class);
  CommandListener listener = new CommandListener(
    repository,
    notificationSender,
    alertSender,
    goalFacade
  );

  @Test
  public void testHandlingUpdateCommand() {
    var merged = mock(HistoryItem.class);
    var old = mock(HistoryItem.class);
    when(old.merge(any())).thenReturn(merged);
    when(repository.findById(any())).thenReturn(Optional.of(old));

    var commandData = minimalHistoryItem();

    listener.listen(
      new HistoryCommand(
        "update",
        commandData,
        false,
        false,
        false,
        false,
        false
      )
    );

    verify(old).merge(commandData);
    verify(merged).save(repository);
  }

  @Test
  public void testAddingItemWithSpecifiedGoal() {
    var merged = mock(HistoryItem.class);
    var old = mock(HistoryItem.class);
    when(old.merge(any())).thenReturn(merged);
    when(repository.findById(any())).thenReturn(Optional.of(old));

    var commandData = new HistoryItem() {
      {
        setPaymentId("paymentId");
        setRecipientId("recipientId");
        setAmount(new Amount(100, 0, "RUB"));
        setGoals(List.of(new TargetGoal("goalId", "goalName")));
      }
    };

    listener.listen(
      new HistoryCommand(
        "create",
        commandData,
        false,
        false,
        false,
        false,
        true
      )
    );

    verify(goalFacade).run(
      new CountPaymentInSpecifiedGoalCommand(
        "paymentId",
        "recipientId",
        "goalId",
        new Amount(100, 0, "RUB")
      )
    );
  }

  @Test
  public void testAddingItemWithDefaultGoal() {
    var merged = mock(HistoryItem.class);
    var old = mock(HistoryItem.class);
    when(old.merge(any())).thenReturn(merged);
    when(repository.findById(any())).thenReturn(Optional.of(old));

    var commandData = new HistoryItem() {
      {
        setPaymentId("paymentId");
        setRecipientId("recipientId");
        setAmount(new Amount(100, 0, "RUB"));
      }
    };

    listener.listen(
      new HistoryCommand(
        "create",
        commandData,
        false,
        false,
        false,
        false,
        true
      )
    );

    verify(goalFacade).run(
      new CountPaymentInDefaultGoalCommand(
        "paymentId",
        "recipientId",
        new Amount(100, 0, "RUB")
      )
    );
  }

  @Test
  public void testDontAddGoalWhenNotSpecified() {
    var merged = mock(HistoryItem.class);
    var old = mock(HistoryItem.class);
    when(old.merge(any())).thenReturn(merged);
    when(repository.findById(any())).thenReturn(Optional.of(old));

    var commandData = new HistoryItem() {
      {
        setPaymentId("paymentId");
        setRecipientId("recipientId");
        setAmount(new Amount(100, 0, "RUB"));
      }
    };

    var commandDataWithGoals = new HistoryItem() {
      {
        setPaymentId("paymentId");
        setRecipientId("recipientId");
        setAmount(new Amount(100, 0, "RUB"));
        setGoals(List.of(new TargetGoal("goalId", "goalName")));
      }
    };

    listener.listen(
      new HistoryCommand(
        "create",
        commandData,
        false,
        false,
        false,
        false,
        false
      )
    );

    listener.listen(
      new HistoryCommand(
        "create",
        commandDataWithGoals,
        false,
        false,
        false,
        false,
        false
      )
    );

    verifyNoInteractions(goalFacade);
  }

  @Test
  public void testDontAddGoalOnUpdate() {
    var merged = mock(HistoryItem.class);
    var old = mock(HistoryItem.class);
    when(old.merge(any())).thenReturn(merged);
    when(repository.findById(any())).thenReturn(Optional.of(old));

    var commandData = new HistoryItem() {
      {
        setPaymentId("paymentId");
        setRecipientId("recipientId");
        setAmount(new Amount(100, 0, "RUB"));
      }
    };

    var commandDataWithGoals = new HistoryItem() {
      {
        setPaymentId("paymentId");
        setRecipientId("recipientId");
        setAmount(new Amount(100, 0, "RUB"));
        setGoals(List.of(new TargetGoal("goalId", "goalName")));
      }
    };

    listener.listen(
      new HistoryCommand(
        "update",
        commandData,
        false,
        false,
        false,
        false,
        true
      )
    );

    listener.listen(
      new HistoryCommand(
        "update",
        commandDataWithGoals,
        false,
        false,
        false,
        false,
        true
      )
    );

    verifyNoInteractions(goalFacade);
  }
}
