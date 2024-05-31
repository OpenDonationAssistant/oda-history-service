package io.github.opendonationassistant;

import static io.github.opendonationassistant.HistoryItemDataTestDataBuilder.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CommandListenerTest {

  HistoryItemRepository repository = mock(HistoryItemRepository.class);
  CommandListener listener = new CommandListener(repository);

  @Test
  public void testHandlingUpdateCommand() {
    var merged = mock(HistoryItem.class);
    var old = mock(HistoryItem.class);
    when(old.merge(any())).thenReturn(merged);
    when(repository.findById(any())).thenReturn(Optional.of(old));

    var commandData = minimalHistoryItem();

    listener.listen(
      new HistoryCommand() {
        {
          setType("update");
          setPartial(commandData);
        }
      }
    );

    verify(old).merge(commandData);
    verify(merged).save(repository);
  }
}
