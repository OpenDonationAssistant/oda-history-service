package io.github.opendonationassistant.payment;

import io.github.opendonationassistant.action.ActionDataRepository;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.payments.PaymentEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RabbitListener
public class PaymentListener {

  private final ODALogger log = new ODALogger(this);
  private final HistoryItemRepository repository;
  private final ActionDataRepository actionRepository;

  @Inject
  public PaymentListener(
    HistoryItemRepository repository,
    ActionDataRepository actionRepository
  ) {
    this.repository = repository;
    this.actionRepository = actionRepository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.History.EVENTS)
  public void listen(@MessageHeader("type") String type, PaymentEvent payment) {
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
        // Optional.ofNullable(payment.goal()).map(List::of).orElse(List.of()),
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
        Optional.ofNullable(payment.vote())
          .map(it -> new HistoryItemData.Vote(it.id(), it.name(), it.isNew()))
          .orElse(null)
      )
    );
  }
}
