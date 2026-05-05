package io.github.opendonationassistant.history.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.listener.handlers.PrintHistoryHandler;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

@Controller
public class PrintCsv extends BaseController {

  private final RabbitClient commandsFacade;

  @Inject
  public PrintCsv(@Named("commands") RabbitClient commandsFacade) {
    this.commandsFacade = commandsFacade;
  }

  @Post("/history/commands/print-csv")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public CompletableFuture<HttpResponse<PrintCsvResponse>> printCsv(
    Authentication auth,
    @Body PrintCsvCommand command
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    return CompletableFuture.supplyAsync(() -> {
      commandsFacade.sendCommand(
        new PrintHistoryHandler.PrintHistoryCommand(
          ownerId.get(),
          UUID.randomUUID().toString(),
          command.systems(),
          command.events(),
          command.after(),
          command.before()
        )
      );
      return HttpResponse.ok();
    });
  }

  @Serdeable
  public static record PrintCsvCommand(
    @Nullable List<String> systems,
    @Nullable List<String> events,
    @Nullable Instant after,
    @Nullable Instant before
  ) {}

  @Serdeable
  public static record PrintCsvResponse(String printId) {}
}
