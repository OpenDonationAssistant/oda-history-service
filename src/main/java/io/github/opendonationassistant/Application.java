package io.github.opendonationassistant;

import com.rabbitmq.client.Channel;
import io.github.opendonationassistant.rabbit.AMQPConfiguration;
import io.github.opendonationassistant.rabbit.Exchange;
import io.github.opendonationassistant.rabbit.Queue;
import io.github.opendonationassistant.rabbit.RabbitClient;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.annotation.Factory;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.ObjectMapper;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;

@OpenAPIDefinition(info = @Info(title = "oda-history-service"))
@Factory
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
    var events = new Queue("history.events");
    var commands = new Queue("history.command");
    return new AMQPConfiguration(
      List.of(
        Exchange.Exchange("commands", Map.of("AddHistoryItem", commands)),
        Exchange.Exchange("payments", Map.of("event.PaymentEvent", events)),
        Exchange.Exchange("twitch", Map.of("*", events)),
        Exchange.Exchange(
          "history",
          Map.of(
            "event.ReelResultHistoryEvent",
            events,
            "event.GoalHistoryEvent",
            events,
            "event.MediaHistoryEvent",
            events,
            "command",
            commands
          )
        ),
        Exchange.Exchange(
          "donaton",
          Map.of("event.DonatonDeadlineChanged", events)
        ),
        Exchange.Exchange("actions", Map.of("event.ActionHistoryEvent", events))
      )
    );
  }

  @Singleton
  @Named("automation")
  public RabbitClient automationFacade(Channel channel, ObjectMapper mapper) {
    return new RabbitClient(channel, mapper, "automation");
  }

  @Singleton
  @Named("commands")
  public RabbitClient commandsFacade(Channel channel, ObjectMapper mapper) {
    return new RabbitClient(channel, mapper, "commands");
  }
}
