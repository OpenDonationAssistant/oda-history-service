package io.github.opendonationassistant.action;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.actions.ActionSender;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.util.Map;

@RabbitListener
public class ActionListener {

  private final ODALogger log = new ODALogger(this);

  private final ActionDataRepository repository;

  @Inject
  public ActionListener(ActionDataRepository repository) {
    this.repository = repository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.History.ACTIONS)
  public void listen(ActionSender.Action action) {
    log.info("Saving action", Map.of("id", action.id(), "name", action.name()));
    repository.save(new ActionData(action.id(), action.name()));
  }
}
