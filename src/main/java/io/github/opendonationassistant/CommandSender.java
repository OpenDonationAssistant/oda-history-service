package io.github.opendonationassistant;

import io.github.opendonationassistant.rabbit.Exchange;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;

@RabbitClient(Exchange.COMMANDS)
public interface CommandSender {
  void send(@Binding String binding, HistoryCommand command);
}
