package proiect_ispgl.proiect_ispgl.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proiect_ispgl.proiect_ispgl.Service.AccountLoginService;
import proiect_ispgl.proiect_ispgl.Service.AccountLoginService.AuthenticatedAccount;
import proiect_ispgl.proiect_ispgl.Service.AccountSessionService;
import proiect_ispgl.proiect_ispgl.Service.AccountSessionService.AccountSession;
import proiect_ispgl.proiect_ispgl.dto.AccountLoginRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AccountLoginController {
    private final AccountLoginService loginService;
    private final AccountSessionService sessions;

    public AccountLoginController(AccountLoginService loginService, AccountSessionService sessions) {
        this.loginService = loginService;
        this.sessions = sessions;
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody AccountLoginRequest credentials, HttpServletRequest request) {
        var result = loginService.authenticate(credentials);
        if (result.error() != null) {
            var status = switch (result.error()) {
                case INVALID_LOGIN -> HttpStatus.BAD_REQUEST;
                case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
                case SHELTER_APPROVAL_PENDING -> HttpStatus.FORBIDDEN;
            };
            return reply(status, Map.of("code", result.error().name()));
        }

        sessions.start(request, result.account());
        return reply(HttpStatus.OK, SessionUser.from(result.account()));
    }

    @GetMapping("/session")
    public ResponseEntity<SessionUser> session(
            @RequestAttribute(AccountSessionService.VERIFIED_SESSION_ATTRIBUTE) AccountSession session) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(AccountSessionService.CSRF_HEADER, session.csrfToken())
                .body(SessionUser.from(session.account()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> invalidBody() {
        return reply(HttpStatus.BAD_REQUEST, Map.of("code", "INVALID_LOGIN"));
    }

    private static ResponseEntity<?> reply(HttpStatus status, Object body) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(body);
    }

    private record SessionUser(String role, String email, String firstName, String lastName, String shelterName) {
        private static SessionUser from(AuthenticatedAccount account) {
            return new SessionUser(account.role(), account.email(), account.firstName(), account.lastName(),
                    account.shelterName());
        }
    }
}
