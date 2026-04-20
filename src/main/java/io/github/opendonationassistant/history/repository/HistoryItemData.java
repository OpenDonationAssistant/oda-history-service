package io.github.opendonationassistant.history.repository;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.sourcegen.annotations.Wither;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Serdeable
@MappedEntity("history")
@Wither
@Schema(description = "History item representing a donation or event")
public record HistoryItemData(
  @Id @Schema(description = "Unique identifier of the history item") String id,
  @MappedProperty("event_type")
  @Schema(description = "Type of event (e.g., payment, goal)")
  String type,
  @Schema(description = "Recipient ID (streamer/creator)") String recipientId,
  @Schema(description = "Source system (e.g., donate.stream, donatello)")
  String system,
  @Nullable
  @Schema(description = "Original ID from the source system")
  String originId,
  @MappedProperty(type = DataType.TIMESTAMP, value = "event_timestamp")
  @Schema(description = "Timestamp of the event")
  Instant timestamp,
  @Nullable @Schema(description = "Donor's nickname") String nickname,
  @Nullable @Schema(description = "Donation amount") Amount amount,
  @Nullable @Schema(description = "Message from the donor") String message,
  @MappedProperty(type = DataType.JSON)
  @Schema(description = "Attached media files")
  List<Attachment> attachments,
  @MappedProperty(type = DataType.JSON)
  @Schema(description = "Target goals associated with the donation")
  List<TargetGoal> goals,
  @MappedProperty(type = DataType.JSON)
  @Schema(description = "Reel results from Instagram/Facebook")
  List<ReelResult> reelResults,
  @MappedProperty(type = DataType.JSON)
  @Schema(description = "Action requests triggered by the donation")
  List<ActionRequest> actions,
  @MappedProperty(type = DataType.JSON)
  @Nullable
  @Schema(description = "Vote associated with the donation")
  Vote vote,
  @MappedProperty(type = DataType.JSON)
  @Schema(description = "Alerts shown to the streamer")
  List<Alert> alerts,
  @Nullable @MappedProperty("level") Integer level,
  @Nullable @MappedProperty("count") Integer count,
  @Nullable @MappedProperty("level_name") String levelName
)
  implements HistoryItemDataWither {
  @Serdeable
  @Schema(description = "Media attachment")
  public record Attachment(
    @Schema(description = "Attachment ID") String id,
    @Schema(description = "URL to the media") String url,
    @Schema(description = "Title of the media") String title,
    @Schema(description = "Thumbnail URL") String thumbnail
  ) {}

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
  public static record ActionRequest(
    @Schema(description = "Action request ID") String id,
    @Schema(description = "Action ID") String actionId,
    @Schema(description = "Action name") String name,
    @Schema(description = "Amount required for the action") Integer amount,
    @Schema(description = "Action payload data") Map<String, Object> payload
  ) {}

  @Serdeable
  @Schema(description = "Vote voting")
  public static record Vote(
    @Nullable @Schema(description = "Vote ID") String id,
    @Schema(description = "Vote name") String name,
    @Schema(description = "Is this a new vote?") Boolean isNew
  ) {}

  @Serdeable
  @Schema(description = "Alert shown to streamer")
  public static record Alert(
    @Schema(description = "Alert ID") String id,
    @Schema(description = "When the alert was shown") Instant shownAt
  ) {}
}
