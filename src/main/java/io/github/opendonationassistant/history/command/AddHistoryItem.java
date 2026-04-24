package io.github.opendonationassistant.history.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.concurrent.CompletableFuture;

@Controller
public class AddHistoryItem
  extends BaseController
  implements AddHistoryItemApi {

  private final RabbitClient commandsFacade;

  @Inject
  public AddHistoryItem(@Named("commands") RabbitClient commandsFacade) {
    this.commandsFacade = commandsFacade;
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> oldAddHistoryItem(
    Authentication auth,
    AddHistoryItemCommand command
  ) {
    return addHistoryItem(auth, command);
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> addHistoryItem(
    Authentication auth,
    AddHistoryItemApi.AddHistoryItemCommand command
  ) {
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    final var paymentId = command.paymentId();
    if (paymentId == null) {
      return CompletableFuture.completedFuture(HttpResponse.badRequest());
    }
    return CompletableFuture.runAsync(() ->
      this.commandsFacade.sendCommand(command)
    ).thenApply(_ -> HttpResponse.ok());
  }
}
