package io.github.opendonationassistant.history.query;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

@Serdeable
public class GetHistory extends BaseController {

  private final HistoryItemRepository repository;

  @Inject
  public GetHistory(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Post("/history/get")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<Page<HistoryItemData>> execute(
    Authentication auth,
    Pageable pageable,
    @Body GetHistoryCommand command
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

  @Serdeable
  public static record GetHistoryCommand(List<String> systems) {}
}
