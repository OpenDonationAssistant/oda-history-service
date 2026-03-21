package io.github.opendonationassistant.history.query;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.model.HistoryItem;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import java.util.Optional;

@Controller
public class GetHistory extends BaseController implements GetHistoryApi {

  private final HistoryItemRepository repository;

  @Inject
  public GetHistory(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Override
  public HttpResponse<Page<HistoryItemData>> getHistory(
    Authentication auth,
    Pageable pageable,
    @Body GetHistoryApi.GetHistoryCommand command
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
