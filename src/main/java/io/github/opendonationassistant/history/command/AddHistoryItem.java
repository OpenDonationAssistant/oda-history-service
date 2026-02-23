package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

@Controller
public class AddHistoryItem extends BaseController {

  private final HistoryItemRepository repository;
  private final ODALogger log = new ODALogger(this);

  @Inject
  public AddHistoryItem(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Post("/history/add")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<Void> addHistoryItem(Authentication auth, @Body AddHistoryItemCommand command) {
    log.debug("AddHistoryItemCommand Authentication", Map.of("auth", auth));
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }
    var created = new HistoryItemData(
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
      List.of(), // command.attachments(),
      List.of(), // command.goals(),
      // command
      //   .goals()
      //   .stream()
      //   .map(it -> new HistoryItemData.TargetGoal(it.goalId(), it.goalTitle()))
      //   .toList(),
      // command
      //   .reelResults()
      //   .stream()
      //   .map(it -> new HistoryItemData.ReelResult(it.title()))
      //   .toList(),
      List.of(), // command.reelResults(),
      List.of(), // command.actions(),
      null // command.vote()
    );
    return CompletableFuture.runAsync(() -> repository.create(created));
  }

  @Serdeable
  public static record AddHistoryItemCommand(
    @Nullable String paymentId,
    String nickname,
    String recipientId,
    Amount amount,
    String message,
    Instant authorizationTimestamp,
    String system,
    @Nullable String externalId,
    List<Attachment> attachments,
    List<TargetGoal> goals,
    List<ReelResult> reelResults,
    List<ActionRequest> actions,
    @Nullable AlertMedia alertMedia,
    @Nullable Vote vote,
    String event,
    boolean triggerAlert,
    boolean triggerReel,
    boolean triggerDonaton,
    boolean addToGoal,
    boolean addToTop
  ) {
    @Serdeable
    public record Attachment(String id) {}

    @Serdeable
    public record ReelResult(String title) {}

    @Serdeable
    public record TargetGoal(String goalId, String goalTitle) {}

    @Serdeable
    public static record ActionRequest(
      String id,
      String actionId,
      String name,
      Integer amount,
      Map<String, Object> payload
    ) {}

    @Serdeable
    public static record AlertMedia(String url) {}

    @Serdeable
    public static record Vote(
      @Nullable String id,
      String name,
      Boolean isNew
    ) {}
  }
}
