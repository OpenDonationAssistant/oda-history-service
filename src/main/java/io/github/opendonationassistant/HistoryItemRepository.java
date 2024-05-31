package io.github.opendonationassistant;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface HistoryItemRepository
  extends CrudRepository<HistoryItem, String> {
  public List<HistoryItem> findByRecipientId(String recipientId);
}
