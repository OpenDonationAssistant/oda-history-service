package io.github.opendonationassistant;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class TargetGoal {

  private String goalId;
  private String goalTitle;

  public String getGoalId() {
    return goalId;
  }

  public void setGoalId(String goalId) {
    this.goalId = goalId;
  }

  public String getGoalTitle() {
    return goalTitle;
  }

  public void setGoalTitle(String goalTitle) {
    this.goalTitle = goalTitle;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((goalId == null) ? 0 : goalId.hashCode());
    result = prime * result + ((goalTitle == null) ? 0 : goalTitle.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TargetGoal other = (TargetGoal) obj;
    if (goalId == null) {
      if (other.goalId != null) return false;
    } else if (!goalId.equals(other.goalId)) return false;
    if (goalTitle == null) {
      if (other.goalTitle != null) return false;
    } else if (!goalTitle.equals(other.goalTitle)) return false;
    return true;
  }

  @Override
  public String toString() {
    return (
      "{\"_type\"=\"TargetGoal\",\"goalId\"=\"" +
      goalId +
      "\", goalTitle\"=\"" +
      goalTitle +
      "\", getGoalId()\"=\"" +
      getGoalId() +
      "}"
    );
  }
}
