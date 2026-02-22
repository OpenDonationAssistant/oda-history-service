package io.github.opendonationassistant.history.repository;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Serdeable
@MappedEntity("history")
public record HistoryItemData(
  @Id String id,
  @MappedProperty("event_type") String type,
  String recipientId,
  String system,
  @Nullable String originId,
  @MappedProperty(type = DataType.TIMESTAMP, value = "event_timestamp") Instant timestamp,
  @Nullable String nickname,
  @Nullable Amount amount,
  @Nullable String message,
  @MappedProperty(type = DataType.JSON) List<Attachment> attachments,
  @MappedProperty(type = DataType.JSON) List<TargetGoal> goals,
  @MappedProperty(type = DataType.JSON) List<ReelResult> reelResults,
  @MappedProperty(type = DataType.JSON) List<ActionRequest> actions,
  @MappedProperty(type = DataType.JSON) @Nullable Vote vote
) {
  @Serdeable
  public record Attachment(
    String id,
    String url,
    String title,
    String thumbnail
  ) {}

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
  public static record Vote(@Nullable String id, String name, Boolean isNew) {}
}
