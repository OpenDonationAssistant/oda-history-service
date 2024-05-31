package io.github.opendonationassistant;

import java.util.Optional;

import io.micronaut.data.annotation.TypeDef;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.data.model.DataType;

@Serdeable
@TypeDef(type = DataType.STRING, converter = AmountConverter.class)
public class Amount {

  private Integer minor;
  private Integer major;
  private String currency;

  public Amount(Integer major, Integer minor, String currency) {
    this.minor = minor;
    this.major = major;
    this.currency = currency;
  }

  public Optional<Integer> getMinor() {
    return Optional.ofNullable(minor);
  }

  public Optional<Integer> getMajor() {
    return Optional.ofNullable(major);
  }

  public Optional<String> getCurrency() {
    return Optional.ofNullable(currency);
  }

  @Override
  public String toString() {
    return """
    {
      "_type": "Amount",
      "major":"%s",
      "minor":"%s",
      "currency":"%s"
    }
    """.formatted(major, minor, currency);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((minor == null) ? 0 : minor.hashCode());
    result = prime * result + ((major == null) ? 0 : major.hashCode());
    result = prime * result + ((currency == null) ? 0 : currency.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Amount other = (Amount) obj;
    if (minor == null) {
      if (other.minor != null)
        return false;
    } else if (!minor.equals(other.minor))
      return false;
    if (major == null) {
      if (other.major != null)
        return false;
    } else if (!major.equals(other.major))
      return false;
    if (currency == null) {
      if (other.currency != null)
        return false;
    } else if (!currency.equals(other.currency))
      return false;
    return true;
  }
}
