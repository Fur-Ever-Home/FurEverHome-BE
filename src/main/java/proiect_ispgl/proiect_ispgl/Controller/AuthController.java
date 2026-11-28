package proiect_ispgl.proiect_ispgl.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proiect_ispgl.proiect_ispgl.Service.AccountRegistrationService;
import proiect_ispgl.proiect_ispgl.dto.AccountRegistrationRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountRegistrationService registrationService;

    public AuthController(AccountRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody AccountRegistrationRequest request) {
        var result = registrationService.register(request);
        return switch (result) {
            case CREATED -> ResponseEntity.status(HttpStatus.CREATED).build();
            case INVALID_REGISTRATION -> invalidBody();
            case EMAIL_ALREADY_USED, PHONE_ALREADY_USED, CUI_ALREADY_USED ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", result.name()));
            case APPROVAL_NOTIFICATION_UNAVAILABLE ->
                    ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("code", result.name()));
        };
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> invalidBody() {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REGISTRATION"));
    }
}
