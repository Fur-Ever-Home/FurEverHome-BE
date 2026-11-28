package proiect_ispgl.proiect_ispgl.Controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import proiect_ispgl.proiect_ispgl.Service.ShelterApprovalService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(ShelterApprovalService.PATH)
public class ShelterApprovalController {
    private static final MediaType HTML = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);

    private final ShelterApprovalService approval;

    public ShelterApprovalController(ShelterApprovalService approval) {
        this.approval = approval;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> confirmation(@RequestParam(name = "token", required = false) String token) {
        if (!approval.canApprove(token)) return unavailable();

        return page(HttpStatus.OK, "Aprobă contul centrului", """
                <p>Confirmă activarea contului de centru asociat acestui link.</p>
                <form method="post" action="shelter-approval">
                  <input type="hidden" name="token" value="%s">
                  <button type="submit">Aprobă centrul</button>
                </form>
                """.formatted(token));
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> approve(@RequestParam(name = "token", required = false) String token) {
        if (!approval.approve(token)) return unavailable();
        return page(HttpStatus.OK, "Centrul a fost aprobat", "<p>Contul centrului este activ.</p>");
    }

    private ResponseEntity<String> unavailable() {
        return page(HttpStatus.NOT_FOUND, "Link indisponibil",
                "<p>Linkul de aprobare este nevalid sau contul a fost deja aprobat.</p>");
    }

    private ResponseEntity<String> page(HttpStatus status, String title, String content) {
        return ResponseEntity.status(status)
                .contentType(HTML)
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'")
                .body("""
                        <!doctype html>
                        <html lang="ro">
                        <head>
                          <meta charset="utf-8">
                          <meta name="viewport" content="width=device-width, initial-scale=1">
                          <title>%s · FurEverHome</title>
                        </head>
                        <body><main><h1>%s</h1>%s</main></body>
                        </html>
                        """.formatted(title, title, content));
    }
}
