package io.github.opendonationassistant;

import com.fasterxml.uuid.Generators;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import java.util.ArrayList;
import java.util.Optional;

@Serdeable
@MappedEntity("history")
public class HistoryItem extends HistoryItemData {

  public void save(HistoryItemRepository repository) {
    if (StringUtils.isEmpty(getId())) {
      setId(Generators.timeBasedEpochGenerator().generate().toString());
    }
    Optional<HistoryItem> existing = repository.findById(getId());
    existing.ifPresentOrElse(
      old -> {
        repository.update(this);
      },
      () -> repository.save(this)
    );
  }

  public HistoryItem merge(HistoryItemData data) {
    if (data == null) {
      return this;
    }
    var updated = new HistoryItem();
    updated.setId(getId());
    if (data.getId() != null) {
      updated.setId(data.getId());
    }
    updated.setAmount(getAmount());
    if (data.getAmount() != null) {
      updated.setAmount(data.getAmount());
    }
    updated.setMessage(getMessage());
    if (data.getMessage() != null) {
      updated.setMessage(data.getMessage());
    }
    updated.setNickname(getNickname());
    if (data.getNickname() != null) {
      updated.setNickname(data.getNickname());
    }
    updated.setPaymentId(getPaymentId());
    if (data.getPaymentId() != null) {
      updated.setPaymentId(data.getPaymentId());
    }
    updated.setRecipientId(getRecipientId());
    if (data.getRecipientId() != null) {
      updated.setRecipientId(data.getRecipientId());
    }
    updated.setAuthorizationTimestamp(getAuthorizationTimestamp());
    if (data.getAuthorizationTimestamp() != null) {
      updated.setAuthorizationTimestamp(data.getAuthorizationTimestamp());
    }

    var updatedGoals = new ArrayList<>(getGoals());
    if (data.getGoals() != null) {
      updatedGoals.addAll(data.getGoals());
    }
    updated.setGoals(updatedGoals);

    var updatedAttachments = new ArrayList<>(getAttachments());
    if (data.getAttachments() != null) {
      updatedAttachments.addAll(data.getAttachments());
    }
    updated.setAttachments(updatedAttachments);

    var updatedReelResults = new ArrayList<>(getReelResults());
    if (data.getReelResults() != null) {
      updatedReelResults.addAll(data.getReelResults());
    }
    updated.setReelResults(updatedReelResults);

    return updated;
  }
}
