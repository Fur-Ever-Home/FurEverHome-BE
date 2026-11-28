package proiect_ispgl.proiect_ispgl.Service;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import proiect_ispgl.proiect_ispgl.Repository.UserRegistrationRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ShelterApprovalService {
    public static final String PATH = "/api/auth/shelter-approval";

    private static final Logger LOG = LoggerFactory.getLogger(ShelterApprovalService.class);
    private static final Pattern TOKEN = Pattern.compile("([1-9][0-9]{0,18})\\.([A-Za-z0-9_-]{43})");
    private static final String HMAC = "HmacSHA256";

    private final UserRegistrationRepository users;
    private final AccountMailDelivery mailSender;
    private final String recipient;
    private final String sender;
    private final String baseUrl;
    private final String signingKey;

    public ShelterApprovalService(UserRegistrationRepository users, AccountMailDelivery mailSender,
                                  @Value("${app.shelter-approval.recipient:}") String recipient,
                                  @Value("${app.shelter-approval.from:${spring.mail.username:}}") String sender,
                                  @Value("${app.shelter-approval.base-url:}") String baseUrl,
                                  @Value("${app.shelter-approval.signing-key:}") String signingKey) {
        this.users = users;
        this.mailSender = mailSender;
        this.recipient = recipient.strip();
        this.sender = sender.strip();
        this.baseUrl = baseUrl.strip();
        this.signingKey = signingKey;
    }

    public void notifyTeam(long id, String passwordHash, String shelterName, String cui, String email) {
        String link;
        try {
            validateMailbox(recipient);
            validateMailbox(sender);
            if (signingKey.isBlank() || signingKey.length() < 32) throw new IllegalArgumentException();
            link = approvalLink(id, passwordHash);
        } catch (IllegalArgumentException | AddressException exception) {
            LOG.warn("Shelter approval mail configuration is missing or invalid (recipient, sender, base URL or signing key).");
            throw new NotificationUnavailable();
        }

        var message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setFrom(sender);
        message.setSubject("FurEverHome - Centru nou în așteptarea aprobării");
        message.setText("""
                Un centru a solicitat un cont FurEverHome.

                Centru: %s
                CUI/CIF: %s
                E-mail: %s

                Verifică datele și deschide linkul pentru a confirma aprobarea:
                %s
                """.formatted(shelterName, cui, email, link));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            LOG.warn("Shelter approval email could not be sent ({}).", exception.getClass().getSimpleName());
            throw new NotificationUnavailable();
        }
    }

    public boolean canApprove(String token) {
        return matchingAccount(token).isPresent();
    }

    public boolean approve(String token) {
        return matchingAccount(token)
                .map(account -> users.activatePendingShelter(account.id(), account.passwordHash()))
                .orElse(false);
    }

    private Optional<PendingAccount> matchingAccount(String token) {
        if (token == null || signingKey.isBlank() || signingKey.length() < 32) return Optional.empty();
        var matcher = TOKEN.matcher(token);
        if (!matcher.matches()) return Optional.empty();

        long id;
        try {
            id = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
        byte[] signature = matcher.group(2).getBytes(StandardCharsets.US_ASCII);
        return users.findPendingShelterPasswordHash(id)
                .filter(hash -> MessageDigest.isEqual(signature, sign(id, hash).getBytes(StandardCharsets.US_ASCII)))
                .map(hash -> new PendingAccount(id, hash));
    }

    private String approvalLink(long id, String passwordHash) {
        URI url = URI.create(baseUrl);
        String host = url.getHost();
        boolean loopback = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "[::1]".equals(host);
        boolean allowedScheme = "https".equalsIgnoreCase(url.getScheme())
                || (loopback && "http".equalsIgnoreCase(url.getScheme()));
        if (host == null || !allowedScheme || url.getRawUserInfo() != null
                || url.getRawQuery() != null || url.getRawFragment() != null) {
            throw new IllegalArgumentException();
        }
        return baseUrl.replaceAll("/+$", "") + PATH + "?token=" + id + "." + sign(id, passwordHash);
    }

    private String sign(long id, String passwordHash) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), HMAC));
            byte[] message = ("shelter-approval:" + id + ":" + passwordHash).getBytes(StandardCharsets.UTF_8);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(message));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not sign a shelter approval link.", exception);
        }
    }

    private static void validateMailbox(String mailbox) throws AddressException {
        var address = new InternetAddress(mailbox, true);
        address.validate();
        if (!mailbox.equals(address.getAddress()) || address.isGroup()) throw new AddressException();
    }

    private record PendingAccount(long id, String passwordHash) {}

    static class NotificationUnavailable extends RuntimeException {}
}
