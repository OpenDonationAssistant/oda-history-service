package io.github.opendonationassistant.history.repository;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Serdeable
@MappedEntity("twitch_id_mapping")
@Schema(description = "Mapping between a recipient and a Twitch refresh token")
public record TwitchIdMappingData(
  @Id
  @MappedProperty("recipient_id")
  @Schema(description = "Recipient ID (streamer/creator)")
  String recipientId,
  @MappedProperty("refresh_token_id")
  @Schema(description = "ID of the linked Twitch refresh token")
  UUID refreshTokenId
) {}
