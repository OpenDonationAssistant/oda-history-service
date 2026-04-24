package io.github.opendonationassistant.history.command;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

public interface AddHistoryItemApi {
  @Post("/history/commands/add-item")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Add a new history item",
    description = "Creates a new donation history item for the authenticated user"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully created the history item"
  )
  CompletableFuture<HttpResponse<Void>> addHistoryItem(
    Authentication auth,
    @Body AddHistoryItemApi.AddHistoryItemCommand command
  );

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
  CompletableFuture<HttpResponse<Void>> oldAddHistoryItem(
    Authentication auth,
    @Body AddHistoryItemApi.AddHistoryItemCommand command
  );

  @Serdeable
  @Schema(description = "Command to add a new history item")
  public record AddHistoryItemCommand(
    @Nullable @Schema(
      description = "Payment ID from the source system"
    ) String paymentId,
    @Schema(description = "Donor's nickname") String nickname,
    @Schema(description = "Recipient ID (streamer/creator)") String recipientId,
    @Schema(description = "Donation amount") Amount amount,
    @Schema(description = "Message from the donor") String message,
    @Schema(
      description = "Authorization timestamp"
    ) Instant authorizationTimestamp,
    @Schema(description = "Source system name") String system,
    @Nullable @Schema(
      description = "External ID from the source system"
    ) String externalId,
    @Schema(description = "Attached media files") List<Attachment> attachments,
    @Schema(description = "Target goals") List<TargetGoal> goals,
    @Schema(description = "Reel results from social media") List<
      ReelResult
    > reelResults,
    @Schema(description = "Action requests triggered by the donation") List<
      ActionRequest
    > actions,
    @Nullable @Schema(description = "Alert media URL") AlertMedia alertMedia,
    @Nullable @Schema(description = "Vote information") Vote vote,
    @Schema(description = "Event type") String event,
    @Nullable @Schema(description = "Related item count") Integer count,
    @Nullable @Schema(description = "Boosty subscription level") Integer level,
    @Nullable @Schema(description = "Boosty subscription level name") String levelName,
    @Schema(description = "Whether to trigger an alert") boolean triggerAlert,
    @Schema(description = "Whether to trigger a reel") boolean triggerReel,
    @Schema(
      description = "Whether to trigger donation processing"
    ) boolean triggerDonaton,
    @Schema(description = "Whether to add to goal") boolean addToGoal,
    @Schema(description = "Whether to add to top") boolean addToTop
  ) {
    @Serdeable
    @Schema(description = "Media attachment")
    public record Attachment(@Schema(description = "Attachment ID") String id) {}

    @Serdeable
    @Schema(description = "Reel result from social media")
    public record ReelResult(
      @Schema(description = "Title of the reel") String title
    ) {}

    @Serdeable
    @Schema(description = "Target goal")
    public record TargetGoal(
      @Schema(description = "Goal ID") String goalId,
      @Schema(description = "Goal title") String goalTitle
    ) {}

    @Serdeable
    @Schema(description = "Action request triggered by donation")
    record ActionRequest(
      @Schema(description = "Action request ID") String id,
      @Schema(description = "Action ID") String actionId,
      @Schema(description = "Action name") String name,
      @Schema(description = "Amount required for the action") Integer amount,
      @Schema(description = "Action payload data") Map<String, Object> payload
    ) {}

    @Serdeable
    @Schema(description = "Alert media")
    public record AlertMedia(@Schema(description = "Alert media URL") String url) {}

    @Serdeable
    @Schema(description = "Vote information")
    public record Vote(
      @Nullable @Schema(description = "Vote ID") String id,
      @Schema(description = "Vote name") String name,
      @Schema(description = "Is this a new vote?") Boolean isNew
    ) {}
  }
}
