package proiect_ispgl.proiect_ispgl.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import proiect_ispgl.proiect_ispgl.Repository.UserLoginRepository;
import proiect_ispgl.proiect_ispgl.dto.AccountLoginRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

@Service
public class AccountLoginService {
    private final UserLoginRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;

    public AccountLoginService(UserLoginRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public LoginResult authenticate(AccountLoginRequest request) {
        if (request == null || request.role() == null || request.email() == null
                || request.password() == null || request.password().isBlank() || request.password().length() > 128) {
            return LoginResult.failure(LoginError.INVALID_LOGIN);
        }

        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String accountType = switch (request.role()) {
            case "adopter" -> "Adoptator";
            case "shelter" -> "Centru";
            default -> null;
        };
        if (email.isEmpty() || email.length() > 100 || accountType == null) {
            return LoginResult.failure(LoginError.INVALID_LOGIN);
        }

        var account = users.findByEmailAndType(email, accountType).orElse(null);
        String storedHash = account == null ? null : account.passwordHash();
        boolean hasPasswordHash = storedHash != null && storedHash.startsWith("$argon2id$");
        // Take the same password verification path even when the account or role is absent.
        boolean matched = passwordEncoder.matches(request.password(), hasPasswordHash ? storedHash : dummyPasswordHash);
        if (!matched || !hasPasswordHash) return LoginResult.failure(LoginError.INVALID_CREDENTIALS);

        if ("shelter".equals(request.role()) && "IN_ASTEPTARE".equals(account.status())) {
            return LoginResult.failure(LoginError.SHELTER_APPROVAL_PENDING);
        }
        if (!"ACTIV".equals(account.status())) return LoginResult.failure(LoginError.INVALID_CREDENTIALS);

        return new LoginResult(new AuthenticatedAccount(account.id(), request.role(), accountType,
                account.email().strip(), account.firstName(), account.lastName(), account.shelterName(),
                AccountSessionService.passwordFingerprint(storedHash)), null);
    }

    public record AuthenticatedAccount(long id, String role, String accountType, String email, String firstName,
                                       String lastName, String shelterName,
                                       String passwordFingerprint) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public enum LoginError {
        INVALID_LOGIN,
        INVALID_CREDENTIALS,
        SHELTER_APPROVAL_PENDING
    }

    public record LoginResult(AuthenticatedAccount account, LoginError error) {
        private static LoginResult failure(LoginError error) {
            return new LoginResult(null, error);
        }
    }
}
