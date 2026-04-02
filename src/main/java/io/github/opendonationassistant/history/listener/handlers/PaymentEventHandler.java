package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.events.history.HistoryFacade.HistoryMessagingClient;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.events.payments.PaymentEvent;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class PaymentEventHandler extends AbstractMessageHandler<PaymentEvent> {

  private ODALogger log = new ODALogger(this);
  private final HistoryItemRepository repository;
  private final HistoryMessagingClient messaging;
  private final ObjectMapper mapper;

  @Inject
  public PaymentEventHandler(
    ObjectMapper mapper,
    HistoryItemRepository repository,
    HistoryMessagingClient messaging
  ) {
    super(mapper);
    this.mapper = mapper;
    this.repository = repository;
    this.messaging = messaging;
  }

  @Override
  public void handle(PaymentEvent event) throws IOException {
    final HistoryItemData data = new HistoryItemData(
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
      List.of(), //actions
      null, // vote
      List.of()
    );
    repository.create(data);
    sendEvent(
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
          .map(it -> new HistoryItemEvent.Vote(it.id(), it.name(), it.isNew()))
          .orElse(null)
      )
    );
  }

  public CompletableFuture<Void> sendEvent(HasRecipientId payload) {
    this.log.info("Send HistoryEvent", Map.of("payload", payload));
    String type = payload.getClass().getSimpleName();

    try {
      return this.messaging.sendEvent(
          "recipient",
          type,
          payload.recipientId(),
          this.mapper.writeValueAsBytes(payload)
        );
    } catch (Exception var4) {
      this.log.error("Serialization error", Map.of("error", var4.getMessage()));
      throw new RuntimeException(var4);
    }
  }
}
