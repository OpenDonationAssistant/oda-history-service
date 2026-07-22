package io.github.opendonationassistant.history.command;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class DeleteHistoryItem extends BaseController implements DeleteHistoryItemApi {

  private final HistoryItemRepository repository;
  private final ODALogger log = new ODALogger(this);

  @Inject
  public DeleteHistoryItem(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> deleteItem(
    Authentication auth,
    DeleteHistoryItemApi.DeleteHistoryItemCommand command
  ) {
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }

    final var historyItemId = command.historyItemId();
    if (historyItemId == null) {
      return CompletableFuture.completedFuture(HttpResponse.badRequest());
    }

    var historyItem = repository.findById(historyItemId);
    if (historyItem.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.notFound());
    }

    var item = historyItem.get();
    log.info(
      "Deleting history item",
      Map.of("historyItemId", historyItemId, "recipientId", item.data().recipientId())
    );

    item.markDeleted();

    return CompletableFuture.completedFuture(HttpResponse.ok());
  }
}
