package io.github.opendonationassistant;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;

@Singleton
public class RabbitConfiguration extends ChannelInitializer {

  public static final String COMMANDS_EXCHANGE_NAME = "commands";
  public static final String COMMANDS_QUEUE_NAME = "commands.history";
  public static final String HISTORY_COMMANDS_ROUTING_KEY = "history";

  @Override
  public void initialize(Channel channel, String name) throws IOException {
    channel.exchangeDeclare(COMMANDS_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
    channel.queueDeclare(
      COMMANDS_QUEUE_NAME,
      true,
      false,
      false,
      new HashMap<>()
    );
    channel.queueBind(
      COMMANDS_QUEUE_NAME,
      COMMANDS_EXCHANGE_NAME,
      HISTORY_COMMANDS_ROUTING_KEY
    );
  }
}
