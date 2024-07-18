package io.github.opendonationassistant;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class HistoryCommand {

  private String type;
  private HistoryItem partial;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public HistoryItem getPartial() {
    return partial;
  }

  public void setPartial(HistoryItem partial) {
    this.partial = partial;
  }

  @Override
  public String toString() {
    try {
      return ObjectMapper.getDefault().writeValueAsString(this);
    } catch (Exception e) {
      return "Can't serialize as json";
    }
  }
}
