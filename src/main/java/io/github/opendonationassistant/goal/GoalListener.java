package io.github.opendonationassistant.goal;

import io.github.opendonationassistant.events.goal.UpdatedGoal;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;

@RabbitListener
public class GoalListener {

  private final GoalDataRepository repository;

  @Inject
  public GoalListener(GoalDataRepository repository) {
    this.repository = repository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.History.GOAL)
  public void listen(UpdatedGoal goal) {
    repository
      .findById(goal.goalId())
      .ifPresentOrElse(
        existing ->
          repository.update(
            new GoalData(
              goal.goalId(),
              goal.briefDescription(),
              goal.isDefault()
            )
          ),
        () ->
          repository.save(
            new GoalData(
              goal.goalId(),
              goal.briefDescription(),
              goal.isDefault()
            )
          )
      );
  }
}
