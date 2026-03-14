package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.action.ActionDataRepository;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.payments.PaymentEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class PaymentEventHandler extends AbstractMessageHandler<PaymentEvent> {

  private final HistoryItemRepository repository;
  private final ActionDataRepository actionRepository;

  @Inject
  public PaymentEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository,
    ActionDataRepository actionRepository
  ) {
    super(mapper);
    this.repository = repository;
    this.actionRepository = actionRepository;
  }

  @Override
  public void handle(PaymentEvent event) throws IOException {
    repository.create(
      new HistoryItemData(
        event.id(),
        "payment",
        event.recipientId(),
        "ODA",
        event.id(),
        event.authorizationTimestamp(),
        event.nickname(),
        event.amount(),
        event.message(),
        List.of(), //attachments
        List.of(), //goals
        List.of(), //reelResults
        event
          .actions()
          .stream()
          .flatMap(it ->
            actionRepository
              .findById(it.actionId())
              .map(action ->
                new HistoryItemData.ActionRequest(
                  it.id(),
                  it.actionId(),
                  action.name(),
                  it.amount(),
                  it.payload()
                )
              )
              .stream()
          )
          .toList(),
        null, // vote
        List.of()
      )
    );
  }
}
