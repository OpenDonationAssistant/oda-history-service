package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.events.goal.GoalFacade;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInDefaultGoalCommand;
import io.github.opendonationassistant.events.goal.GoalFacade.CountPaymentInSpecifiedGoalCommand;
import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.HistoryFacade.HistoryMessagingClient;
import io.github.opendonationassistant.events.history.event.HistoryItemEvent;
import io.github.opendonationassistant.history.command.AddHistoryItemApi.AddHistoryItemCommand.AlertMedia;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

@Controller
public class AddHistoryItem
  extends BaseController
  implements AddHistoryItemApi {

  private final HistoryItemRepository repository;
  private final ODALogger log = new ODALogger(this);
  private final HistoryMessagingClient messaging;
  private final HistoryFacade facade;
  private final ObjectMapper mapper;
  private final GoalFacade goalFacade;

  @Inject
  public AddHistoryItem(
    HistoryItemRepository repository,
    HistoryMessagingClient messaging,
    HistoryFacade facade,
    GoalFacade goalFacade,
    ObjectMapper mapper
  ) {
    this.repository = repository;
    this.messaging = messaging;
    this.facade = facade;
    this.mapper = mapper;
    this.goalFacade = goalFacade;
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> oldAddHistoryItem(
    Authentication auth,
    AddHistoryItemCommand command
  ) {
    return addHistoryItem(auth, command);
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> addHistoryItem(
    Authentication auth,
    AddHistoryItemApi.AddHistoryItemCommand command
  ) {
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    final var paymentId = command.paymentId();
    if (paymentId == null) {
      return CompletableFuture.completedFuture(HttpResponse.badRequest());
    }
    if (repository.findByOriginId(paymentId).isPresent()){
      return CompletableFuture.completedFuture(HttpResponse.ok());
    }
    var data = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "payment",
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
      List.of()
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
              .map(AlertMedia::url)
              .orElse(null)
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
    return chain
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
      .thenApply(v -> HttpResponse.ok());
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
    @Nullable String url
  )
    implements HasRecipientId {}
}
