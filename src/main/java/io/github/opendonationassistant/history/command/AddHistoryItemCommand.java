package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.HistoryItem;
import io.github.opendonationassistant.HistoryItemData;
import io.github.opendonationassistant.HistoryItemRepository;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.PaymentNotificationSender;
import io.github.opendonationassistant.events.PaymentSender;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Optional;

@Serdeable
public class AddHistoryItemCommand extends HistoryItemData {

  private Boolean triggerAlert;
  private Boolean triggerReel;
  private Boolean addToGoal;
  private Boolean addToTop;

  public void execute(
    HistoryItemRepository repository,
    PaymentNotificationSender paymentSender,
    PaymentSender alertSender
  ) {
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
    if (addToGoal) {
      paymentSender.sendToGoals(notification);
    }
    if (addToTop) {
      paymentSender.sendToContributions(notification);
    }
    if (triggerAlert){
      alertSender.send("%salerts".formatted(getRecipientId()), notification);
    }
  }
}
