package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.HistoryItem;
import io.github.opendonationassistant.HistoryItemData;
import io.github.opendonationassistant.HistoryItemRepository;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.PaymentNotificationSender;
import io.github.opendonationassistant.events.PaymentSender;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Serdeable
public class AddHistoryItemCommand extends HistoryItemData {

  private Logger log = LoggerFactory.getLogger(AddHistoryItemCommand.class);

  private boolean triggerAlert = false;
  private boolean triggerReel = false;
  private boolean addToGoal = false;
  private boolean addToTop = false;

  public void execute(
    HistoryItemRepository repository,
    PaymentNotificationSender paymentSender,
    PaymentSender alertSender
  ) {
    log.info("Executing:  {}", this);
    HistoryItem created = new HistoryItem();
    created.setId(Generators.timeBasedEpochGenerator().generate().toString());
    created.setAmount(getAmount());
    created.setMessage(getMessage());
    created.setNickname(getNickname());
    created.setPaymentId(getPaymentId());
    created.setGoals(getGoals());
    created.setRecipientId(getRecipientId());
    created.setAttachments(getAttachments());
    created.setReelResults(getReelResults());
    created.setAuthorizationTimestamp(
      Optional
        .ofNullable(getAuthorizationTimestamp())
        .orElseGet(() -> Instant.now())
    );
    repository.save(created);
    CompletedPaymentNotification notification = created.makeNotification();
    if (triggerReel) {
      paymentSender.sendToReel(notification);
    }
    if (getGoals() != null && getGoals().size() > 0) {
      paymentSender.sendToGoals(notification);
    }
    if (addToTop) {
      paymentSender.sendToContributions(notification);
    }
    if (triggerAlert) {
      alertSender.send("%salerts".formatted(getRecipientId()), notification);
    }
  }

  public Boolean getTriggerAlert() {
    return triggerAlert;
  }

  public void setTriggerAlert(Boolean triggerAlert) {
    this.triggerAlert = triggerAlert;
  }

  public Boolean getTriggerReel() {
    return triggerReel;
  }

  public void setTriggerReel(Boolean triggerReel) {
    this.triggerReel = triggerReel;
  }

  public Boolean getAddToGoal() {
    return addToGoal;
  }

  public void setAddToGoal(Boolean addToGoal) {
    this.addToGoal = addToGoal;
  }

  public Boolean getAddToTop() {
    return addToTop;
  }

  public void setAddToTop(Boolean addToTop) {
    this.addToTop = addToTop;
  }

  @Override
  public String toString() {
    try {
      return ObjectMapper.getDefault().writeValueAsString(this);
    } catch (Exception e) {
      return "Can't serialize as  json";
    }
  }
}
