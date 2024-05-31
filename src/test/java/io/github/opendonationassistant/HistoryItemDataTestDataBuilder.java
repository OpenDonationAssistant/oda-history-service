package io.github.opendonationassistant;

import java.time.Instant;

public class HistoryItemDataTestDataBuilder {

  public static HistoryItem minimalHistoryItem(){
    var item = new HistoryItem();
    item.setId("id");
    return  item;
  }

  public static HistoryItem oldHistoryItem(){
    var old = new HistoryItem();
    old.setId("id");
    old.setPaymentId("oldPaymentId");
    old.setAmount(new Amount(100, 0, "RUB"));
    old.setMessage("oldMessage");
    old.setNickname("oldNickname");
    old.setAuthorizationTimestamp(Instant.MIN);
    old.setRecipientId("oldRecipientId");
    return old;
  }

  public static HistoryItem updatedHistoryItem(){
    var updated = new HistoryItem();
    updated.setId("id");
    updated.setPaymentId("updatedPaymentId");
    updated.setAmount(new Amount(200, 0, "RUB"));
    updated.setMessage("updatedMessage");
    updated.setNickname("updatedNickname");
    updated.setAuthorizationTimestamp(Instant.MAX);
    updated.setRecipientId("newRecipientId");
    return updated;
  }

}
