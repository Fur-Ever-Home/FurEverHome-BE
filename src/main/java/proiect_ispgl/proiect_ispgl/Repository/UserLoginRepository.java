package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class UserLoginRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public UserLoginRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Account> findByEmailAndType(String email, String accountType) {
        var accounts = jdbc.query("""
                SELECT id_utilizator, email, parola_hash, status_cont, prenume, nume, nume_centru
                FROM utilizatori
                WHERE LOWER(TRIM(email)) = :email AND tip_utilizator = :accountType
                LIMIT 2
                """, Map.of("email", email, "accountType", accountType), (row, index) ->
                new Account(row.getLong("id_utilizator"), row.getString("email"), row.getString("parola_hash"),
                        row.getString("status_cont"), row.getString("prenume"), row.getString("nume"),
                        row.getString("nume_centru")));
        return accounts.size() == 1 ? Optional.of(accounts.getFirst()) : Optional.empty();
    }

    public Optional<String> findActivePasswordHash(long id, String accountType) {
        return jdbc.query("""
                SELECT parola_hash FROM utilizatori
                WHERE id_utilizator = :id AND tip_utilizator = :accountType AND status_cont = 'ACTIV'
                    AND parola_hash IS NOT NULL
                """, Map.of("id", id, "accountType", accountType),
                (row, index) -> row.getString("parola_hash")).stream().findFirst();
    }

    public record Account(long id, String email, String passwordHash, String status, String firstName,
                          String lastName, String shelterName) {
        @Override
        public String toString() {
            return "Account[redacted]";
        }
    }
}
