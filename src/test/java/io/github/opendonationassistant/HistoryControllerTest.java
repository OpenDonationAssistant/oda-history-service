package io.github.opendonationassistant;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;

@MicronautTest(environments = "allinone")
public class HistoryControllerTest {

  @Test
  public void testGettingHistoryPage() {
  }
}
