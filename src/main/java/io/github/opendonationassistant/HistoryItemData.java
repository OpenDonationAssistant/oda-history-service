package io.github.opendonationassistant;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Serdeable
@MappedEntity("history")
public class HistoryItemData {

  @Id
  private String id;

  private String paymentId;
  private String nickname;
  private String recipientId;
  private Amount amount;
  private String message;
  private Instant authorizationTimestamp;

  private List<Attachment> attachments = new ArrayList<>();
  private List<TargetGoal> goals = new ArrayList<>();
  private List<ReelResult> reelResults = new ArrayList<>();

  public CompletedPaymentNotification makeNotification() {
    CompletedPaymentNotification notification =
      new CompletedPaymentNotification();
    goals
      .stream()
      .findFirst()
      .map(TargetGoal::getGoalId)
      .ifPresent(notification::setGoal);
    notification.setAmount(amount);
    notification.setFailed(false);
    notification.setMessage(message);
    notification.setNickname(nickname);
    notification.setRecipientId(recipientId);
    notification.setAttachments(
      attachments.stream().map(Attachment::getId).toList()
    );
    return notification;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public Amount getAmount() {
    return amount;
  }

  public void setAmount(Amount amount) {
    this.amount = amount;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @MappedProperty(type = DataType.JSON)
  public List<Attachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<Attachment> attachments) {
    this.attachments = attachments;
  }

  @MappedProperty(type = DataType.JSON)
  public List<TargetGoal> getGoals() {
    return goals;
  }

  public void setGoals(List<TargetGoal> goals) {
    this.goals = goals;
  }

  @MappedProperty(type = DataType.JSON)
  public List<ReelResult> getReelResults() {
    return reelResults;
  }

  public void setReelResults(List<ReelResult> reelResults) {
    this.reelResults = reelResults;
  }

  public Instant getAuthorizationTimestamp() {
    return authorizationTimestamp;
  }

  public void setAuthorizationTimestamp(Instant authorizationTimestamp) {
    this.authorizationTimestamp = authorizationTimestamp;
  }

  @Override
  public String toString() {
    return (
      "{\"_type\"=\"HistoryItemData\",\"id\"=\"" +
      id +
      "\", paymentId\"=\"" +
      paymentId +
      "\", nickname\"=\"" +
      nickname +
      "\", recipientId\"=\"" +
      recipientId +
      "\", amount\"=\"" +
      amount +
      "\", message\"=\"" +
      message +
      "\", authorizationTimestamp\"=\"" +
      authorizationTimestamp +
      "\", attachments\"=\"" +
      attachments +
      "\", goals\"=\"" +
      goals +
      "\", reelResults\"=\"" +
      reelResults +
      "}"
    );
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((paymentId == null) ? 0 : paymentId.hashCode());
    result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
    result =
      prime * result + ((recipientId == null) ? 0 : recipientId.hashCode());
    result = prime * result + ((amount == null) ? 0 : amount.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result =
      prime * result +
      ((authorizationTimestamp == null)
          ? 0
          : authorizationTimestamp.hashCode());
    result =
      prime * result + ((attachments == null) ? 0 : attachments.hashCode());
    result = prime * result + ((goals == null) ? 0 : goals.hashCode());
    result =
      prime * result + ((reelResults == null) ? 0 : reelResults.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    HistoryItemData other = (HistoryItemData) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    if (paymentId == null) {
      if (other.paymentId != null) return false;
    } else if (!paymentId.equals(other.paymentId)) return false;
    if (nickname == null) {
      if (other.nickname != null) return false;
    } else if (!nickname.equals(other.nickname)) return false;
    if (recipientId == null) {
      if (other.recipientId != null) return false;
    } else if (!recipientId.equals(other.recipientId)) return false;
    if (amount == null) {
      if (other.amount != null) return false;
    } else if (!amount.equals(other.amount)) return false;
    if (message == null) {
      if (other.message != null) return false;
    } else if (!message.equals(other.message)) return false;
    if (authorizationTimestamp == null) {
      if (other.authorizationTimestamp != null) return false;
    } else if (
      !authorizationTimestamp.equals(other.authorizationTimestamp)
    ) return false;
    if (attachments == null) {
      if (other.attachments != null) return false;
    } else if (!attachments.equals(other.attachments)) return false;
    if (goals == null) {
      if (other.goals != null) return false;
    } else if (!goals.equals(other.goals)) return false;
    if (reelResults == null) {
      if (other.reelResults != null) return false;
    } else if (!reelResults.equals(other.reelResults)) return false;
    return true;
  }
}
