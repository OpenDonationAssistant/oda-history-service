package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.events.history.HistoryCommand;
import io.github.opendonationassistant.events.history.HistoryCommandSender;
import io.github.opendonationassistant.events.history.ReelResult;
import io.github.opendonationassistant.events.history.TargetGoal;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

@Controller
public class AddHistoryItem {

  private final HistoryCommandSender commandSender;

  @Inject
  public AddHistoryItem(HistoryCommandSender commandSender) {
    this.commandSender = commandSender;
  }

  public void execute(@Body AddHistoryItemCommand command) {
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
        getReelResults()
          .stream()
          .map(it -> new ReelResult(it.title()))
          .toList(),
        List.of(),
        Optional.ofNullable(getAlertMedia())
          .map(it -> it.url())
          .map(url ->
            new io.github.opendonationassistant.events.history.HistoryItemData.AlertMedia(
              url
            )
          )
          .orElse(null)
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

  @Serdeable
  public static record AddHistoryItemCommand(
  String paymentId,
  String nickname,
  String recipientId,
  Amount amount,
  String message,
  Instant authorizationTimestamp,
  String system,
  @Nullable String externalId,
  List<Attachment> attachments,
  List<TargetGoal> goals,
  List<ReelResult> reelResults,
  List<ActionRequest> actions,
  @Nullable AlertMedia alertMedia,
  @Nullable Vote vote,
  String event,
    boolean triggerAlert,
    boolean triggerReel,
    boolean triggerDonaton,
    boolean addToGoal,
    boolean addToTop
  ) {

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

}
