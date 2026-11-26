package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import proiect_ispgl.proiect_ispgl.dto.ShelterResponseDto;

import java.util.List;
import java.util.Map;

@Repository
public class ShelterRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ShelterRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ShelterResponseDto> findShelterCards() {
        String sql = """
                WITH available_animals AS (
                    SELECT id_centru, TRANSLATE(LOWER(TRIM(specie)), 'ăâțţ', 'aatt') AS species
                    FROM animale
                    WHERE id_centru IS NOT NULL AND LOWER(TRIM(status)) IN ('disponibil', 'available')
                ), totals AS (
                    SELECT id_centru, COUNT(*) AS animals_count,
                           COUNT(*) FILTER (WHERE species IN ('pisica', 'pisici', 'cat', 'cats')) AS cats_count,
                           COUNT(*) FILTER (WHERE species IN ('caine', 'caini', 'catel', 'catei', 'dog', 'dogs')) AS dogs_count
                    FROM available_animals
                    GROUP BY id_centru
                )
                SELECT c.id_centru, c.nume_centru, c.judet, c.oras, c.adresa, c.nr_telefon,
                       c.program_lucru, c.email, c.nr_maxim_animale, c.url_instagram, c.url_facebook,
                       c.latitudine, c.longitudine,
                       COALESCE(totals.animals_count, 0) AS animals_count,
                       COALESCE(totals.cats_count, 0) AS cats_count,
                       COALESCE(totals.dogs_count, 0) AS dogs_count
                FROM centre_animale c
                LEFT JOIN totals ON totals.id_centru = c.id_centru
                ORDER BY c.id_centru
                """;

        return jdbcTemplate.query(sql, Map.of(), (row, index) -> {
            long animalsCount = row.getLong("animals_count");
            long catsCount = row.getLong("cats_count");
            long dogsCount = row.getLong("dogs_count");
            return new ShelterResponseDto(
                    row.getInt("id_centru"), row.getString("nume_centru"),
                    row.getString("judet"), row.getString("oras"),
                    row.getString("adresa"), row.getString("nr_telefon"),
                    row.getString("program_lucru"), row.getString("email"),
                    row.getObject("nr_maxim_animale", Integer.class),
                    animalsCount, catsCount, dogsCount, animalsCount - catsCount - dogsCount,
                    new ShelterResponseDto.SocialLinks(row.getString("url_instagram"), row.getString("url_facebook")),
                    row.getBigDecimal("latitudine"), row.getBigDecimal("longitudine")
            );
        });
    }
}
