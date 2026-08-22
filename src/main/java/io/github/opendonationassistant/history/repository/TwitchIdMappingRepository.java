package io.github.opendonationassistant.history.repository;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class TwitchIdMappingRepository {

  private final TwitchIdMappingDataRepository repository;

  @Inject
  public TwitchIdMappingRepository(TwitchIdMappingDataRepository repository) {
    this.repository = repository;
  }

  public Optional<TwitchIdMappingData> findByRecipientId(String recipientId) {
    return repository.findById(recipientId);
  }

  public TwitchIdMappingData link(String recipientId, UUID refreshTokenId) {
    var mapping = new TwitchIdMappingData(recipientId, refreshTokenId);
    if (repository.existsById(recipientId)) {
      return repository.update(mapping);
    }
    return repository.save(mapping);
  }
}
