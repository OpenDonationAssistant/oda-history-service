package io.github.opendonationassistant;

import org.instancio.Instancio;

public class HistoryItemDataTestDataBuilder {

  static HistoryItem oldHistoryItem = Instancio.create(HistoryItem.class);
  static HistoryItem updatedHistoryItem = Instancio.create(HistoryItem.class);

  public static HistoryItem minimalHistoryItem() {
    return new HistoryItem() {{
      setId("id");
    }};
  }

  public static HistoryItem oldHistoryItem() {
    return oldHistoryItem;
  }

  public static HistoryItem updatedHistoryItem() {
    return updatedHistoryItem;
  }
}
