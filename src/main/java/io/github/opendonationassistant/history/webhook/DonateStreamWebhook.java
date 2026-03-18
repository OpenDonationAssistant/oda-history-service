package io.github.opendonationassistant.history.webhook;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Controller
public class DonateStreamWebhook {

  private final HistoryItemRepository repository;
  private ODALogger log = new ODALogger(this);

  @Inject
  public DonateStreamWebhook(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Post("/notification/donate.stream/{recipientId}/{token}")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @ExecuteOn(TaskExecutors.BLOCKING)
  @Operation(
    summary = "Handle Donate.Stream webhook notifications",
    description = "Receives donation notifications from Donate.Stream and creates history items"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Successfully processed the webhook",
    content = @Content(mediaType = "text/plain")
  )
  public HttpResponse<String> addHistoryItem(
    @PathVariable("recipientId") String recipientId,
    @PathVariable("token") String token,
    @Body DonateStreamWebhookBody body
  ) {
    log.info("donate.stream webhook", Map.of("payload", body));
    if ("confirm".equals(body.type())) {
      return HttpResponse.ok(body.uid());
    }
    handleDonation(recipientId, body);
    return HttpResponse.ok("OK");
  }

  private void handleDonation(
    String recipientId,
    DonateStreamWebhookBody body
  ) {
    repository.create(
      new HistoryItemData(
        Generators.timeBasedEpochGenerator().generate().toString(),
        "payment",
        recipientId,
        "Donate.Stream",
        body.uid(),
        Instant.now(),
        body.nickname(),
        parseAmount(body.sum()),
        body.message(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        null,
        List.of()
      )
    );
  }

  private Amount parseAmount(String sum) {
    if (StringUtils.isEmpty(sum)) {
      return new Amount(0, 0, "RUB");
    }
    var dotPosition = sum.indexOf('.');
    return new Amount(
      Integer.parseInt(sum.substring(0, dotPosition)),
      Integer.parseInt(sum.substring(dotPosition + 1)),
      "RUB"
    );
  }

  @Serdeable
  @Schema(
    description = "Donate.Stream webhook payload",
    requiredProperties = {"type", "uid"}
  )
  public static record DonateStreamWebhookBody(
    @Schema(description = "Event type (e.g., 'confirm', 'donation')") String type,
    @Schema(description = "Unique identifier from Donate.Stream") String uid,
    @Schema(description = "Donation message from the donor") String message,
    @Schema(description = "Donor's nickname") String nickname,
    @Schema(description = "Donation amount as string (e.g., '100.00')") String sum
  ) {}
}
