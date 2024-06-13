package io.github.opendonationassistant;

import static io.github.opendonationassistant.HistoryItemDataTestDataBuilder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class HistoryItemTest {

  HistoryItemRepository repository = mock(HistoryItemRepository.class);

  @Test
  public void testSavingNewHistoryItem() {
    when(repository.findById(any())).thenReturn(Optional.empty());
    var item = new HistoryItem();
    item.save(repository);
    verify(repository).save(item);
  }

  @Test
  public void testUpdatingExistingItem() {
    var previous = new HistoryItem();
    when(repository.findById(any())).thenReturn(Optional.of(previous));
    var item = new HistoryItem();
    item.save(repository);
    verify(repository).update(item);
  }

  @Test
  public void testMergingWithEmptyItem() {
    var result = oldHistoryItem().merge(new HistoryItem());
    assertEquals(oldHistoryItem(), result);
  }

  @Test
  public void testOverrideData() {
    var old = oldHistoryItem();
    old.setReelResults(List.of());
    old.setGoals(List.of());
    old.setAttachments(List.of());

    var updated = updatedHistoryItem();
    updated.setAttachments(List.of());
    updated.setReelResults(List.of());
    updated.setAttachments(List.of());

    HistoryItem result = old.merge(updated);
    assertEquals(updated, result);
  }

  @Test
  public void testMergingGoals() {
    var result = oldHistoryItem().merge(updatedHistoryItem());
    var expected = oldHistoryItem().getGoals();
    expected.addAll(updatedHistoryItem().getGoals());
    assertEquals(expected, result.getGoals());
  }

  @Test
  public void testMergingAttachments() {
    var result = oldHistoryItem().merge(updatedHistoryItem());
    var expected = oldHistoryItem().getAttachments();
    expected.addAll(updatedHistoryItem().getAttachments());
    assertEquals(expected, result.getAttachments());
  }

  @Test
  public void testMergingReelResults() {
    var result = oldHistoryItem().merge(updatedHistoryItem());
    var expected = oldHistoryItem().getReelResults();
    expected.addAll(updatedHistoryItem().getReelResults());
    assertEquals(expected, result.getReelResults());
  }


}
