package proiect_ispgl.proiect_ispgl.Repository;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DonationRepositoryTest {
    @Test
    void sumsOnlyPositiveConfirmedAnimalDonationsWithinMonth() {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        var jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE donatii_bani (id_animal BIGINT, suma NUMERIC(12,2), status_plata VARCHAR(30), data_donatie TIMESTAMP)");
        jdbc.execute("""
                INSERT INTO donatii_bani VALUES
                (1, 100, 'confirmata', '2026-11-01 00:00:00'),
                (1, 25, 'confirmata', '2026-11-30 23:59:59.999999'),
                (1, 500, 'in_asteptare', '2026-11-10 12:00:00'),
                (1, 600, 'rambursata', '2026-11-10 12:00:00'),
                (1, 700, 'esuata', '2026-11-10 12:00:00'),
                (1, 800, NULL, '2026-11-10 12:00:00'),
                (1, 900, 'confirmata', '2026-10-31 23:59:59.999999'),
                (1, 1000, 'confirmata', '2026-12-01 00:00:00'),
                (1, -5, 'confirmata', '2026-11-10 12:00:00'),
                (1, 50, 'confirmata', NULL),
                (2, 75, 'confirmata', '2026-11-10 12:00:00'),
                (3, 9999, 'confirmata', '2026-11-10 12:00:00'),
                (NULL, 9999, 'confirmata', '2026-11-10 12:00:00')
                """);
        var repository = new DonationRepository(new NamedParameterJdbcTemplate(source));
        var totals = repository.sumConfirmedByAnimal(List.of(1L, 2L, 4L),
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 12, 1, 0, 0));
        assertThat(totals).containsOnlyKeys(1L, 2L);
        assertThat(totals.get(1L)).isEqualByComparingTo("125");
        assertThat(totals.get(2L)).isEqualByComparingTo("75");
    }

    @Test
    void emptyInputDoesNotNeedSchema() {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:" + UUID.randomUUID());
        var repository = new DonationRepository(new NamedParameterJdbcTemplate(source));
        assertThat(repository.sumConfirmedByAnimal(List.of(), LocalDateTime.now(), LocalDateTime.now())).isEmpty();
    }
}
