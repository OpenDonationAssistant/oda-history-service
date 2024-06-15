package io.github.opendonationassistant;

import io.github.opendonationassistant.history.query.GetHistoryCommand;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/history")
public class HistoryController {

  private Logger log = LoggerFactory.getLogger(HistoryController.class);

  private final HistoryItemRepository repository;

  public HistoryController(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Post("get")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public Page<HistoryItem> getHistory(
    Pageable pageable,
    @Body GetHistoryCommand command
  ) {
    log.debug("command: {}, pageable: {}", command, pageable);
    return command.execute(repository, pageable);
  }
}
