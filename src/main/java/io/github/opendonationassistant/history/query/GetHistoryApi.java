package io.github.opendonationassistant.history.query;

import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;

public interface GetHistoryApi {
  @Post("/history/get")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    hidden = true,
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
  HttpResponse<Page<HistoryItemData>> deprecatedGetHistory(
    Authentication auth,
    Pageable pageable,
    GetHistoryApi.GetHistoryCommand command
  );

  @Get("/history")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Get donation history",
    description = "Retrieves paginated donation history for the authenticated user"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully retrieved history items",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = GetHistoryResponse.class)
    )
  )
  @ApiResponse(
    responseCode = "401",
    description = "Unauthorized - user not authenticated",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = Void.class)
    )
  )
  CompletableFuture<HttpResponse<Page<HistoryItemData>>> getHistory(
    Authentication auth,
    Pageable pageable,
    @Nullable @QueryValue(value = "systems") List<String> systems,
    @Nullable @QueryValue(value = "events") List<String> events
  );

  @Serdeable
  @Schema(description = "Command to filter history by systems")
  record GetHistoryCommand(
    @Schema(
      description = "List of system names to filter by (e.g., 'donate.stream', 'donatello')"
    ) List<String> systems
  ) {}

  @Serdeable
  public static interface GetHistoryResponse extends Page<HistoryItemData> {}
}
