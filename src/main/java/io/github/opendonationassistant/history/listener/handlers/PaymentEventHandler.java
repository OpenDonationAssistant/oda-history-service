package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.action.ActionDataRepository;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.MessageHandler;
import io.github.opendonationassistant.events.payments.PaymentEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaymentEventHandler implements MessageHandler {

  private final ODALogger log = new ODALogger(this);
  private final HistoryItemRepository repository;
  private final ActionDataRepository actionRepository;
  private final ObjectMapper objectMapper;

  @Inject
  public PaymentEventHandler(
    HistoryItemRepository repository,
    ObjectMapper mapper,
    ActionDataRepository actionRepository
  ) {
    this.repository = repository;
    this.objectMapper = mapper;
    this.actionRepository = actionRepository;
  }

  @Override
  public void handle(byte[] message) throws IOException {
    final var payment = objectMapper.readValue(message, PaymentEvent.class);
    if (payment == null) {
      return;
    }
    log.debug("Received PaymentEvent", Map.of("payment", payment));
    repository.create(
      new HistoryItemData(
        payment.id(),
        "payment",
        payment.recipientId(),
        "ODA",
        payment.id(),
        payment.authorizationTimestamp(),
        payment.nickname(),
        payment.amount(),
        payment.message(),
        List.of(), //attachments
        List.of(), //goals
        List.of(), //reelResults
        payment
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
        null // vote
      )
    );
  }

  @Override
  public String type() {
    return "PaymentEvent";
  }
}
