package io.github.opendonationassistant.goal;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.goal.UpdatedGoal;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.util.Map;

@RabbitListener
public class GoalListener {

  private final GoalDataRepository repository;
  private final ODALogger log = new ODALogger(this);

  @Inject
  public GoalListener(GoalDataRepository repository) {
    this.repository = repository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.History.GOAL)
  public void listen(UpdatedGoal goal) {
    log.info("Received UpdatedGoal", Map.of("goal", goal));

    repository
      .findById(goal.goalId())
      .ifPresentOrElse(
        existing -> {
          log.debug("Updating goal", Map.of("goal", goal));
          repository.update(
            new GoalData(
              goal.goalId(),
              goal.briefDescription(),
              goal.isDefault()
            )
          );
        },
        () -> {
          log.debug("Saving goal", Map.of("goal", goal));
          repository.save(
            new GoalData(
              goal.goalId(),
              goal.briefDescription(),
              goal.isDefault()
            )
          );
        }
      );
  }
}
