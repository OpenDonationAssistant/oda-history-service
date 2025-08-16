package io.github.opendonationassistant;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.CompletedPaymentNotification;
import io.github.opendonationassistant.events.PaymentNotificationSender;
import io.github.opendonationassistant.events.alerts.AlertSender;
import io.micronaut.core.util.StringUtils;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Optional;

@RabbitListener
public class CommandListener {

  private final ODALogger log = new ODALogger(this);
  private final HistoryItemRepository repository;
  private final PaymentNotificationSender paymentSender;
  private final AlertSender alertSender;

  @Inject
  public CommandListener(
    HistoryItemRepository repository,
    PaymentNotificationSender paymentSender,
    AlertSender alertSender
  ) {
    this.repository = repository;
    this.paymentSender = paymentSender;
    this.alertSender = alertSender;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.Commands.HISTORY)
  @Transactional
  public void listen(HistoryCommand command) {
    log.info("Executing HistoryCommand", Map.of("command", command));
    switch (command.type()) {
      case "update":
        Optional<HistoryItemData> data = Optional.ofNullable(command.partial());
        data
          .map(HistoryItemData::getId)
          .flatMap(repository::findById)
          .or(() ->
            data
              .map(HistoryItemData::getPaymentId)
              .flatMap(repository::findByPaymentId)
          )
          .map(item -> item.merge(command.partial()))
          .ifPresentOrElse(
            updated -> updated.save(repository),
            () ->
              Optional.ofNullable(command.partial()).ifPresent(partial -> {
                partial.setSystem("ODA");
                new HistoryItem().merge(partial).save(repository);
              })
          );
        break;
      case "create":
        if (command.partial() == null) {
          return;
        }
        if (StringUtils.isNotEmpty(command.partial().getId())) {
          var existing = repository.findById(command.partial().getId());
          if (existing.isPresent()) {
            return;
          }
        }
        if (StringUtils.isNotEmpty(command.partial().getExternalId())) {
          var existing = repository.findByExternalId(
            command.partial().getExternalId()
          );
          if (existing.isPresent()) {
            return;
          }
        }
        CompletedPaymentNotification notification = command
          .partial()
          .makeNotification();
        new HistoryItem().merge(command.partial()).save(repository);
        if (
          command.addToGoal() &&
          command.partial().getGoals() != null &&
          command.partial().getGoals().size() > 0
        ) {
          paymentSender.sendToGoals(notification);
        }
        if (command.addToTop()) {
          paymentSender.sendToContributions(notification);
        }
        if (command.triggerReel()) {
          paymentSender.sendToReel(notification);
        }
        if (command.triggerDonaton()) {
          paymentSender.sendToDonaton(notification);
        }
        if (command.triggerAlert()) {
          alertSender.send(
            "%salerts".formatted(command.partial().getRecipientId()),
            notification.asAlertNotification()
          );
        }
        break;
      default:
        break;
    }
  }
}
