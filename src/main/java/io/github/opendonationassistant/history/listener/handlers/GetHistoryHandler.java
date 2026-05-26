package io.github.opendonationassistant.history.listener.handlers;

import static io.micronaut.data.repository.jpa.criteria.PredicateSpecification.*;

import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent.Vote;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemData.ActionRequest;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@Singleton
public class GetHistoryHandler {

  private final HistoryItemRepository repository;

  @Inject
  public GetHistoryHandler(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Serdeable
  public static record GetHistoryCommand(
    String recipientId,
    Pageable pageable,
    @Nullable List<String> systems,
    @Nullable List<String> events,
    @Nullable Instant after,
    @Nullable Instant before
  ) {}

  public Page<HistoryItemEvent> handle(GetHistoryCommand command) {
    final ArrayList<PredicateSpecification<HistoryItemData>> conditions =
      new ArrayList<>();
    conditions.add(
      where((root, builder) ->
        builder.equal(root.get("recipientId"), command.recipientId())
      )
    );
    if (command.systems() != null && !command.systems().isEmpty()) {
      conditions.add(
        where((root, builder) -> root.get("system").in(command.systems()))
      );
    }
    if (command.events() != null && !command.events().isEmpty()) {
      conditions.add(
        where((root, builder) -> root.get("type").in(command.events()))
      );
    }
    if (command.after() != null) {
      conditions.add(
        where((root, builder) ->
          builder.greaterThan(root.get("timestamp"), command.after())
        )
      );
    }
    if (command.before() != null) {
      conditions.add(
        where((root, builder) ->
          builder.lessThan(root.get("timestamp"), command.before())
        )
      );
    }
    return repository
      .findAll(conditions, command.pageable())
      .map(this::toEvent);
  }

  private HistoryItemEvent toEvent(HistoryItem item) {
    var data = item.data();
    return new HistoryItemEvent(
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
      data.actions().stream().map(this::toActionRequest).toList(),
      toVote(data)
    );
  }

  private HistoryItemEvent.ActionRequest toActionRequest(ActionRequest a) {
    return new HistoryItemEvent.ActionRequest(
      a.id(),
      a.actionId(),
      a.name(),
      a.amount(),
      a.payload()
    );
  }

  private @Nullable Vote toVote(HistoryItemData data) {
    return Optional.ofNullable(data.vote())
      .map(it -> new HistoryItemEvent.Vote(it.id(), it.name(), it.isNew()))
      .orElse(null);
  }
}
