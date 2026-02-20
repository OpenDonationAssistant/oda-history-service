package io.github.opendonationassistant.history.repository;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.micronaut.context.annotation.Mapper;
import io.micronaut.context.annotation.Mapper.Mapping;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class HistoryItemRepository {

  private final HistoryItemDataRepository repository;
  private final HistoryFacade facade;

  @Inject
  public HistoryItemRepository(
    HistoryItemDataRepository repository,
    HistoryFacade facade
  ) {
    this.repository = repository;
    this.facade = facade;
  }

  public Page<HistoryItem> findByRecipientId(
    String recipientId,
    Pageable pageable
  ) {
    return repository
      .findByRecipientId(recipientId, pageable)
      .map(this::convert);
  }

  public HistoryItem create(HistoryItemData data) {
    repository.save(data);
    return convert(data);
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

  public Optional<HistoryItem> findByOriginId(String originId) {
    return repository.findByOriginId(originId).map(this::convert);
  }

  private HistoryItem convert(HistoryItemData data) {
    return new HistoryItem(repository, data);
  }

  public static interface HistoryItemDataToEventMapper {
    @Mapper
    HistoryItemEvent map(HistoryItemData data);
  }
}
