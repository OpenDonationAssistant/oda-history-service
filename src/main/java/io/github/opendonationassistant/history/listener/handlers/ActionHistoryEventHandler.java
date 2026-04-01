package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.actions.ActionHistoryEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ActionHistoryEventHandler
  extends AbstractMessageHandler<ActionHistoryEvent> {

  private final HistoryItemRepository repository;

  public ActionHistoryEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Override
  public void handle(ActionHistoryEvent event) throws IOException {
    List<HistoryItemData.ActionRequest> actions = event
      .actions()
      .stream()
      .map(it ->
        new HistoryItemData.ActionRequest(
          it.id(),
          it.actionId(),
          it.name(),
          it.amount(),
          Map.of()
        )
      )
      .toList();
    Optional.ofNullable(event.originId())
      .flatMap(repository::findByOriginId)
      .ifPresent(historyItem -> historyItem.addActions(actions));
  }
}
