package io.github.opendonationassistant.history.model;

import static org.instancio.Select.*;
import static org.mockito.Mockito.*;

import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemData.Attachment;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.List;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class HistoryItemTest {

  Model<HistoryItemData> model = Instancio.of(HistoryItemData.class).toModel();
  HistoryItemDataRepository repository = mock(HistoryItemDataRepository.class);

  @Test
  public void testAddingAttachmentToNewItem(@Given Attachment attachment) {
    var data = Instancio.of(model)
      .set(field(HistoryItemData::attachments), List.of())
      .create();
    new HistoryItem(repository, data).addMedia(attachment);
    verify(repository).update(
      argThat(it -> List.of(attachment).equals(it.attachments()))
    );
  }
}
