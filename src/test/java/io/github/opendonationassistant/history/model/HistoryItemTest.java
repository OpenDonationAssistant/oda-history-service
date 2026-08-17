package io.github.opendonationassistant.history.model;

import static org.instancio.Select.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemData.Attachment;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import java.util.List;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InstancioExtension.class)
public class HistoryItemTest {

  Model<HistoryItemData> model = Instancio.of(HistoryItemData.class).toModel();
  HistoryItemDataRepository repository = mock(HistoryItemDataRepository.class);
  HistoryFacade facade = mock(HistoryFacade.class);

  @Test
  public void testAddingAttachmentToNewItem(@Given Attachment attachment) {
    when(repository.existsById(anyString())).thenReturn(true);
    var data = Instancio.of(model)
      .set(field(HistoryItemData::attachments), List.of())
      .create();
    new HistoryItem(repository, data, facade).addMedia(attachment);
    verify(repository).update(
      argThat(it -> List.of(attachment).equals(it.attachments()))
    );
  }

  @Test
  public void testItemActionsDefaultToEmptyList() {
    var data = Instancio.of(model).create();
    var item = new HistoryItem(repository, data, facade);
    assertTrue(item.itemActions().isEmpty());
  }

}
