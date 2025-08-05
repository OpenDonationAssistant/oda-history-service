package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.HistoryItem;
import io.github.opendonationassistant.HistoryItemData;
import io.github.opendonationassistant.HistoryItemRepository;
import io.github.opendonationassistant.commons.ToString;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.PaymentNotificationSender;
import io.github.opendonationassistant.events.alerts.AlertSender;
import io.github.opendonationassistant.events.history.HistoryCommand;
import io.github.opendonationassistant.events.history.HistoryCommandSender;
import io.github.opendonationassistant.events.history.ReelResult;
import io.github.opendonationassistant.events.history.TargetGoal;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Serdeable
public class AddHistoryItemCommand extends HistoryItemData {

  private final ODALogger log = new ODALogger(this);

  private boolean triggerAlert = false;
  private boolean triggerReel = false;
  private boolean addToGoal = false;
  private boolean addToTop = false;

  public void execute(
    HistoryItemRepository repository,
    HistoryCommandSender commandSender
  ) {
    log.info("Executing AddHistoryItemCommand", Map.of("command", this));

    var created =
      new io.github.opendonationassistant.events.history.HistoryItemData(
        Generators.timeBasedEpochGenerator().generate().toString(),
        getPaymentId(),
        getNickname(),
        getNickname(),
        getRecipientId(),
        getAmount(),
        getMessage(),
        getMessage(),
        Optional.ofNullable(getSystem()).orElse("ODA"),
        getExternalId(),
        Optional.ofNullable(getAuthorizationTimestamp()).orElseGet(() ->
          Instant.now()
        ),
        getAttachments(),
        getGoals()
          .stream()
          .map(it -> new TargetGoal(it.getGoalId(), it.getGoalTitle()))
          .toList(),
        getReelResults()
          .stream()
          .map(it -> new ReelResult(it.getTitle()))
          .toList()
      );
    commandSender.send(
      new HistoryCommand(
        "create",
        created,
        triggerAlert,
        triggerReel,
        addToTop,
        addToGoal
      )
    );
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
    return ToString.asJson(this);
  }
}
