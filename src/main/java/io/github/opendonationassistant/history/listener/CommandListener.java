package io.github.opendonationassistant.history.listener;

import io.github.opendonationassistant.events.MessageProcessor;
import io.micronaut.messaging.annotation.MessageHeader;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;

@RabbitListener
public class CommandListener {

  private MessageProcessor processor;

  public void listenHistoryCommands(
    @MessageHeader("type") String type,
    byte[] payload,
    RabbitAcknowledgement ack
  ) {
    processor.process(type, payload, ack);
  }
}
