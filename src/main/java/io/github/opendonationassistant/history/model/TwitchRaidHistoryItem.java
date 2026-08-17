package io.github.opendonationassistant.history.model;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import java.util.List;
import java.util.UUID;

public class TwitchRaidHistoryItem extends HistoryItem {

  private final List<ItemAction> itemActions = List.of(
    new ItemAction(
      UUID.randomUUID().toString(),
      "create-shoutout",
      "Create Shoutout"
    )
  );

  public TwitchRaidHistoryItem(
    HistoryItemDataRepository repository,
    HistoryItemData data,
    HistoryFacade facade
  ) {
    super(repository, data, facade);
  }

  @Override
  public List<ItemAction> itemActions() {
    return this.itemActions;
  }
}
