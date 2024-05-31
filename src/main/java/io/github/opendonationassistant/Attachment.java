package io.github.opendonationassistant;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Attachment {

  private String id;
  private String url;
  private String title;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Attachment other = (Attachment) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    if (url == null) {
      if (other.url != null) return false;
    } else if (!url.equals(other.url)) return false;
    if (title == null) {
      if (other.title != null) return false;
    } else if (!title.equals(other.title)) return false;
    return true;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
