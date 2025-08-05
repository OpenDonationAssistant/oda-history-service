package io.github.opendonationassistant;

import static io.github.opendonationassistant.HistoryItemDataTestDataBuilder.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.events.PaymentNotificationSender;
import io.github.opendonationassistant.events.alerts.AlertSender;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CommandListenerTest {

  HistoryItemRepository repository = mock(HistoryItemRepository.class);
  PaymentNotificationSender notificationSender = mock(
    PaymentNotificationSender.class
  );
  AlertSender alertSender = mock(AlertSender.class);
  CommandListener listener = new CommandListener(
    repository,
    notificationSender,
    alertSender
  );

  @Test
  public void testHandlingUpdateCommand() {
    var merged = mock(HistoryItem.class);
    var old = mock(HistoryItem.class);
    when(old.merge(any())).thenReturn(merged);
    when(repository.findById(any())).thenReturn(Optional.of(old));

    var commandData = minimalHistoryItem();

    listener.listen(
      new HistoryCommand("update", commandData, false, false, false, false)
    );

    verify(old).merge(commandData);
    verify(merged).save(repository);
  }
}
