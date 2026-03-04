package io.github.opendonationassistant.history.listener;

import io.github.opendonationassistant.events.MessageProcessor;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;
import java.io.IOException;

@RabbitListener
public class EventsListener {

  private final MessageProcessor processor;

  @Inject
  public EventsListener(MessageProcessor processor) {
    this.processor = processor;
  }

  @Queue(io.github.opendonationassistant.rabbit.Queue.History.EVENTS)
  public void listen(
    @MessageHeader("type") String type,
    byte[] payload,
    RabbitAcknowledgement ack
  ) throws IOException {
    processor.process(type, payload, ack);
  }
}
