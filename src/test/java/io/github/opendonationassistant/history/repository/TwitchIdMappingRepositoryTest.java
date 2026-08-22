package io.github.opendonationassistant.history.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class TwitchIdMappingRepositoryTest {

  TwitchIdMappingDataRepository dataRepository = mock(
    TwitchIdMappingDataRepository.class
  );
  TwitchIdMappingRepository repository = new TwitchIdMappingRepository(
    dataRepository
  );

  @Test
  public void testLinkSavesNewMapping() {
    var tokenId = UUID.randomUUID();
    when(dataRepository.existsById("recipient-1")).thenReturn(false);
    var mapping = new TwitchIdMappingData("recipient-1", tokenId);
    when(dataRepository.save(any())).thenReturn(mapping);

    var result = repository.link("recipient-1", tokenId);

    assertEquals(mapping, result);
    verify(dataRepository).save(mapping);
  }

  @Test
  public void testLinkUpdatesExistingMapping() {
    when(dataRepository.existsById("recipient-1")).thenReturn(true);
    var tokenId = UUID.randomUUID();
    var mapping = new TwitchIdMappingData("recipient-1", tokenId);
    when(dataRepository.update(mapping)).thenReturn(mapping);

    var result = repository.link("recipient-1", tokenId);

    assertEquals(mapping, result);
    verify(dataRepository).update(mapping);
  }
}
