package io.github.opendonationassistant.history.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface HistoryItemDataRepository
  extends CrudRepository<HistoryItemData, String> {
  public Page<HistoryItemData> findByRecipientId(
    String recipientId,
    Pageable pageable
  );

  public Page<HistoryItemData> findByRecipientIdAndSystemIn(
    String recipientId,
    List<String> system,
    Pageable pageable
  );

  public Optional<HistoryItemData> findByOriginId(String paymentId);
}
