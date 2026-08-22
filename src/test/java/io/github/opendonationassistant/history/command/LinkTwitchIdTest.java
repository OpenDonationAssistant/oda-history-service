package io.github.opendonationassistant.history.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.opendonationassistant.history.repository.TwitchIdMappingRepository;
import io.github.opendonationassistant.testutils.AuthenticationGenerator;
import io.micronaut.http.HttpStatus;
import io.micronaut.security.authentication.Authentication;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class LinkTwitchIdTest {

  TwitchIdMappingRepository repository = mock(TwitchIdMappingRepository.class);
  LinkTwitchId controller = new LinkTwitchId(repository);

  @Test
  public void testLinkStoresMappingForAuthenticatedUser() {
    var uuid = UUID.randomUUID();
    var response = controller
      .linkTwitchId(
        AuthenticationGenerator.forUser("recipient-1"),
        new LinkTwitchIdApi.LinkTwitchIdCommand(uuid.toString())
      )
      .join();

    assertEquals(HttpStatus.OK, response.getStatus());
    verify(repository).link("recipient-1", uuid);
  }

  @Test
  public void testLinkReturnsUnauthorizedWithoutPreferredUsername() {
    var response = controller
      .linkTwitchId(
        Authentication.build("unknown", Map.of()),
        new LinkTwitchIdApi.LinkTwitchIdCommand("token-1")
      )
      .join();

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
    verifyNoInteractions(repository);
  }

  @Test
  public void testLinkReturnsBadRequestForBlankRefreshToken() {
    var response = controller
      .linkTwitchId(
        AuthenticationGenerator.forUser("recipient-1"),
        new LinkTwitchIdApi.LinkTwitchIdCommand("")
      )
      .join();

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    verifyNoInteractions(repository);
  }
}
