package io.github.opendonationassistant.history.model;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.history.repository.HistoryItemData;
import io.github.opendonationassistant.history.repository.HistoryItemDataRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.http.MediaType;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class Printer {

  private ODALogger log = new ODALogger(this);
  private final HistoryItemDataRepository repository;
  private final Map<String, PrintableData> map;

  @Inject
  public Printer(
    HistoryItemDataRepository dataRepository,
    Map<String, PrintableData> map
  ) {
    this.repository = dataRepository;
    this.map = map;
  }

  public boolean isReady(String recipientId, String printId) {
    if (!map.containsKey(printId)) {
      return false;
    }
    return map.get(printId).recipientId().equals(recipientId);
  }

  String print(PrintableData data) {
    StringBuilder sb = new StringBuilder();
    sb
      .append(
        "Event;System;Timestamp;Nickname;Amount;Message;Goal;Roulette;Actions;LevelName;ItemCount;"
      )
      .append("\n");

    data
      .data()
      .forEach(item ->
        sb
          .append(item.type())
          .append(";")
          .append(item.system())
          .append(";")
          .append(item.timestamp())
          .append(";\"")
          .append(item.nickname())
          .append("\";")
          .append(
            Optional.ofNullable(item.amount())
              .map(it -> "%s.%s".formatted(it.getMajor(), it.getMinor()))
              .orElse("")
          )
          .append(";\"")
          .append(item.message())
          .append("\";\"")
          .append(
            item
              .goals()
              .stream()
              .findFirst()
              .map(it -> it.goalTitle())
              .orElse("")
          )
          .append("\";\"")
          .append(
            item
              .reelResults()
              .stream()
              .findFirst()
              .map(it -> it.title())
              .orElse("")
          )
          .append("\";\"")
          .append(
            item
              .actions()
              .stream()
              .map(it -> it.name())
              .reduce("", (a, b) -> a + "," + b)
          )
          .append("\";\"")
          .append(Optional.ofNullable(item.levelName()).orElse(""))
          .append("\";")
          .append(
            Optional.ofNullable(item.count())
              .map(it -> it.toString())
              .orElse("")
          )
          .append(";")
          .append("\n")
      );
    return sb.toString();
  }

  public StreamedFile print(String printId) {
    PrintableData printableData = Optional.ofNullable(
      map.get(printId)
    ).orElseThrow();
    byte[] bytes = print(printableData).getBytes(StandardCharsets.UTF_8);

    // Optionally give a unique filename (timestamp)
    String filename =
      "export-" +
      ZonedDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
      ".csv";

    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    StreamedFile streamedFile = new StreamedFile(
      bais,
      MediaType.TEXT_CSV_TYPE
    ).attach(filename);
    map.remove(printId);
    return streamedFile;
  }

  public void loadData(
    String recipientId,
    String printId,
    List<PredicateSpecification<HistoryItemData>> predicates
  ) {
    map.put(printId, new PrintableData(recipientId, load(predicates)));
  }

  private List<HistoryItemData> load(
    List<PredicateSpecification<HistoryItemData>> predicates
  ) {
    Page<HistoryItemData> data = loadPage(predicates, 0);
    List<HistoryItemData> collected = new ArrayList<>(
      (int) data.getTotalSize()
    );
    collected.addAll(data.getContent());
    while (data.hasNext()) {
      data = loadPage(predicates, data.getPageNumber() + 1);
      collected.addAll(data.getContent());
    }
    return collected;
  }

  private Page<HistoryItemData> loadPage(
    List<PredicateSpecification<HistoryItemData>> predicates,
    int page
  ) {
    Pageable pageable = Pageable.from(page, 100);
    return repository.findAll(
      (root, builder) -> {
        return builder.and(
          predicates
            .stream()
            .map(it -> it.toPredicate(root, builder))
            .toArray(Predicate[]::new)
        );
      },
      pageable
    );
  }

  @Serdeable
  public static record PrintableData(
    String recipientId,
    List<HistoryItemData> data
  ) {}
}
