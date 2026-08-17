package io.github.opendonationassistant.history.model;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.events.history.event.DeletedHistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemData.Attachment;
import io.github.opendonationassistant.history.repository.HistoryItemData.ReelResult;
import io.github.opendonationassistant.history.repository.HistoryItemData.TargetGoal;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.micronaut.serde.annotation.Serdeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Serdeable
public class HistoryItem {

  private ODALogger log = new ODALogger(this);
  private HistoryItemData data;
  private HistoryItemDataRepository repository;
  private HistoryFacade facade;
  private List<ItemAction> itemActions;

  public HistoryItem(
    HistoryItemDataRepository repository,
    HistoryItemData data,
    HistoryFacade facade
  ) {
    this.repository = repository;
    this.data = data;
    this.facade = facade;
    this.itemActions = List.of();
  }

  public HistoryItemData data() {
    return this.data;
  }

  @Serdeable
  public record ItemAction(String id, String code, String name) {}

  public List<ItemAction> itemActions() {
    return this.itemActions;
  }

  public CompletableFuture<Void> executeItemAction(String itemActionId) {
    return CompletableFuture.completedFuture(null);
  }

  public void addActions(List<HistoryItemData.ActionRequest> actions) {
    var mergedActions = new ArrayList<>(data.actions());
    mergedActions.addAll(actions);
    data = data.withActions(mergedActions);
    save();
  }

  public void addGoal(TargetGoal goal) {
    var updatedGoals = new ArrayList<>(data.goals());
    updatedGoals.add(goal);
    data = data.withGoals(updatedGoals);
    save();
  }

  public void addMedia(Attachment attachment) {
    var updatedAttachments = new ArrayList<>(data.attachments());
    updatedAttachments.add(attachment);
    data = data.withAttachments(updatedAttachments);
    save();
  }

  public void addReelResult(ReelResult reelResult) {
    var updatedReelResults = new ArrayList<>(data.reelResults());
    updatedReelResults.add(reelResult);
    data = data.withReelResults(updatedReelResults);
    save();
  }

  public void markDeleted() {
    data = data.withDeleted(true);
    save();
    facade.sendEvent(
      new DeletedHistoryItem(
        data.id(),
        data.recipientId(),
        data.system(),
        data.originId(),
        data
          .goals()
          .stream()
          .map(it -> new DeletedHistoryItem.Goal(it.goalId()))
          .toList()
      )
    );
  }

  public void save() {
    log.info("Updating HistoryItemData", Map.of("data", data));
    if (repository.existsById(data.id())) {
      repository.update(data);
    } else {
      repository.save(data);
    }
  }
}
