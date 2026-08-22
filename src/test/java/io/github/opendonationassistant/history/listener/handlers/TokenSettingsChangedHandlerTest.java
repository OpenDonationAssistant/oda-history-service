package io.github.opendonationassistant.history.listener.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.opendonationassistant.history.repository.TwitchIdMappingData;
import io.github.opendonationassistant.history.repository.TwitchIdMappingRepository;
import io.micronaut.serde.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class TokenSettingsChangedHandlerTest {

  private static final UUID TOKEN_ID = UUID.fromString(
    "11111111-1111-1111-1111-111111111111"
  );

  TwitchIdMappingRepository repository = mock(TwitchIdMappingRepository.class);
  ObjectMapper mapper = mock(ObjectMapper.class);
  TokenSettingsChangedHandler handler = new TokenSettingsChangedHandler(
    mapper,
    repository
  );

  private TokenSettingsChangedHandler.TokenSettingsChanged message() {
    return new TokenSettingsChangedHandler.TokenSettingsChanged(
      TOKEN_ID.toString(),
      "refreshToken",
      "recipient-1",
      "Twitch",
      true,
      false,
      Map.of()
    );
  }

  @Test
  public void testHandlesTokenSettingsChangedMessages() {
    assertEquals("TokenSettingsChanged", handler.type());
  }

  @Test
  public void testCreatesMappingWhenMissing() throws IOException {
    when(repository.findByRecipientId("recipient-1")).thenReturn(
      Optional.empty()
    );

    handler.handle(message());

    verify(repository).link("recipient-1", TOKEN_ID);
  }

  @Test
  public void testDoesNotCreateMappingForOtherSystems() throws IOException {
    var message = new TokenSettingsChangedHandler.TokenSettingsChanged(
      TOKEN_ID.toString(),
      "refreshToken",
      "recipient-1",
      "Kick",
      true,
      false,
      Map.of()
    );

    handler.handle(message);

    verifyNoInteractions(repository);
  }

  @Test
  public void testKeepsExistingMapping() throws IOException {
    when(repository.findByRecipientId("recipient-1"))
      .thenReturn(
        Optional.of(new TwitchIdMappingData("recipient-1", TOKEN_ID))
      );

    handler.handle(message());

    verify(repository, never()).link(any(), any());
  }
}
