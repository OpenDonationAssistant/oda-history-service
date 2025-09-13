package io.github.opendonationassistant;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface HistoryItemRepository
  extends CrudRepository<HistoryItem, String> {
  public Page<HistoryItem> findByRecipientId(
    String recipientId,
    Pageable pageable
  );

  public Page<HistoryItem> findByRecipientIdAndSystemIn(
    String recipientId,
    List<String> system,
    Pageable pageable
  );

  public Optional<HistoryItem> findByPaymentId(String paymentId);

  public Optional<HistoryItem> findByExternalId(String externalId);
}
