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

public interface LinkTwitchIdApi {
  @Post("/history/commands/link-twitch-id")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Link a Twitch refresh token",
    description = "Stores the mapping between the authenticated recipient and the Twitch refresh token ID"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully stored the mapping",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = Void.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated")
  CompletableFuture<HttpResponse<Void>> linkTwitchId(
    Authentication auth,
    @Body LinkTwitchIdCommand command
  );

  @Serdeable
  @Schema(description = "Command to link a Twitch refresh token to the recipient")
  public static record LinkTwitchIdCommand(
    @Schema(description = "ID of the Twitch refresh token to link") String refreshTokenId
  ) {}
}
