package io.github.opendonationassistant.history.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.opendonationassistant.Amount;
import io.github.opendonationassistant.Attachment;
import io.github.opendonationassistant.HistoryItem;
import io.github.opendonationassistant.HistoryItemData;
import io.github.opendonationassistant.HistoryItemRepository;
import io.github.opendonationassistant.ReelResult;
import io.github.opendonationassistant.TargetGoal;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;

@MicronautTest(environments = "allinone")
public class GetHistoryCommandTest {

  @Inject
  public HistoryItemRepository repository;

  @Test
  public void testSaveAndReadByRecipientId() {
    var testdata = defaultHistoryItemData();
    repository.save(testdata);

    var wrongTestData = defaultHistoryItemData();
    wrongTestData.setRecipientId("wrongRecipientId");
    repository.save(wrongTestData);

    var command = new GetHistoryCommand();
    command.setRecipientId("recipientId");
    List<HistoryItem> results = command.execute(repository);

    var expected = List.of(testdata);
    assertEquals(expected, results);
  }

  private TargetGoal defaultGoal() {
    var goal = new TargetGoal();
    goal.setGoalId("goalId");
    goal.setGoalTitle("goalTitle");
    return goal;
  }

  private Attachment defaultAttachment() {
    var attach = new Attachment();
    attach.setUrl("url");
    attach.setTitle("attachTitle");
    return attach;
  }

  private ReelResult defaultReelResult() {
    var reelResult = new ReelResult();
    reelResult.setTitle("reelTitle");
    return reelResult;
  }

  private HistoryItem defaultHistoryItemData() {
    var testdata = new HistoryItem();
    testdata.setId("id");
    testdata.setMessage("message");
    testdata.setNickname("nickname");
    testdata.setPaymentId("paymentId");
    testdata.setRecipientId("recipientId");
    testdata.setAmount(new Amount(100, 0, "RUB"));
    testdata.setGoals(List.of(defaultGoal()));
    testdata.setAttachments(List.of(defaultAttachment()));
    testdata.setReelResults(List.of(defaultReelResult()));
    return testdata;
  }

}