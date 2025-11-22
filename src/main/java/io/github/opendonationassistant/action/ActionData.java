package io.github.opendonationassistant.action;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("actions")
public record ActionData(@Id String id, String name) {}
