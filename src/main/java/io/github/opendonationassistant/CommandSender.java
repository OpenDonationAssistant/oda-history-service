package io.github.opendonationassistant;

import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;

@RabbitClient(RabbitConfiguration.COMMANDS_EXCHANGE_NAME)
public interface CommandSender {
  void send(@Binding String binding, HistoryCommand command);
}
