package io.github.opendonationassistant.history.command;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.concurrent.CompletableFuture;

public interface RepeatAlertApi {

  @Post("/history/commands/repeat-alert")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Repeat an alert",
    description = "Repeats an alert for a given history item"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully repeated the alert"
  )
  @ApiResponse(
    responseCode = "404",
    description = "History item not found"
  )
  CompletableFuture<HttpResponse<Void>> repeatAlert(
    Authentication auth,
    @Body RepeatAlertCommand command
  );

  @Serdeable
  @Schema(description = "Command to repeat an alert")
  public static record RepeatAlertCommand(
    @Schema(description = "ID of the history item") String historyItemId
  ) {}
}
