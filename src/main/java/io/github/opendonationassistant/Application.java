package io.github.opendonationassistant;

import io.github.opendonationassistant.rabbit.RabbitConfiguration;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;
import jakarta.inject.Singleton;

@OpenAPIDefinition(info = @Info(title = "oda-history-service", version = "0.3"))
public class Application {

  @ContextConfigurer
  public static class DefaultEnvironmentConfigurer
    implements ApplicationContextConfigurer {

    @Override
    public void configure(ApplicationContextBuilder builder) {
      builder.defaultEnvironments("standalone");
    }
  }

  public static void main(String[] args) {
    Micronaut.build(args).banner(false).classes(Application.class).start();
  }

  @Singleton
  public ChannelInitializer rabbitConfiguration() {
    return new RabbitConfiguration();
  }
}
