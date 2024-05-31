package io.github.opendonationassistant.history.query;

import java.util.List;
import java.util.Optional;

import io.github.opendonationassistant.Amount;
import io.github.opendonationassistant.HistoryItem;
import io.github.opendonationassistant.HistoryItemData;
import io.github.opendonationassistant.HistoryItemRepository;
import io.micronaut.data.model.Pageable;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class GetHistoryCommand {

  private String recipientId;
  private Optional<String> nickname;
  private Optional<Amount> minAmount;
  private Optional<Amount> maxAmount;

  public List<HistoryItem> execute(HistoryItemRepository repository){
    return repository.findByRecipientId(recipientId);
  }

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public Optional<String> getNickname() {
    return nickname;
  }

  public void setNickname(Optional<String> nickname) {
    this.nickname = nickname;
  }

  public Optional<Amount> getMinAmount() {
    return minAmount;
  }

  public void setMinAmount(Optional<Amount> minAmount) {
    this.minAmount = minAmount;
  }

  public Optional<Amount> getMaxAmount() {
    return maxAmount;
  }

  public void setMaxAmount(Optional<Amount> maxAmount) {
    this.maxAmount = maxAmount;
  }
}
