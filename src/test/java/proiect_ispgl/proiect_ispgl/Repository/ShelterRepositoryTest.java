package proiect_ispgl.proiect_ispgl.Repository;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import proiect_ispgl.proiect_ispgl.dto.ShelterResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

class ShelterRepositoryTest {
    @Test
    void countsOnlyAvailableAnimalsAndMatchesWholeSpeciesAliases() {
        var database = new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2).build();
        try {
            var jdbc = new JdbcTemplate(database);
            jdbc.execute("""
                    CREATE TABLE centre_animale (
                        id_centru INTEGER PRIMARY KEY, nume_centru VARCHAR, judet VARCHAR, oras VARCHAR,
                        adresa VARCHAR, nr_telefon VARCHAR, program_lucru VARCHAR, email VARCHAR,
                        nr_maxim_animale INTEGER, url_instagram VARCHAR, url_facebook VARCHAR,
                        latitudine NUMERIC(10, 6), longitudine NUMERIC(10, 6)
                    )
                    """);
            jdbc.execute("CREATE TABLE animale (id_centru INTEGER, specie VARCHAR, status VARCHAR)");
            jdbc.update("INSERT INTO centre_animale (id_centru, nume_centru) VALUES (1, 'Centru A'), (2, 'Centru B')");
            jdbc.update("""
                    INSERT INTO animale VALUES
                        (1, 'Pisică', 'Disponibil'), (1, ' PISICI ', 'Disponibil'),
                        (1, ' CAT ', ' AVAILABLE '), (1, 'cats', 'Disponibil'),
                        (1, 'Caine', 'Disponibil'), (1, 'CÂINI', 'Disponibil'),
                        (1, 'Căței', 'Disponibil'), (1, ' Catel ', 'Disponibil'), (1, 'DOGS', 'Disponibil'),
                        (1, 'Catfish', 'Disponibil'), (1, 'Arici', 'Disponibil'),
                        (1, 'Pisică', 'Adoptat'), (2, 'Câine', 'Rezervat'), (NULL, 'dog', 'Available')
                    """);

            var repository = new ShelterRepository(new NamedParameterJdbcTemplate(database));
            var shelters = repository.findShelterCards();

            assertThat(shelters).extracting(ShelterResponseDto::id).containsExactly(1, 2);
            assertThat(shelters.getFirst()).satisfies(shelter -> {
                assertThat(shelter.animalsCount()).isEqualTo(11);
                assertThat(shelter.catsCount()).isEqualTo(4);
                assertThat(shelter.dogsCount()).isEqualTo(5);
                assertThat(shelter.otherCount()).isEqualTo(2);
            });
            assertThat(shelters.getLast().animalsCount()).isZero();
        } finally {
            database.shutdown();
        }
    }
}
