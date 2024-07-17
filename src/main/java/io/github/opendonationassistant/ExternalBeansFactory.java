package io.github.opendonationassistant;

import io.micronaut.context.annotation.Factory;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import jakarta.inject.Singleton;

@Factory
public class ExternalBeansFactory {

  @Singleton
  RabbitConfiguration rabbitConfiguration() {
    return new RabbitConfiguration();
  }
}
