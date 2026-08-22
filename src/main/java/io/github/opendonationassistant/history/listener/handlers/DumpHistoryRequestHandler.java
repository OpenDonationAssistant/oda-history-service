package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent.Vote;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemData.ActionRequest;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@RabbitListener(executor = "command-listener")
public class DumpHistoryRequestHandler {

  private final ODALogger log = new ODALogger(this);
  public static final String QUEUE_NAME = "history.dump";

  private final HistoryItemDataRepository repository;
  private final HistoryFacade facade;

  @Inject
  public DumpHistoryRequestHandler(
    HistoryItemDataRepository repository,
    HistoryFacade facade
  ) {
    this.repository = repository;
    this.facade = facade;
  }

  @Serdeable
  public static record DumpHistoryRequest(String recipientId, Instant from) {}

  @Serdeable
  public static record DumpHistoryResponse(int count) {}

  @Queue(QUEUE_NAME)
  public DumpHistoryResponse handle(DumpHistoryRequest request) {
    var items =
      repository.findByRecipientIdAndTimestampGreaterThanEqualOrderByTimestampAsc(
        request.recipientId(),
        request.from()
      );
    items.forEach(item -> facade.sendEvent(toEvent(item)).join());
    log.info(
      "Dumped history items",
      Map.of(
        "recipientId",
        request.recipientId(),
        "from",
        request.from(),
        "count",
        items.size()
      )
    );
    return new DumpHistoryResponse(items.size());
  }

  private HistoryItemEvent toEvent(HistoryItemData data) {
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
    return Optional
      .ofNullable(data.vote())
      .map(it -> new HistoryItemEvent.Vote(it.id(), it.name(), it.isNew()))
      .orElse(null);
  }
}
