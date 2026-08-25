package io.github.opendonationassistant.history.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

public class HistoryMetricsTest {

  SimpleMeterRegistry registry = new SimpleMeterRegistry();
  HistoryMetrics metrics = new HistoryMetrics(registry);

  @Test
  public void testEventHandledIncrementsPerType() {
    metrics.eventHandled("PaymentEvent");
    metrics.eventHandled("PaymentEvent");
    metrics.eventHandled("TwitchChannelCheerEvent");

    assertEquals(
      2.0,
      registry
        .counter("history.events.handled", "type", "PaymentEvent")
        .count()
    );
    assertEquals(
      1.0,
      registry
        .counter("history.events.handled", "type", "TwitchChannelCheerEvent")
        .count()
    );
  }

  @Test
  public void testEventHandledUsesUnknownForNullType() {
    metrics.eventHandled(null);

    assertEquals(
      1.0,
      registry.counter("history.events.handled", "type", "unknown").count()
    );
  }

  @Test
  public void testItemCreatedIncrementsPerSystemAndType() {
    metrics.itemCreated("Twitch", "cheer");
    metrics.itemCreated("ODA", "payment");
    metrics.itemCreated("Twitch", "cheer");

    assertEquals(
      2.0,
      registry
        .counter("history.items.created", "system", "Twitch", "type", "cheer")
        .count()
    );
    assertEquals(
      1.0,
      registry
        .counter("history.items.created", "system", "ODA", "type", "payment")
        .count()
    );
  }

  @Test
  public void testItemCreatedUsesUnknownForNulls() {
    metrics.itemCreated(null, null);

    assertEquals(
      1.0,
      registry
        .counter(
          "history.items.created",
          "system",
          "unknown",
          "type",
          "unknown"
        )
        .count()
    );
  }
}