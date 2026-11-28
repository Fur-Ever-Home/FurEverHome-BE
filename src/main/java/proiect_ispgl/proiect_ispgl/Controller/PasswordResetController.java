package proiect_ispgl.proiect_ispgl.Controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proiect_ispgl.proiect_ispgl.Service.PasswordResetService;
import proiect_ispgl.proiect_ispgl.Service.PasswordResetService.Result;

import java.util.Map;

@RestController
@RequestMapping(PasswordResetController.PATH)
public class PasswordResetController {
    public static final String PATH = "/api/auth/password-reset";
    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping(path = "/request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> request(@RequestBody EmailRequest input) {
        return response(service.request(input.email()));
    }

    @PostMapping(path = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> confirm(@RequestBody ResetConfirmation input) {
        return response(service.confirm(input.token(), input.newPassword()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> invalidBody() {
        return response(Result.INVALID_RESET_REQUEST);
    }

    private static ResponseEntity<?> response(Result result) {
        return switch (result) {
            case ACCEPTED -> ResponseEntity.accepted().cacheControl(CacheControl.noStore()).build();
            case CHANGED -> ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
            case INVALID_RESET_REQUEST, INVALID_NEW_PASSWORD, INVALID_RESET_TOKEN ->
                    error(HttpStatus.BAD_REQUEST, result);
            case RESET_UNAVAILABLE -> error(HttpStatus.SERVICE_UNAVAILABLE, result);
        };
    }

    private static ResponseEntity<?> error(HttpStatus status, Result result) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(Map.of("code", result.name()));
    }

    private record EmailRequest(String email) {}

    private record ResetConfirmation(
            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String token,
            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String newPassword) {
        @Override
        public String toString() {
            return "ResetConfirmation[redacted]";
        }
    }
}
