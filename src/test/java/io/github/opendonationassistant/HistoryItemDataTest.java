package io.github.opendonationassistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.github.opendonationassistant.HistoryItemDataTestDataBuilder.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

public class HistoryItemDataTest {
  @Test
  public void testOverrideData() {

    HistoryItem result = oldHistoryItem().merge(updatedHistoryItem());

    var expected = new HistoryItem();
    expected.setId("id");
    expected.setPaymentId("updatedPaymentId");
    expected.setAmount(new Amount(200, 0, "RUB"));
    expected.setMessage("updatedMessage");
    expected.setNickname("updatedNickname");
    expected.setAuthorizationTimestamp(Instant.MAX);
    expected.setRecipientId("newRecipientId");

    assertEquals(expected, result);
  }

  @Test
  public void testMergeWithOldData() {

    var updated = new HistoryItem();
    updated.setId("id");

    HistoryItemData result = oldHistoryItem().merge(updated);

    var expected = new HistoryItem();
    expected.setId("id");
    expected.setPaymentId("oldPaymentId");
    expected.setAmount(new Amount(100, 0, "RUB"));
    expected.setMessage("oldMessage");
    expected.setNickname("oldNickname");
    expected.setAuthorizationTimestamp(Instant.MIN);
    expected.setRecipientId("oldRecipientId");

    assertEquals(expected, result);
  }


}
