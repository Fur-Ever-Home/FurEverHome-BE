package proiect_ispgl.proiect_ispgl.Service;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import proiect_ispgl.proiect_ispgl.Repository.PasswordResetRepository;
import proiect_ispgl.proiect_ispgl.Repository.PasswordResetRepository.ResetAccount;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class PasswordResetService {
    private static final Logger LOG = LoggerFactory.getLogger(PasswordResetService.class);
    private static final String HMAC = "HmacSHA256";
    private static final Base64.Encoder BASE64 = Base64.getUrlEncoder().withoutPadding();
    private static final Pattern TOKEN = Pattern.compile(
            "(([1-9][0-9]{0,18})\\.([1-9][0-9]{0,18})\\.[A-Za-z0-9_-]{22})\\.([A-Za-z0-9_-]{43})");

    private final PasswordResetRepository accounts;
    private final PasswordEncoder passwords;
    private final JavaMailSender mail;
    private final TaskExecutor executor;
    private final Clock clock;
    private final String sender;
    private final String frontendUrl;
    private final String signingKey;
    private final long ttlSeconds;
    private final boolean canSendMail;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetRepository accounts, PasswordEncoder passwords, JavaMailSender mail,
                                @Qualifier("passwordResetExecutor") TaskExecutor executor, Clock clock,
                                @Value("${app.password-reset.from}") String sender,
                                @Value("${app.password-reset.frontend-url}") String frontendUrl,
                                @Value("${app.password-reset.signing-key}") String signingKey,
                                @Value("${app.password-reset.ttl-seconds}") long ttlSeconds) {
        this.accounts = accounts;
        this.passwords = passwords;
        this.mail = mail;
        this.executor = executor;
        this.clock = clock;
        this.sender = sender.strip();
        this.frontendUrl = frontendUrl.strip().replaceAll("/+$", "");
        this.signingKey = signingKey;
        this.ttlSeconds = ttlSeconds;
        this.canSendMail = validMailAddress(this.sender) && validFrontendUrl(this.frontendUrl)
                && ttlSeconds > 0 && ttlSeconds <= 3600;
    }

    public Result request(String address) {
        String email = address == null ? "" : address.strip().toLowerCase(Locale.ROOT);
        if (!AccountRegistrationService.isValidEmail(email)) return Result.INVALID_RESET_REQUEST;
        if (!hasSigningKey() || !canSendMail) return Result.RESET_UNAVAILABLE;

        try {
            // Account lookup and mail run after the same generic response path for every valid address.
            executor.execute(() -> sendIfEligible(email));
        } catch (TaskRejectedException exception) {
            return Result.RESET_UNAVAILABLE;
        }
        return Result.ACCEPTED;
    }

    public Result confirm(String token, String newPassword) {
        if (!AccountRegistrationService.isValidNewPassword(newPassword)) return Result.INVALID_NEW_PASSWORD;
        if (!hasSigningKey()) return Result.RESET_UNAVAILABLE;
        if (token == null) return Result.INVALID_RESET_TOKEN;

        var matcher = TOKEN.matcher(token);
        if (!matcher.matches()) return Result.INVALID_RESET_TOKEN;

        long id;
        long expiresAt;
        try {
            id = Long.parseLong(matcher.group(2));
            expiresAt = Long.parseLong(matcher.group(3));
        } catch (NumberFormatException exception) {
            return Result.INVALID_RESET_TOKEN;
        }
        if (expired(expiresAt)) return Result.INVALID_RESET_TOKEN;

        var account = accounts.findById(id).orElse(null);
        if (account == null || !MessageDigest.isEqual(matcher.group(4).getBytes(StandardCharsets.US_ASCII),
                sign(matcher.group(1), account).getBytes(StandardCharsets.US_ASCII))) {
            return Result.INVALID_RESET_TOKEN;
        }

        String newHash = passwords.encode(newPassword);
        // The conditional update consumes all links tied to the old hash, even during concurrent requests.
        return !expired(expiresAt) && accounts.replacePassword(account, newHash)
                ? Result.CHANGED : Result.INVALID_RESET_TOKEN;
    }

    private void sendIfEligible(String email) {
        try {
            accounts.findByEmail(email).ifPresent(this::sendMail);
        } catch (RuntimeException exception) {
            LOG.warn("A password reset email could not be prepared or sent ({}).",
                    exception.getClass().getSimpleName());
        }
    }

    private void sendMail(ResetAccount account) {
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        long expiresAt = clock.instant().plusSeconds(ttlSeconds).getEpochSecond();
        String payload = account.id() + "." + expiresAt + "." + BASE64.encodeToString(nonce);
        String link = frontendUrl + "/login#reset=" + payload + "." + sign(payload, account);

        var message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(account.email().strip());
        message.setSubject("FurEverHome - Resetarea parolei");
        message.setText("""
                Ai primit acest e-mail pentru o cerere de resetare a parolei FurEverHome.

                Deschide linkul pentru a alege o parolă nouă:
                %s

                Linkul expiră și poate fi folosit o singură dată. Dacă nu ai cerut resetarea, poți ignora acest mesaj.
                """.formatted(link));
        mail.send(message);
    }

    private boolean expired(long expiresAt) {
        return clock.instant().getEpochSecond() >= expiresAt;
    }

    private boolean hasSigningKey() {
        return !signingKey.isBlank() && signingKey.length() >= 32;
    }

    private String sign(String payload, ResetAccount account) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), HMAC));
            String input = "password-reset:v1:" + payload + ":" + account.email() + ":" + account.passwordHash();
            return BASE64.encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not sign a password reset link.", exception);
        }
    }

    private static boolean validMailAddress(String value) {
        try {
            var address = new InternetAddress(value, true);
            address.validate();
            return value.equals(address.getAddress()) && !address.isGroup();
        } catch (AddressException exception) {
            return false;
        }
    }

    private static boolean validFrontendUrl(String value) {
        try {
            URI url = URI.create(value);
            String host = url.getHost();
            boolean loopback = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "[::1]".equals(host);
            return host != null && ("https".equalsIgnoreCase(url.getScheme())
                    || (loopback && "http".equalsIgnoreCase(url.getScheme())))
                    && url.getRawUserInfo() == null && url.getRawQuery() == null && url.getRawFragment() == null
                    && url.getPort() != 0 && url.getPort() <= 65535;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public enum Result {
        ACCEPTED, CHANGED, INVALID_RESET_REQUEST, INVALID_NEW_PASSWORD, INVALID_RESET_TOKEN, RESET_UNAVAILABLE
    }
}
