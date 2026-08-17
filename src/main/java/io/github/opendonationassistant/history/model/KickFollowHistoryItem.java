package io.github.opendonationassistant.history.model;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;

public class KickFollowHistoryItem extends HistoryItem {

  public KickFollowHistoryItem(
    HistoryItemDataRepository repository,
    HistoryItemData data,
    HistoryFacade facade
  ) {
    super(repository, data, facade);
  }
}
