package io.github.opendonationassistant;

import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.util.Optional;

@RabbitListener
public class CommandListener {

  private final HistoryItemRepository repository;

  @Inject
  public CommandListener(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Queue(RabbitConfiguration.COMMANDS_QUEUE_NAME)
  public void listen(HistoryCommand command) {
    switch (command.getType()) {
      case "update":
        Optional
          .ofNullable(command.getPartial())
          .map(HistoryItem::getId)
          .flatMap(repository::findById)
          .map(item -> item.merge(command.getPartial()))
          .ifPresentOrElse(
            updated -> updated.save(repository),
            () ->
              Optional
                .ofNullable(command.getPartial())
                .ifPresent(partial -> partial.save(repository))
          );
        break;
      default:
        break;
    }
  }
}