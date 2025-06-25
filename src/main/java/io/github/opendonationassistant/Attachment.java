package io.github.opendonationassistant;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Attachment(String id, String url, String title) {}
