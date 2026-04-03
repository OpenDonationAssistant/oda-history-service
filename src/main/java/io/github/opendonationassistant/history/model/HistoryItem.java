package io.github.opendonationassistant.history.model;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemData.Attachment;
import io.github.opendonationassistant.history.repository.HistoryItemData.ReelResult;
import io.github.opendonationassistant.history.repository.HistoryItemData.TargetGoal;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.micronaut.serde.annotation.Serdeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Serdeable
public class HistoryItem {

  private ODALogger log = new ODALogger(this);
  private HistoryItemData data;
  private HistoryItemDataRepository repository;

  public HistoryItem(
    HistoryItemDataRepository repository,
    HistoryItemData data
  ) {
    this.repository = repository;
    this.data = data;
  }

  public HistoryItemData data() {
    return this.data;
  }

  public void addActions(List<HistoryItemData.ActionRequest> actions) {
    var mergedActions = new ArrayList(data.actions());
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

  public void save() {
    log.info("Updating history item", Map.of("data", data));
    repository.update(data);
  }

}
