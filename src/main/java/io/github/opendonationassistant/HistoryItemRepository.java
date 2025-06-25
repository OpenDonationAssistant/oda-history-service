package io.github.opendonationassistant;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface HistoryItemRepository
  extends CrudRepository<HistoryItem, String> {
  public Page<HistoryItem> findByRecipientIdOrderByAuthorizationTimestampDesc(
    String recipientId,
    Pageable pageable
  );

  public Optional<HistoryItem> findByPaymentId(String paymentId);
}
