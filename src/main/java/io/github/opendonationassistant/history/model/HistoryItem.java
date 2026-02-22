package io.github.opendonationassistant.history.model;

import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class HistoryItem {

  private HistoryItemData data;
  private HistoryItemDataRepository repository;

  public HistoryItem(
    HistoryItemDataRepository repository,
    HistoryItemData data
  ) {
    this.repository = repository;
    this.data = data;
  }

  public HistoryItemData data(){
    return this.data;
  }

  public void save() {
    repository.update(data);
  }
}
