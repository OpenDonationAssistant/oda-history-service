package io.github.opendonationassistant.history.command;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.history.model.Printer;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.SerdeImport;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;

@Controller
public class DownloadCsv extends BaseController {

  private final Printer printer;

  @Inject
  public DownloadCsv(Printer printer) {
    this.printer = printer;
  }

  @Get(value = "/history/csv/{id}/status")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<CsvStatus> getCsvStatus(
    Authentication auth,
    @PathVariable String id
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    return HttpResponse.ok(new CsvStatus(printer.isReady(ownerId.get(), id)));
  }

  @Get(value = "/history/csv/{id}", produces = MediaType.TEXT_CSV)
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public HttpResponse<StreamedFile> downloadCsv(
    Authentication auth,
    @PathVariable String id
  ) {
    var ownerId = getOwnerId(auth);
    if (ownerId.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    if (!printer.isReady(ownerId.get(), id)) {
      return HttpResponse.noContent();
    }

    // Return with content-disposition set by StreamedFile#attach
    final StreamedFile content = printer.print(id);
    return HttpResponse.ok(content).contentLength(content.getLength());
  }

  @Serdeable
  public static record CsvStatus(Boolean ready) {}
}
