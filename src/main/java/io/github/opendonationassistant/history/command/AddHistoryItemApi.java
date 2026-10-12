package io.github.opendonationassistant.history.command;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.concurrent.CompletableFuture;

public interface AddHistoryItemApi {
  @Post("/history/commands/add-item")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Add a new history item",
    description = "Creates a new donation history item for the authenticated user"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully created the history item"
  )
  CompletableFuture<HttpResponse<Void>> addHistoryItem(
    Authentication auth,
    @Body AddHistoryItemCommand command
  );

  @Post("/history/add")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    hidden = true,
    summary = "Add a new history item",
    description = "Creates a new donation history item for the authenticated user"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully created the history item"
  )
  CompletableFuture<HttpResponse<Void>> oldAddHistoryItem(
    Authentication auth,
    @Body AddHistoryItemCommand command
  );

}
