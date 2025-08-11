package io.github.opendonationassistant.payment;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.history.HistoryCommand;
import io.github.opendonationassistant.events.history.HistoryCommandSender;
import io.github.opendonationassistant.events.history.HistoryItemData;
import io.github.opendonationassistant.events.history.TargetGoal;
import io.github.opendonationassistant.goal.GoalDataRepository;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RabbitListener
public class PaymentListener {

  private final ODALogger log = new ODALogger(this);

  private final HistoryCommandSender commandSender;
  private final GoalDataRepository goalRepository;

  @Inject
  public PaymentListener(
    HistoryCommandSender commandSender,
    GoalDataRepository goalRepository
  ) {
    this.commandSender = commandSender;
    this.goalRepository = goalRepository;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.Payments.HISTORY)
  public void listen(CompletedPaymentNotification payment) {
    log.info("Received notification for history", Map.of("payment", payment));

    var partial = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      payment.id(),
      payment.nickname(),
      payment.cleanNickname(),
      payment.recipientId(),
      payment.amount(),
      payment.message(),
      payment.cleanMessage(),
      "ODA",
      null,
      payment.authorizationTimestamp(),
      List.of(),
      Optional.ofNullable(payment.goal())
        .flatMap(goalRepository::findById)
        .map(goal -> List.of(new TargetGoal(goal.id(), goal.title())))
        .orElse(null),
      null
    );

    commandSender.send("history", new HistoryCommand("update", partial, false, false, false, false, false));
  }
}
