package io.github.opendonationassistant.history.listener.handlers;

import static io.micronaut.data.repository.jpa.criteria.PredicateSpecification.*;

import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.history.model.Printer;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Singleton
public class PrintHistoryHandler
  extends AbstractMessageHandler<PrintHistoryHandler.PrintHistoryCommand> {

  private final Printer printer;

  @Inject
  public PrintHistoryHandler(ObjectMapper mapper, Printer printer) {
    super(mapper);
    this.printer = printer;
  }

  @Override
  public void handle(PrintHistoryCommand message) throws IOException {
    final ArrayList<PredicateSpecification<HistoryItemData>> conditions =
      new ArrayList<>();
    conditions.add(
      where((root, builder) ->
        builder.equal(root.get("recipientId"), message.recipientId())
      )
    );
    if (message.systems() != null && message.systems().size() > 0) {
      conditions.add(
        where((root, builder) -> root.get("system").in(message.systems()))
      );
    }
    if (message.events() != null && message.events().size() > 0) {
      conditions.add(
        where((root, builder) -> root.get("type").in(message.events()))
      );
    }
    if (message.after() != null) {
      conditions.add(
        where((root, builder) ->
          builder.greaterThan(root.get("timestamp"), message.after())
        )
      );
    }
    if (message.before() != null) {
      conditions.add(
        where((root, builder) ->
          builder.lessThan(root.get("timestamp"), message.before())
        )
      );
    }
    printer.loadData(message.recipientId(), message.printId(), conditions);
  }

  @Serdeable
  public static record PrintHistoryCommand(
    String recipientId,
    String printId,
    @Nullable List<String> systems,
    @Nullable List<String> events,
    @Nullable Instant after,
    @Nullable Instant before
  ) {}
}
