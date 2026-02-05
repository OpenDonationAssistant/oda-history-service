package io.github.opendonationassistant.history.repository;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.payments.PaymentFacade;
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
  String paymentId,
  String nickname,
  String recipientId,
  Amount amount,
  String message,
  Instant authorizationTimestamp,
  String system,
  @Nullable String externalId,
  @MappedProperty(type = DataType.JSON) List<Attachment> attachments,
  @MappedProperty(type = DataType.JSON) List<TargetGoal> goals,
  @MappedProperty(type = DataType.JSON) List<ReelResult> reelResults,
  @MappedProperty(type = DataType.JSON) List<ActionRequest> actions,
  @Nullable AlertMedia alertMedia,
  @Nullable Vote vote,
  String event
) {
  public CompletedPaymentNotification makeNotification() {
    return new CompletedPaymentNotification(
      paymentId,
      nickname,
      nickname,
      message,
      message,
      recipientId,
      amount,
      attachments.stream().map(Attachment::id).toList(),
      goals.stream().findFirst().map(TargetGoal::goalId).orElse(""),
      authorizationTimestamp,
      system,
      actions
        .stream()
        .map(request ->
          new PaymentFacade.ActionRequest(
            request.id,
            request.actionId,
            request.amount(),
            request.payload
          )
        )
        .toList(),
      null
    );
  }

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
