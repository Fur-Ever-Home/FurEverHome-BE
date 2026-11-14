package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class DonationRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public DonationRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<Long, BigDecimal> sumConfirmedByAnimal(
            Collection<Long> animalIds, LocalDateTime from, LocalDateTime until) {
        if (animalIds.isEmpty()) return Map.of();
        var params = new MapSqlParameterSource("animalIds", animalIds)
                .addValue("from", from)
                .addValue("until", until);
        Map<Long, BigDecimal> totals = new HashMap<>();
        jdbc.query("""
                SELECT id_animal, SUM(suma) AS total
                FROM donatii_bani
                WHERE id_animal IN (:animalIds)
                  AND status_plata = 'confirmata'
                  AND suma > 0
                  AND data_donatie >= :from AND data_donatie < :until
                GROUP BY id_animal
                """, params, (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                totals.put(rs.getLong("id_animal"), rs.getBigDecimal("total")));
        return totals;
    }
}
