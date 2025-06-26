package io.github.opendonationassistant;

import io.github.opendonationassistant.events.PaymentNotificationSender;
import io.github.opendonationassistant.events.alerts.AlertSender;
import io.github.opendonationassistant.history.command.AddHistoryItemCommand;
import io.github.opendonationassistant.history.query.GetHistoryCommand;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.Sort.Order;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/history")
public class HistoryController {

  private Logger log = LoggerFactory.getLogger(HistoryController.class);

  private final HistoryItemRepository repository;
  private final PaymentNotificationSender paymentSender;
  private final AlertSender alertSender;

  public HistoryController(
    HistoryItemRepository repository,
    PaymentNotificationSender paymentSender,
    AlertSender alertSender
  ) {
    this.repository = repository;
    this.paymentSender = paymentSender;
    this.alertSender = alertSender;
  }

  @Post("add")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @ExecuteOn(TaskExecutors.BLOCKING)
  public void addHistoryItem(@Body AddHistoryItemCommand command) {
    log.debug("command: {}", command);
    command.execute(repository, paymentSender, alertSender);
  }

  @Post("get")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public Page<HistoryItem> getHistory(
    Pageable pageable,
    @Body GetHistoryCommand command
  ) {
    log.debug("command: {}, pageable: {}", command, pageable);
    return command.execute(repository, pageable.withSort(Sort.of(Order.desc("authorization_timestamp")))); // TODO: fix it
  }
}
