package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class PasswordResetRepository {
    private static final String ELIGIBLE = """
            status_cont = 'ACTIV' AND tip_utilizator IN ('Adoptator', 'Centru') AND parola_hash IS NOT NULL
            """;
    private final NamedParameterJdbcTemplate jdbc;

    public PasswordResetRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ResetAccount> findByEmail(String email) {
        return find("LOWER(TRIM(email)) = :email", Map.of("email", email));
    }

    public Optional<ResetAccount> findById(long id) {
        return find("id_utilizator = :id", Map.of("id", id));
    }

    private Optional<ResetAccount> find(String condition, Map<String, ?> values) {
        var matches = jdbc.query("SELECT id_utilizator, email, parola_hash FROM utilizatori WHERE "
                        + ELIGIBLE + " AND " + condition + " LIMIT 2", values,
                (row, index) -> new ResetAccount(row.getLong("id_utilizator"), row.getString("email"),
                        row.getString("parola_hash")));
        return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
    }

    public boolean replacePassword(ResetAccount account, String newHash) {
        return jdbc.update("""
                UPDATE utilizatori SET parola_hash = :newHash
                WHERE id_utilizator = :id AND email = :email AND parola_hash = :oldHash AND
                """ + ELIGIBLE, Map.of("id", account.id(), "email", account.email(),
                "oldHash", account.passwordHash(), "newHash", newHash)) == 1;
    }

    public record ResetAccount(long id, String email, String passwordHash) {
        @Override
        public String toString() {
            return "ResetAccount[redacted]";
        }
    }
}
