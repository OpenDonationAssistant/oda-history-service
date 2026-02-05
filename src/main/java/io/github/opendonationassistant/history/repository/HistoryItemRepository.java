package io.github.opendonationassistant.history.repository;

import io.github.opendonationassistant.history.model.HistoryItem;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class HistoryItemRepository {

  private final HistoryItemDataRepository repository;

  @Inject
  public HistoryItemRepository(HistoryItemDataRepository repository) {
    this.repository = repository;
  }

  public Page<HistoryItem> findByRecipientId(
    String recipientId,
    Pageable pageable
  ) {
    return repository
      .findByRecipientId(recipientId, pageable)
      .map(this::convert);
  }

  public Page<HistoryItem> findByRecipientIdAndSystemIn(
    String recipientId,
    List<String> system,
    Pageable pageable
  ) {
    return repository
      .findByRecipientIdAndSystemIn(recipientId, system, pageable)
      .map(this::convert);
  }

  public Optional<HistoryItem> findByPaymentId(String paymentId) {
    return repository.findByPaymentId(paymentId).map(this::convert);
  }

  public Optional<HistoryItem> findByExternalId(String externalId) {
    return repository.findByExternalId(externalId).map(this::convert);
  }

  private HistoryItem convert(HistoryItemData data) {
    return new HistoryItem(repository, data);
  }
}
