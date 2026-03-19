package io.github.opendonationassistant.history.command;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
public class AddHistoryItem
  extends BaseController
  implements AddHistoryItemApi {

  private final HistoryItemRepository repository;
  private final ODALogger log = new ODALogger(this);

  @Inject
  public AddHistoryItem(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Override
  public CompletableFuture<Void> addHistoryItem(
    Authentication auth,
    AddHistoryItemApi.AddHistoryItemCommand command
  ) {
    log.debug("AddHistoryItemCommand Authentication", Map.of("auth", auth));
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }
    var created = new HistoryItemData(
      Generators.timeBasedEpochGenerator().generate().toString(),
      "payment",
      command.recipientId(),
      command.system(),
      command.paymentId(),
      Optional.ofNullable(command.authorizationTimestamp()).orElseGet(() ->
        Instant.now()
      ),
      command.nickname(),
      command.amount(),
      command.message(),
      List.of(), // attachments
      List.of(), // goals
      List.of(), // reelResults
      List.of(), // actions
      null, // votes
      List.of()
    );
    return CompletableFuture.runAsync(() -> repository.create(created));
  }
}
