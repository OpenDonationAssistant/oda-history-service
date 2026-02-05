package io.github.opendonationassistant.history.query;

import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.Optional;

@Serdeable
public class GetHistoryCommand {

  private String recipientId;
  private List<String> systems;

  public Page<HistoryItem> execute(
    HistoryItemRepository repository,
    Pageable pageable
  ) {
    return Optional.ofNullable(systems)
      .filter(list -> !list.isEmpty())
      .map(list ->
        repository.findByRecipientIdAndSystemIn(recipientId, list, pageable)
      )
      .orElseGet(() -> repository.findByRecipientId(recipientId, pageable));
  }

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public List<String> getSystems() {
    return systems;
  }

  public void setSystems(List<String> systems) {
    this.systems = systems;
  }
}
