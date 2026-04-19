package io.github.opendonationassistant.history.repository;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.micronaut.context.annotation.Mapper;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

@Singleton
public class HistoryItemRepository {

  private final ODALogger log = new ODALogger(this);
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

  public Optional<HistoryItem> findById(@Nullable String historyItemId) {
    return Optional.ofNullable(historyItemId)
      .flatMap(id -> repository.findById(id))
      .map(this::convert);
  }

  public Page<HistoryItem> findByRecipientId(
    String recipientId,
    Pageable pageable
  ) {
    return repository
      .findByRecipientIdOrderByTimestampDesc(recipientId, pageable)
      .map(this::convert);
  }

  public CompletableFuture<HistoryItem> create(HistoryItemData data) {
    log.debug("Saving history item", Map.of("data", data));
    repository.save(data);
    return facade
      .sendEvent(
        new HistoryItemEvent(
          data.id(),
          data.type(),
          data.recipientId(),
          data.system(),
          data.originId(),
          data.timestamp(),
          data.nickname(),
          data.amount(),
          data.message(),
          data.goals().stream().map(it -> it.goalId()).toList(),
          data
            .actions()
            .stream()
            .map(it ->
              new HistoryItemEvent.ActionRequest(
                it.id(),
                it.actionId(),
                it.name(),
                it.amount(),
                it.payload()
              )
            )
            .toList(),
          Optional.ofNullable(data.vote())
            .map(it -> new HistoryItemEvent.Vote(it.id(), it.name(), it.isNew())
            )
            .orElse(null)
        )
      )
      .thenApply(it -> convert(data));
  }

  public Page<HistoryItem> findByRecipientIdAndSystemIn(
    String recipientId,
    List<String> system,
    Pageable pageable
  ) {
    return repository
      .findByRecipientIdAndSystemInOrderByTimestampDesc(
        recipientId,
        system,
        pageable
      )
      .map(this::convert);
  }

  public Optional<HistoryItem> findByOriginId(String originId) {
    return repository.findByOriginId(originId).map(this::convert);
  }

  public @NonNull Page<HistoryItem> findAll(
    List<PredicateSpecification<HistoryItemData>> predicates,
    Pageable pageable
  ) {
    return repository
      .findAll(
        (root, builder) -> {
          return builder.and(
            predicates
              .stream()
              .map(it -> it.toPredicate(root, builder))
              .toArray(Predicate[]::new)
          );
        },
        pageable
      )
      .map(this::convert);
  }

  private HistoryItem convert(HistoryItemData data) {
    return new HistoryItem(repository, data);
  }

  public static interface HistoryItemDataToEventMapper {
    @Mapper
    HistoryItemEvent map(HistoryItemData data);
  }
}
