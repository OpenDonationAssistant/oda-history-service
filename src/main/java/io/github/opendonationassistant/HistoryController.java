package io.github.opendonationassistant;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.history.HistoryCommandSender;
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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/history")
public class HistoryController {

  private ODALogger log = new ODALogger(this);

  private final HistoryItemRepository repository;
  private final HistoryCommandSender sender;

  public HistoryController(
    HistoryItemRepository repository,
    HistoryCommandSender commandSender
  ) {
    this.repository = repository;
    this.sender = commandSender;
  }

  @Post("add")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @ExecuteOn(TaskExecutors.BLOCKING)
  public void addHistoryItem(@Body AddHistoryItemCommand command) {
    log.info("Executing AddHistoryItemCommand", Map.of("command", command));
    command.execute(sender);
  }

  @Post("get")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @ExecuteOn(TaskExecutors.BLOCKING)
  public Page<HistoryItem> getHistory(
    Pageable pageable,
    @Body GetHistoryCommand command
  ) {
    log.debug(
      "Executing GetHistoryCommand",
      Map.of("command", command, "pageable", pageable)
    );
    return command.execute(
      repository,
      pageable.withSort(Sort.of(Order.desc("authorizationTimestamp")))
    );
  }
}
