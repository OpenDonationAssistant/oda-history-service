package io.github.opendonationassistant.history.listener.handlers;

import io.github.opendonationassistant.commons.logging.ODALogger;
import io.github.opendonationassistant.events.AbstractMessageHandler;
import io.github.opendonationassistant.events.HasRecipientId;
import io.github.opendonationassistant.history.repository.TwitchIdMappingRepository;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Singleton
public class TokenSettingsChangedHandler
  extends AbstractMessageHandler<
    TokenSettingsChangedHandler.TokenSettingsChanged
  > {

  private final ODALogger log = new ODALogger(this);
  private final TwitchIdMappingRepository repository;

  @Inject
  public TokenSettingsChangedHandler(
    ObjectMapper mapper,
    TwitchIdMappingRepository repository
  ) {
    super(mapper);
    this.repository = repository;
  }

  @Serdeable
  public static record TokenSettingsChanged(
    String id,
    String type,
    String recipientId,
    String system,
    boolean enabled,
    boolean deleted,
    Map<String, Object> settings
  )
    implements HasRecipientId {}

  @Override
  public void handle(TokenSettingsChanged message) throws IOException {
    var recipientId = message.recipientId();
    if (repository.findByRecipientId(recipientId).isPresent()) {
      return;
    }
    log.info(
      "Linking Twitch token for recipient",
      Map.of("recipientId", recipientId, "tokenId", message.id())
    );
    repository.link(recipientId, UUID.fromString(message.id()));
  }
}
