package io.github.opendonationassistant.history.model;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.github.opendonationassistant.history.repository.TwitchIdMappingData;
import io.github.opendonationassistant.history.repository.TwitchIdMappingRepository;
import io.github.opendonationassistant.rabbit.RabbitClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

public class TwitchRaidHistoryItemTest {

  HistoryItemDataRepository dataRepository = mock(
    HistoryItemDataRepository.class
  );
  HistoryFacade facade = mock(HistoryFacade.class);
  RabbitClient commands = mock(RabbitClient.class);
  TwitchIdMappingRepository twitchIdMappingRepository = mock(
    TwitchIdMappingRepository.class
  );

  @Test
  public void testReturnsCreateShoutoutActionWhenMappingExists() {
    when(twitchIdMappingRepository.findByRecipientId("recipient-1"))
      .thenReturn(
        Optional.of(new TwitchIdMappingData("recipient-1", UUID.randomUUID()))
      );

    var actions = item().itemActions();

    assertEquals(1, actions.size());
    assertEquals("create-shoutout", actions.get(0).id());
  }

  @Test
  public void testReturnsNoActionsWhenMappingMissing() {
    when(twitchIdMappingRepository.findByRecipientId("recipient-1"))
      .thenReturn(Optional.empty());

    var actions = item().itemActions();

    assertTrue(actions.isEmpty());
  }

  private TwitchRaidHistoryItem item() {
    var data = Instancio
      .of(HistoryItemData.class)
      .set(field(HistoryItemData::recipientId), "recipient-1")
      .set(field(HistoryItemData::actions), List.of())
      .set(field(HistoryItemData::goals), List.of())
      .set(field(HistoryItemData::metadata), Map.of())
      .create();
    return new TwitchRaidHistoryItem(
      dataRepository,
      data,
      facade,
      commands,
      twitchIdMappingRepository
    );
  }
}
