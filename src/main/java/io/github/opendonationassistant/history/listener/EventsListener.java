package io.github.opendonationassistant.history.listener;

import io.github.opendonationassistant.events.MessageProcessor;
import io.github.opendonationassistant.history.metrics.HistoryMetrics;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;
import java.io.IOException;

@RabbitListener(executor = "event-listener")
public class EventsListener {

  private final MessageProcessor processor;
  private final HistoryMetrics metrics;

  @Inject
  public EventsListener(MessageProcessor processor, HistoryMetrics metrics) {
    this.processor = processor;
    this.metrics = metrics;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.History.EVENTS)
  public void listen(
    @MessageHeader("type") String type,
    byte[] payload,
    RabbitAcknowledgement ack
  ) throws IOException {
    metrics.eventHandled(type);
    processor.process(type, payload, ack);
  }
}
