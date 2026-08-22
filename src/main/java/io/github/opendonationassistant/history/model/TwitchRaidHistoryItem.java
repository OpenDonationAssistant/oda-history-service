package io.github.opendonationassistant.history.model;

import io.github.opendonationassistant.events.history.HistoryFacade;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.github.opendonationassistant.history.repository.TwitchIdMappingRepository;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TwitchRaidHistoryItem extends HistoryItem {

  private static final List<ItemAction> itemActions = List.of(
    new ItemAction("create-shoutout", "create-shoutout", "Create Shoutout")
  );

  private final RabbitClient commands;
  private final TwitchIdMappingRepository twitchIdMappingRepository;

  public TwitchRaidHistoryItem(
    HistoryItemDataRepository repository,
    HistoryItemData data,
    HistoryFacade facade,
    RabbitClient commands,
    TwitchIdMappingRepository twitchIdMappingRepository
  ) {
    super(repository, data, facade);
    this.commands = commands;
    this.twitchIdMappingRepository = twitchIdMappingRepository;
  }

  @Override
  public List<ItemAction> itemActions() {
    if (
      twitchIdMappingRepository
        .findByRecipientId(data().recipientId())
        .isEmpty()
    ) {
      return List.of();
    }
    return this.itemActions;
  }

  @Override
  public CompletableFuture<Void> executeItemAction(String itemActionId) {
    if ("create-shoutout".equals(itemActionId)) {
      final var fromChannelId = (String) data().metadata().get("fromChannelId");
      if (fromChannelId == null) {
        return CompletableFuture.completedFuture(null);
      }
      return CompletableFuture.runAsync(() ->
        commands.sendCommand(
          new TwitchShoutoutCommand(data().recipientId(), fromChannelId)
        )
      );
    }
    return super.executeItemAction(itemActionId);
  }

  @Serdeable
  public static record TwitchShoutoutCommand(
    String recipientId,
    String targetTwitchId
  ) {}
}
