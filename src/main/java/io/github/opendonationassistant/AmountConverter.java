package io.github.opendonationassistant;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;

import java.util.Objects;
import java.util.Optional;

@Singleton
public class AmountConverter implements AttributeConverter<Amount, String> {

  private static final String SPLIT_CHAR = ";";

  @Override
  public @Nullable String convertToPersistedValue(
    @Nullable Amount entityValue,
    @NonNull ConversionContext context
  ) {
    if (Objects.isNull(entityValue)) {
      return "";
    }
    return "%s;%s;%s".formatted(
        entityValue.getCurrency().orElse(""),
        entityValue.getMajor().orElse(0),
        entityValue.getMinor().orElse(0)
      );
  }

  @Override
  public @Nullable Amount convertToEntityValue(
    @Nullable String persistedValue,
    @NonNull ConversionContext context
  ) {
    if (persistedValue == null || persistedValue.isBlank()) {
      return null;
    }
    String[] parts = persistedValue.split(SPLIT_CHAR);
    return new Amount(
      Integer.parseInt(parts[1]),
      Integer.parseInt(parts[2]),
      parts[0]
    );
  }
}

