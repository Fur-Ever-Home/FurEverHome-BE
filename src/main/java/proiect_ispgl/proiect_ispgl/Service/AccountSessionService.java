package proiect_ispgl.proiect_ispgl.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import proiect_ispgl.proiect_ispgl.Repository.UserLoginRepository;
import proiect_ispgl.proiect_ispgl.Service.AccountLoginService.AuthenticatedAccount;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AccountSessionService {
    public static final String CSRF_HEADER = "X-CSRF-Token";
    public static final String VERIFIED_SESSION_ATTRIBUTE = "proiect_ispgl.verifiedSession";
    private static final String SESSION_ATTRIBUTE = "proiect_ispgl.accountSession";

    private final UserLoginRepository users;
    private final SecureRandom random = new SecureRandom();

    public AccountSessionService(UserLoginRepository users) {
        this.users = users;
    }

    public void start(HttpServletRequest request, AuthenticatedAccount account) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) invalidate(oldSession);

        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        request.getSession(true).setAttribute(SESSION_ATTRIBUTE, new AccountSession(account, csrfToken));
    }

    public AccountSession verify(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) return null;

        Object storedSession;
        try {
            storedSession = httpSession.getAttribute(SESSION_ATTRIBUTE);
        } catch (IllegalStateException invalidated) {
            return null;
        }
        if (!(storedSession instanceof AccountSession session)) return null;

        var account = session.account();
        if (users.findActivePasswordHash(account.id(), account.accountType()).map(AccountSessionService::passwordFingerprint)
                .filter(account.passwordFingerprint()::equals).isPresent()) {
            return session;
        }
        invalidate(httpSession);
        return null;
    }

    static String passwordFingerprint(String hash) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(hash.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Could not verify the session password fingerprint.", exception);
        }
    }

    private static void invalidate(HttpSession session) {
        try {
            session.invalidate();
        } catch (IllegalStateException alreadyInvalidated) {
            // Another request ended the same session.
        }
    }

    public record AccountSession(AuthenticatedAccount account, String csrfToken) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public boolean matchesCsrfToken(String submitted) {
            return submitted != null && submitted.length() == csrfToken.length()
                    && MessageDigest.isEqual(csrfToken.getBytes(StandardCharsets.UTF_8),
                    submitted.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String toString() {
            return "AccountSession[redacted]";
        }
    }
}
