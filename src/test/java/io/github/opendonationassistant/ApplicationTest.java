package io.github.opendonationassistant;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.rabbit.Key;
import io.github.opendonationassistant.rabbit.RabbitConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone")
public class ApplicationTest {

  @Inject
  public CommandSender commandSender;

  @Inject
  public HistoryItemRepository repository;

  @Test
  public void testHandlingUpdateHistoryCommand() {
    var item = new HistoryItem();
    item.setId("id");
    item.setAmount(new Amount(100, 0, "RUB"));
    item.setRecipientId("recipientId");
    item.setPaymentId("paymentId");
    item.setNickname("nickname");

    var command = new HistoryCommand("update",item, false,false, false, false, false);

    commandSender.send(
      Key.HISTORY,
      command
    );
    Awaitility.await().until(() -> repository.findById("id").isPresent());
  }
}
