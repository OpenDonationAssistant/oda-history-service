package io.github.opendonationassistant;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;

@RabbitListener
public class CommandListener {

  private final ODALogger log = new ODALogger(this);
  private final HistoryItemRepository repository;

  @Inject
  public CommandListener(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.Commands.HISTORY)
  public void listen(HistoryCommand command) {
    log.info("Executing HistoryCommand", Map.of("command", command));
    switch (command.getType()) {
      case "update":
        Optional<HistoryItem> data = Optional.ofNullable(command.getPartial());
        data
          .map(HistoryItem::getId)
          .flatMap(repository::findById)
          .or(() ->
            data
              .map(HistoryItem::getPaymentId)
              .flatMap(repository::findByPaymentId)
          )
          .map(item -> item.merge(command.getPartial()))
          .ifPresentOrElse(
            updated -> updated.save(repository),
            () ->
              Optional.ofNullable(command.getPartial()).ifPresent(partial -> {
                partial.setSystem("ODA");
                partial.save(repository);
              })
          );
        break;
      default:
        break;
    }
  }
}
