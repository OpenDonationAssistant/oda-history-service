package io.github.opendonationassistant.history.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.repository.TwitchIdMappingRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Controller
public class LinkTwitchId extends BaseController implements LinkTwitchIdApi {

  private final TwitchIdMappingRepository repository;

  @Inject
  public LinkTwitchId(TwitchIdMappingRepository repository) {
    this.repository = repository;
  }

  @Override
  public CompletableFuture<HttpResponse<Void>> linkTwitchId(
    Authentication auth,
    LinkTwitchIdCommand command
  ) {
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return CompletableFuture.completedFuture(HttpResponse.unauthorized());
    }
    final var refreshTokenId = command.refreshTokenId();
    if (refreshTokenId == null || refreshTokenId.isBlank()) {
      return CompletableFuture.completedFuture(HttpResponse.badRequest());
    }
    repository.link(recipientId.get(), UUID.fromString(refreshTokenId));
    return CompletableFuture.completedFuture(HttpResponse.ok());
  }
}
