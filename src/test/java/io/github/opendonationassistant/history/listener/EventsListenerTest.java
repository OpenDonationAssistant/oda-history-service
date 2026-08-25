package io.github.opendonationassistant.history.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import io.github.opendonationassistant.events.MessageProcessor;
import io.github.opendonationassistant.history.metrics.HistoryMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class EventsListenerTest {

  @Test
  public void testCountsHandledEventsByType() throws IOException {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    HistoryMetrics metrics = new HistoryMetrics(registry);
    MessageProcessor processor = mock(MessageProcessor.class);
    RabbitAcknowledgement ack = mock(RabbitAcknowledgement.class);
    var listener = new EventsListener(processor, metrics);

    listener.listen("PaymentEvent", new byte[0], ack);
    listener.listen("PaymentEvent", new byte[0], ack);
    listener.listen("TwitchChannelCheerEvent", new byte[0], ack);

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
}