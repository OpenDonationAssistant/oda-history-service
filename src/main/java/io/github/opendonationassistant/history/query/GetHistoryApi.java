package io.github.opendonationassistant.history.query;

import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public interface GetHistoryApi {

  HttpResponse<Page<HistoryItemData>> getHistory(
    Authentication auth,
    Pageable pageable,
    GetHistoryApi.GetHistoryCommand command
  );

  @Serdeable
  @Schema(description = "Command to filter history by systems")
  record GetHistoryCommand(
    @Schema(description = "List of system names to filter by (e.g., 'donate.stream', 'donatello')") List<String> systems
  ) {}
}
