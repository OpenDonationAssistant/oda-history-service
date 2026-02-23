package io.github.opendonationassistant.history.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.instancio.Select.*;

import com.deblock.jsondiff.DiffGenerator;
import com.deblock.jsondiff.matcher.CompositeJsonMatcher;
import com.deblock.jsondiff.matcher.LenientJsonArrayPartialMatcher;
import com.deblock.jsondiff.matcher.LenientJsonObjectPartialMatcher;
import com.deblock.jsondiff.matcher.StrictPrimitivePartialMatcher;
import com.deblock.jsondiff.viewer.OnlyErrorDiffViewer;
import io.github.opendonationassistant.commons.ToString;
import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemRepository;
import io.github.opendonationassistant.testutils.AuthenticationGenerator;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.instancio.Instancio;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@MicronautTest(environments = "allinone")
@ExtendWith(InstancioExtension.class)
public class GetHistoryCommandTest {

  private final ODALogger log = new ODALogger(this);

  @Inject
  HistoryItemRepository repository;

  @Inject
  GetHistory getHistory;

  @Test
  public void testGetByRecipientId(@Given String recipientId) {
    var shouldNotBeFound = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::system), "ODA")
      .set(field(HistoryItemData::actions), List.of())
      .stream()
      .limit(10);
    shouldNotBeFound.forEach(repository::create);

    var shouldBeFound = Instancio.of(HistoryItemData.class)
      .set(field(HistoryItemData::recipientId), recipientId)
      .set(field(HistoryItemData::actions), List.of())
      .set(
        field(HistoryItemData::timestamp),
        Instant.parse("2022-01-01T00:00:00.000Z")
      )
      .stream()
      .limit(3)
      .toList();
    shouldBeFound.forEach(repository::create);

    Pageable page = Pageable.from(0, 10);

    Page<HistoryItemData> results = getHistory
      .getHistory(
        AuthenticationGenerator.forUser(recipientId),
        page,
        new GetHistory.GetHistoryCommand(List.of())
      )
      .getBody()
      .get();

    var expected = Page.of(shouldBeFound, page, 1L);
    assertSame(expected, results);
  }

  @Test
  public void testFilterBySystem(@Given String recipientId) {
    Function<String, HistoryItemData> createPayment = system ->
      Instancio.of(HistoryItemData.class)
        .set(field(HistoryItemData::recipientId), recipientId)
        .set(field(HistoryItemData::actions), List.of())
        .set(
          field(HistoryItemData::timestamp),
          Instant.parse("2022-01-01T00:00:00.000Z")
        )
        .set(field(HistoryItemData::system), system)
        .create();

    var odaPayment = createPayment.apply("ODA");
    var daPayment = createPayment.apply("DonationAlerts");
    var donatePayPayment = createPayment.apply("DonatePay");
    var donatestreamPayment = createPayment.apply("Donate.Stream");

    var allPayments = List.of(
      odaPayment,
      daPayment,
      donatePayPayment,
      donatestreamPayment
    );
    allPayments.forEach(repository::create);

    final Pageable page = Pageable.from(0, 10);
    final Authentication auth = AuthenticationGenerator.forUser(recipientId);
    // prettier-ignore ON
    Function<GetHistory.GetHistoryCommand,Page<HistoryItemData>> getHistoryPage = command ->
      getHistory.getHistory(auth, page, command).getBody().get();
    // prettier-ignore OFF

    assertSame(
      Page.of(allPayments, page, 4L),
      getHistoryPage.apply(new GetHistory.GetHistoryCommand(List.of()))
    );

    assertSame(
      Page.of(List.of(daPayment), page, 1L),
      getHistoryPage.apply(
        new GetHistory.GetHistoryCommand(List.of("DonationAlerts"))
      )
    );

    assertSame(
      Page.of(List.of(odaPayment, donatestreamPayment), page, 2L),
      getHistoryPage.apply(
        new GetHistory.GetHistoryCommand(List.of("ODA", "Donate.Stream"))
      )
    );
  }

  private void assertSame(Object expected, Object actual) {
    log.debug("AssertSame", Map.of("expected", expected, "actual", actual));
    var diff = DiffGenerator.diff(
      ToString.asJson(expected),
      ToString.asJson(actual),
      new CompositeJsonMatcher(
        new LenientJsonArrayPartialMatcher(),
        new LenientJsonObjectPartialMatcher(),
        new StrictPrimitivePartialMatcher()
      )
    );
    assertThat(
      OnlyErrorDiffViewer.from(diff).toString(),
      diff.similarityRate() == 100.0
    );
  }
}
