package io.github.opendonationassistant.history.query;

import io.github.opendonationassistant.HistoryItem;
import io.github.opendonationassistant.HistoryItemRepository;
import io.github.opendonationassistant.commons.Amount;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.Optional;

@Serdeable
public class GetHistoryCommand {

  private String recipientId;
  private Optional<String> nickname;
  private Optional<Amount> minAmount;
  private Optional<Amount> maxAmount;

  public Page<HistoryItem> execute(
    HistoryItemRepository repository,
    Pageable pageable
  ) {
    return repository.findByRecipientId(
      recipientId,
      pageable
    );
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
