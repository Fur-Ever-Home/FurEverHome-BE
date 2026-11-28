package proiect_ispgl.proiect_ispgl.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import proiect_ispgl.proiect_ispgl.Repository.UserRegistrationRepository;
import proiect_ispgl.proiect_ispgl.dto.AccountRegistrationRequest;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AccountRegistrationService {
    private static final Pattern EMAIL = Pattern.compile("[a-z0-9._+%\\-]+@[a-z0-9.\\-]+\\.[a-z]+");
    private static final Pattern COUNTRY_CODE = Pattern.compile("\\+[1-9][0-9]{0,2}");
    private static final Pattern PHONE = Pattern.compile("[0-9]{1,20}");
    private static final Pattern PHONE_SEPARATORS = Pattern.compile("[\\s().-]");
    private static final Pattern CUI = Pattern.compile("(?:RO)?([0-9]{1,30})");

    private final UserRegistrationRepository users;
    private final PasswordEncoder passwordEncoder;
    private final ShelterApprovalService shelterApproval;
    private final TransactionTemplate shelterTransactions;

    public AccountRegistrationService(UserRegistrationRepository users, PasswordEncoder passwordEncoder,
                                      PlatformTransactionManager transactionManager, ShelterApprovalService shelterApproval) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.shelterApproval = shelterApproval;
        this.shelterTransactions = new TransactionTemplate(transactionManager);
        this.shelterTransactions.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public Result register(AccountRegistrationRequest request) {
        String email = trim(request.email()).toLowerCase(Locale.ROOT);
        String countryCode = trim(request.countryCode());
        String phone = PHONE_SEPARATORS.matcher(trim(request.phone())).replaceAll("");
        String password = request.password();
        if (!isValidEmail(email)
                || !COUNTRY_CODE.matcher(countryCode).matches() || !PHONE.matcher(phone).matches()
                || !isValidNewPassword(password)) {
            return Result.INVALID_REGISTRATION;
        }

        var contact = new Contact(email, countryCode, phone);
        return switch (trim(request.role())) {
            case "adopter" -> registerAdopter(request, contact);
            case "shelter" -> registerShelter(request, contact);
            default -> Result.INVALID_REGISTRATION;
        };
    }

    private Result registerAdopter(AccountRegistrationRequest request, Contact contact) {
        String firstName = trim(request.firstName());
        String lastName = trim(request.lastName());
        if (firstName.isEmpty() || firstName.length() > 100 || lastName.isEmpty() || lastName.length() > 100) {
            return Result.INVALID_REGISTRATION;
        }
        if (users.emailExists(contact.email())) return Result.EMAIL_ALREADY_USED;

        String passwordHash = passwordEncoder.encode(request.password());
        boolean inserted = users.insertAdopter(contact.email(), passwordHash, lastName, firstName,
                contact.countryCode(), contact.phone());
        return inserted ? Result.CREATED : existingContactConflict(contact);
    }

    private Result registerShelter(AccountRegistrationRequest request, Contact contact) {
        String shelterName = trim(request.shelterName());
        String cui = normalizeCui(request.cui());
        if (shelterName.isEmpty() || shelterName.length() > 150 || cui.isEmpty()) {
            return Result.INVALID_REGISTRATION;
        }
        if (users.emailExists(contact.email())) return Result.EMAIL_ALREADY_USED;

        String passwordHash = passwordEncoder.encode(request.password());
        try {
            return shelterTransactions.execute(transaction -> {
                // The lock and the lookup must run in separate statements in the same transaction.
                users.lockCui(cui);
                if (users.cuiExists(cui)) return Result.CUI_ALREADY_USED;

                var id = users.insertShelter(contact.email(), passwordHash, shelterName, cui,
                        contact.countryCode(), contact.phone());
                if (id.isEmpty()) return existingContactConflict(contact);

                // An SMTP failure rolls back the insert so the same registration can be retried.
                shelterApproval.notifyTeam(id.get(), passwordHash, shelterName, cui, contact.email());
                return Result.CREATED;
            });
        } catch (ShelterApprovalService.NotificationUnavailable exception) {
            return Result.APPROVAL_NOTIFICATION_UNAVAILABLE;
        }
    }

    private Result existingContactConflict(Contact contact) {
        if (users.emailExists(contact.email())) return Result.EMAIL_ALREADY_USED;
        if (users.phoneExists(contact.countryCode(), contact.phone())) return Result.PHONE_ALREADY_USED;
        throw new IllegalStateException("Could not insert a user account.");
    }

    private static String normalizeCui(String value) {
        var matcher = CUI.matcher(trim(value).replace(" ", "").toUpperCase(Locale.ROOT));
        return matcher.matches() ? matcher.group(1).replaceFirst("^0+", "") : "";
    }

    private static String trim(String value) {
        return value == null ? "" : value.strip();
    }

    static boolean isValidEmail(String email) {
        return email.length() <= 100 && EMAIL.matcher(email).matches();
    }

    static boolean isValidNewPassword(String password) {
        return password != null && password.length() >= 8 && password.length() <= 128 && !password.isBlank();
    }

    private record Contact(String email, String countryCode, String phone) {}

    public enum Result {
        CREATED,
        INVALID_REGISTRATION,
        EMAIL_ALREADY_USED,
        PHONE_ALREADY_USED,
        CUI_ALREADY_USED,
        APPROVAL_NOTIFICATION_UNAVAILABLE
    }
}
