package io.github.opendonationassistant.history.webhook;

import com.fasterxml.uuid.Generators;
import io.github.opendonationassistant.HistoryItem;
import io.github.opendonationassistant.HistoryItemRepository;
import io.github.opendonationassistant.commons.Amount;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
public class DonateStreamWebhook {

  private final HistoryItemRepository repository;

  @Inject
  public DonateStreamWebhook(HistoryItemRepository repository) {
    this.repository = repository;
  }

  @Post("/notification/donate.stream/{recipientId}")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<String> addHistoryItem(
    @PathVariable("recipientId") String recipientId,
    @Body DonateStreamWebhookBody body
  ) {
    handleDonation(recipientId, body);
    return HttpResponse.ok("OK");
  }

  private void handleDonation(
    String recipientId,
    DonateStreamWebhookBody body
  ) {
    HistoryItem created = new HistoryItem();
    created.setId(Generators.timeBasedEpochGenerator().generate().toString());
    created.setAmount(parseAmount(body.sum()));
    created.setMessage(body.message());
    created.setNickname(body.nickname());
    created.setPaymentId(
      Generators.timeBasedEpochGenerator().generate().toString()
    );
    created.setGoals(List.of());
    created.setRecipientId(recipientId);
    created.setAttachments(List.of());
    created.setReelResults(List.of());
    created.setSystem("Donate.Stream");
    created.setExternalId(body.uid());
    created.setAuthorizationTimestamp(Instant.now());
    repository.save(created);
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
  public static record DonateStreamWebhookBody(
    String type,
    String uid,
    String message,
    String nickname,
    String sum
  ) {}
}
