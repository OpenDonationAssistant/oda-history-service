package io.github.opendonationassistant.history.command;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.concurrent.CompletableFuture;

public interface DeleteHistoryItemApi {
  @Post("/history/commands/delete-item")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Delete a history item",
    description = "Soft-deletes a history item by setting the deleted flag to true"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully deleted the history item",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = Void.class)
    )
  )
  @ApiResponse(responseCode = "404", description = "History item not found")
  CompletableFuture<HttpResponse<Void>> deleteItem(
    Authentication auth,
    @Body DeleteHistoryItemApi.DeleteHistoryItemCommand command
  );

  @Serdeable
  @Schema(description = "Command to delete a history item")
  public static record DeleteHistoryItemCommand(
    @Schema(description = "ID of the history item to delete") String historyItemId
  ) {}
}
