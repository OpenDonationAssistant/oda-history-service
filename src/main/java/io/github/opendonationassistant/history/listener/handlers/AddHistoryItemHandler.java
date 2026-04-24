package io.github.opendonationassistant.history.listener.handlers;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.events.goal.GoalFacade;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInDefaultGoalCommand;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInSpecifiedGoalCommand;
import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.HistoryFacade.HistoryMessagingClient;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.history.command.AddHistoryItemApi;
import io.github.opendonationassistant.history.command.AddHistoryItemApi.AddHistoryItemCommand;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

@Singleton
public class AddHistoryItemHandler
  extends AbstractMessageHandler<AddHistoryItemApi.AddHistoryItemCommand> {

  private ODALogger log = new ODALogger(this);
  private final HistoryMessagingClient messaging;
  private final HistoryItemRepository repository;
  private final HistoryFacade facade;
  private final ObjectMapper mapper;
  private final GoalFacade goalFacade;

  @Inject
  public AddHistoryItemHandler(
    ObjectMapper mapper,
    HistoryMessagingClient messaging,
    HistoryItemRepository repository,
    HistoryFacade facade,
    GoalFacade goalFacade
  ) {
    super(mapper);
    this.messaging = messaging;
    this.repository = repository;
    this.facade = facade;
    this.mapper = mapper;
    this.goalFacade = goalFacade;
  }

  @Override
  public void handle(AddHistoryItemCommand command) throws IOException {
    final var paymentId = command.paymentId();
    if (paymentId == null) {
      return;
    }
    if (repository.findByOriginId(paymentId).isPresent()) {
      return;
    }
    var data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      Optional.ofNullable(command.event()).orElse("payment"),
      command.recipientId(),
      command.system(),
      command.paymentId(),
      Optional.ofNullable(command.authorizationTimestamp()).orElseGet(() ->
        Instant.now()
      ),
      command.nickname(),
      command.amount(),
      command.message(),
      List.of(), // attachments
      List.of(), // goals
      List.of(), // reelResults
      List.of(), // actions
      null, // votes
      List.of(),
      command.level(),
      command.count(),
      command.levelName()
    );
    CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
    if (
      command.addToGoal() &&
      command.goals() != null &&
      command.goals().size() > 0
    ) {
      chain = chain.thenRunAsync(() ->
        goalFacade.run(
          new CountPaymentInSpecifiedGoalCommand(
            paymentId,
            command.recipientId(),
            command.goals().getFirst().goalId(),
            command.amount()
          )
        )
      );
    }
    if (
      command.addToGoal() &&
      (command.goals() == null || command.goals().size() == 0)
    ) {
      chain = chain.thenRunAsync(() ->
        goalFacade.run(
          new CountPaymentInDefaultGoalCommand(
            paymentId,
            command.recipientId(),
            command.amount()
          )
        )
      );
    }
    if (command.triggerDonaton()) {
      chain = chain.thenCompose(v ->
        facade.sendEvent(
          new ChangeDonatonCommand(
            command.recipientId(),
            command.amount(),
            paymentId
          )
        )
      );
    }
    if (command.triggerAlert()) {
      chain = chain.thenCompose(v ->
        facade.sendEvent(
          new CreateAlertCommand(
            paymentId,
            command.recipientId(),
            command.nickname(),
            command.message(),
            command.amount(),
            Optional.ofNullable(command.alertMedia())
              .map(it -> it.url())
              .orElse(null),
            command.system(),
            command.event(),
            command.count(),
            command.levelName()
          )
        )
      );
    }
    if (command.triggerReel()) {
      chain = chain.thenCompose(v ->
        facade.sendEvent(
          new LinkReelCommand(
            command.recipientId(),
            paymentId,
            command.amount()
          )
        )
      );
    }
    chain
      .thenRun(() -> repository.create(data))
      .thenCompose(v ->
        command.addToTop()
          ? sendEvent(
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
                .map(it ->
                  new HistoryItemEvent.Vote(it.id(), it.name(), it.isNew())
                )
                .orElse(null)
            )
          )
          : CompletableFuture.completedFuture(null)
      )
      .join();
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

  @Serdeable
  public static record LinkReelCommand(
    String recipientId,
    String paymentId,
    Amount amount
  )
    implements HasRecipientId {}

  @Serdeable
  public static record ChangeDonatonCommand(
    String recipientId,
    Amount change,
    String paymentId
  )
    implements HasRecipientId {}

  @Serdeable
  public static record CreateAlertCommand(
    String paymentId,
    String recipientId,
    String nickname,
    String message,
    Amount amount,
    @Nullable String url,
    String system,
    String event,
    @Nullable Integer count,
    @Nullable String levelName
  )
    implements HasRecipientId {}
}
