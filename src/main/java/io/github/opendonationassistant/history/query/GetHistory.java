package io.github.opendonationassistant.history.query;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@Controller
public class GetHistory extends BaseController implements GetHistoryApi {

  private final HistoryItemRepository repository;

  @Inject
  public GetHistory(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Post("/history/get")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Get donation history",
    description = "Retrieves paginated donation history for the authenticated user"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully retrieved history items",
    content = @Content(mediaType = "application/json")
  )
  @ApiResponse(
    responseCode = "401",
    description = "Unauthorized - user not authenticated",
    content = @Content(mediaType = "application/json")
  )
  @Override
  public HttpResponse<Page<HistoryItemData>> getHistory(
    Authentication auth,
    Pageable pageable,
    GetHistoryApi.GetHistoryCommand command
  ) {
    var recipientId = getOwnerId(auth);
    if (recipientId.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    return HttpResponse.ok(
      Optional.ofNullable(command.systems())
        .filter(list -> !list.isEmpty())
        .map(list ->
          repository.findByRecipientIdAndSystemIn(
            recipientId.get(),
            list,
            pageable
          )
        )
        .orElseGet(() ->
          repository.findByRecipientId(recipientId.get(), pageable)
        )
        .map(HistoryItem::data)
    );
  }
}
