package io.github.opendonationassistant.history.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

@Singleton
public class HistoryMetrics {

  private final MeterRegistry registry;

  public HistoryMetrics(MeterRegistry registry) {
    this.registry = registry;
  }

  public void eventHandled(@Nullable String type) {
    Counter
      .builder("history.events.handled")
      .tag("type", Optional.ofNullable(type).orElse("unknown"))
      .register(registry)
      .increment();
  }

  public void itemCreated(@Nullable String system, @Nullable String type) {
    Counter
      .builder("history.items.created")
      .tag("system", Optional.ofNullable(system).orElse("unknown"))
      .tag("type", Optional.ofNullable(type).orElse("unknown"))
      .register(registry)
      .increment();
  }
}