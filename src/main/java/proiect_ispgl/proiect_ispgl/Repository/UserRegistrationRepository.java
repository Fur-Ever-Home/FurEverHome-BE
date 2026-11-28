package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class UserRegistrationRepository {
    private static final int SHELTER_CUI_LOCK_NAMESPACE = "fureverhome.shelter-registration.cui".hashCode();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserRegistrationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean emailExists(String email) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM utilizatori WHERE LOWER(TRIM(email)) = :email)",
                Map.of("email", email), Boolean.class));
    }

    public boolean phoneExists(String countryCode, String phone) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM utilizatori WHERE cod_tara = :countryCode AND telefon = :phone)
                """, Map.of("countryCode", countryCode, "phone", phone), Boolean.class));
    }

    public void lockCui(String cui) {
        jdbcTemplate.query("SELECT pg_advisory_xact_lock(:namespace, :cuiKey)",
                Map.of("namespace", SHELTER_CUI_LOCK_NAMESPACE, "cuiKey", cui.hashCode()), rows -> null);
    }

    public boolean cuiExists(String cui) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM utilizatori
                    WHERE TRIM(LEADING '0' FROM REGEXP_REPLACE(REPLACE(UPPER(TRIM(cui_cif)), ' ', ''), '^RO', '')) = :cui
                )
                """, Map.of("cui", cui), Boolean.class));
    }

    public boolean insertAdopter(String email, String passwordHash, String lastName, String firstName,
                                 String countryCode, String phone) {
        return jdbcTemplate.update("""
                INSERT INTO utilizatori (email, parola_hash, nume, prenume, cod_tara, telefon, tip_utilizator, status_cont)
                VALUES (:email, :passwordHash, :lastName, :firstName, :countryCode, :phone, 'Adoptator', 'ACTIV')
                ON CONFLICT DO NOTHING
                """, Map.of("email", email, "passwordHash", passwordHash, "lastName", lastName,
                "firstName", firstName, "countryCode", countryCode, "phone", phone)) == 1;
    }

    public Optional<Long> insertShelter(String email, String passwordHash, String shelterName, String cui,
                                        String countryCode, String phone) {
        var values = new MapSqlParameterSource(Map.of("email", email, "passwordHash", passwordHash,
                "shelterName", shelterName, "cui", cui, "countryCode", countryCode, "phone", phone));
        var keyHolder = new GeneratedKeyHolder();
        int inserted = jdbcTemplate.update("""
                INSERT INTO utilizatori (email, parola_hash, nume_centru, cui_cif, cod_tara, telefon, tip_utilizator, status_cont)
                VALUES (:email, :passwordHash, :shelterName, :cui, :countryCode, :phone, 'Centru', 'IN_ASTEPTARE')
                ON CONFLICT DO NOTHING
                """, values, keyHolder, new String[]{"id_utilizator"});
        if (inserted == 0) return Optional.empty();

        Number key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("The shelter registration returned no account ID.");
        return Optional.of(key.longValue());
    }

    public Optional<String> findPendingShelterPasswordHash(long id) {
        return jdbcTemplate.query("""
                SELECT parola_hash FROM utilizatori
                WHERE id_utilizator = :id AND tip_utilizator = 'Centru' AND status_cont = 'IN_ASTEPTARE'
                """, Map.of("id", id), (row, index) -> row.getString("parola_hash")).stream().findFirst();
    }

    public boolean activatePendingShelter(long id, String passwordHash) {
        return jdbcTemplate.update("""
                UPDATE utilizatori
                SET status_cont = 'ACTIV'
                WHERE id_utilizator = :id AND tip_utilizator = 'Centru' AND status_cont = 'IN_ASTEPTARE'
                    AND parola_hash = :passwordHash
                """, Map.of("id", id, "passwordHash", passwordHash)) == 1;
    }
}
