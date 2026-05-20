package io.github.opendonationassistant.history.command;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

@Controller
public class RepeatAlert extends BaseController implements RepeatAlertApi {

  private final HistoryItemRepository repository;
  private final ODALogger log = new ODALogger(this);
  private final RabbitClient rabbit;

  @Inject
  public RepeatAlert(
    HistoryItemRepository repository,
    @Named("commands") RabbitClient rabbit
  ) {
    this.repository = repository;
    this.rabbit = rabbit;
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> repeatAlert(
    Authentication auth,
    RepeatAlertApi.RepeatAlertCommand command
  ) {
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }

    final var historyItemId = command.historyItemId();
    if (historyItemId == null) {
      return CompletableFuture.completedFuture(HttpResponse.badRequest());
    }

    Optional<HistoryItem> historyItem = repository.findById(historyItemId);
    if (historyItem.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.notFound());
    }

    var item = historyItem.get();
    String originId = item.data().originId();

    log.info(
      "RepeatAlert: Sending RepeatAlertCommand",
      Map.of("historyItemId", historyItemId, "originId", originId)
    );

    return CompletableFuture.supplyAsync(() -> {
      rabbit.sendCommand(new RepeatAlertCommand(null, originId));
      return HttpResponse.ok();
    });
  }

  @Serdeable
  public static record RepeatAlertCommand(
    @Nullable String alertId,
    @Nullable String originId
  ) {}
}
