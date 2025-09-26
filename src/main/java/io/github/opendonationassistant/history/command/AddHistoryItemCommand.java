package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.HistoryItemData;
import io.github.opendonationassistant.events.history.HistoryCommand;
import io.github.opendonationassistant.events.history.HistoryCommandSender;
import io.github.opendonationassistant.events.history.ReelResult;
import io.github.opendonationassistant.events.history.TargetGoal;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Optional;

@Serdeable
public class AddHistoryItemCommand extends HistoryItemData {

  private boolean triggerAlert = false;
  private boolean triggerReel = false;
  private boolean triggerDonaton = false;
  private boolean addToGoal = false;
  private boolean addToTop = false;

  public void execute(HistoryCommandSender commandSender) {
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
          .map(it -> new TargetGoal(it.goalId(), it.goalTitle()))
          .toList(),
        getReelResults().stream().map(it -> new ReelResult(it.title())).toList()
      );
    commandSender.send(
      new HistoryCommand(
        "create",
        created,
        triggerAlert,
        triggerReel,
        triggerDonaton,
        addToTop,
        created.goals().size() > 0 || addToGoal
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

  public void setTriggerDonaton(Boolean triggerDonaton) {
    this.triggerDonaton = triggerDonaton;
  }
}
