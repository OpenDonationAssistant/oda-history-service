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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
  @Operation(
    summary = "Add a new history item",
    description = "Creates a new donation history item for the authenticated user"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully created the history item"
  )
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
      null, // command.vote(),
      List.of()
    );
    return CompletableFuture.runAsync(() -> repository.create(created));
  }

  @Serdeable
  @Schema(description = "Command to add a new history item")
  public static record AddHistoryItemCommand(
    @Nullable @Schema(description = "Payment ID from the source system") String paymentId,
    @Schema(description = "Donor's nickname") String nickname,
    @Schema(description = "Recipient ID (streamer/creator)") String recipientId,
    @Schema(description = "Donation amount") Amount amount,
    @Schema(description = "Message from the donor") String message,
    @Schema(description = "Authorization timestamp") Instant authorizationTimestamp,
    @Schema(description = "Source system name") String system,
    @Nullable @Schema(description = "External ID from the source system") String externalId,
    @Schema(description = "Attached media files") List<Attachment> attachments,
    @Schema(description = "Target goals") List<TargetGoal> goals,
    @Schema(description = "Reel results from social media") List<ReelResult> reelResults,
    @Schema(description = "Action requests triggered by the donation") List<ActionRequest> actions,
    @Nullable @Schema(description = "Alert media URL") AlertMedia alertMedia,
    @Nullable @Schema(description = "Vote information") Vote vote,
    @Schema(description = "Event type") String event,
    @Schema(description = "Whether to trigger an alert") boolean triggerAlert,
    @Schema(description = "Whether to trigger a reel") boolean triggerReel,
    @Schema(description = "Whether to trigger donation processing") boolean triggerDonaton,
    @Schema(description = "Whether to add to goal") boolean addToGoal,
    @Schema(description = "Whether to add to top") boolean addToTop
  ) {
    @Serdeable
    @Schema(description = "Media attachment")
    public record Attachment(@Schema(description = "Attachment ID") String id) {}

    @Serdeable
    @Schema(description = "Reel result from social media")
    public record ReelResult(@Schema(description = "Title of the reel") String title) {}

    @Serdeable
    @Schema(description = "Target goal")
    public record TargetGoal(
      @Schema(description = "Goal ID") String goalId,
      @Schema(description = "Goal title") String goalTitle
    ) {}

    @Serdeable
    @Schema(description = "Action request triggered by donation")
    public static record ActionRequest(
      @Schema(description = "Action request ID") String id,
      @Schema(description = "Action ID") String actionId,
      @Schema(description = "Action name") String name,
      @Schema(description = "Amount required for the action") Integer amount,
      @Schema(description = "Action payload data") Map<String, Object> payload
    ) {}

    @Serdeable
    @Schema(description = "Alert media")
    public static record AlertMedia(@Schema(description = "Alert media URL") String url) {}

    @Serdeable
    @Schema(description = "Vote information")
    public static record Vote(
      @Nullable @Schema(description = "Vote ID") String id,
      @Schema(description = "Vote name") String name,
      @Schema(description = "Is this a new vote?") Boolean isNew
    ) {}
  }
}
