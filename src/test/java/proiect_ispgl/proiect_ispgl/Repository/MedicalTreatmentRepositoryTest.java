package proiect_ispgl.proiect_ispgl.Repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import proiect_ispgl.proiect_ispgl.dto.MedicalTreatmentDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalTreatmentRepositoryTest {
    private EmbeddedDatabase database;
    private JdbcTemplate jdbc;
    private MedicalTreatmentRepository repository;

    @BeforeEach
    void setUp() {
        database = new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2).build();
        jdbc = new JdbcTemplate(database);
        repository = new MedicalTreatmentRepository(new NamedParameterJdbcTemplate(database));
        jdbc.execute("CREATE TABLE fisa_medicala (nr_fisa INTEGER PRIMARY KEY, id_animal BIGINT NOT NULL)");
        jdbc.execute("""
                CREATE TABLE tratamente (
                    id_tratament INTEGER PRIMARY KEY, nume_tratament VARCHAR(100),
                    categorie VARCHAR(100), descriere TEXT)
                """);
        jdbc.execute("""
                CREATE TABLE inregistrari_medicale (
                    nr_fisa INTEGER, id_tratament INTEGER, data_inregistrarii DATE,
                    PRIMARY KEY (nr_fisa, id_tratament),
                    FOREIGN KEY (nr_fisa) REFERENCES fisa_medicala(nr_fisa),
                    FOREIGN KEY (id_tratament) REFERENCES tratamente(id_tratament))
                """);
    }

    @AfterEach
    void tearDown() {
        database.shutdown();
    }

    @Test
    void joinsTreatmentsToTheirOwnAnimalAndOrdersNewestThenTreatmentId() {
        jdbc.update("INSERT INTO fisa_medicala VALUES (10, 101), (20, 202), (30, 303)");
        jdbc.update("""
                INSERT INTO tratamente VALUES
                    (1, 'Tratament vechi', 'Categorie 1', 'Descriere 1'),
                    (2, 'Control recent', 'Categorie 2', 'Descriere 2'),
                    (3, 'Tratament recent', 'Categorie 3', 'Descriere 3'),
                    (4, 'Istoric fara data', 'Categorie 4', NULL)
                """);
        jdbc.update("""
                INSERT INTO inregistrari_medicale VALUES
                    (10, 1, DATE '2026-10-01'), (10, 3, DATE '2026-11-12'),
                    (10, 2, DATE '2026-11-12'), (10, 4, NULL),
                    (20, 1, DATE '2026-11-14'), (30, 3, DATE '2026-11-15')
                """);

        Map<Long, List<MedicalTreatmentDto>> result = repository.findByAnimalIds(List.of(101L, 202L, 404L));

        assertEquals(Set.of(101L, 202L), result.keySet());
        assertEquals(List.of(2, 3, 1, 4), result.get(101L).stream().map(MedicalTreatmentDto::id).toList());
        assertEquals(new MedicalTreatmentDto(2, "Control recent", "Categorie 2", "Descriere 2",
                LocalDate.of(2026, 11, 12)), result.get(101L).getFirst());
        assertNull(result.get(101L).getLast().recordedAt());
        assertEquals(List.of(1), result.get(202L).stream().map(MedicalTreatmentDto::id).toList());
        assertEquals(LocalDate.of(2026, 11, 14), result.get(202L).getFirst().recordedAt());
        assertFalse(result.containsKey(303L));
    }

    @Test
    void medicalFileWithNoTreatmentProducesNoInventedHistory() {
        jdbc.update("INSERT INTO fisa_medicala VALUES (10, 101)");

        assertEquals(Map.of(), repository.findByAnimalIds(List.of(101L)));
    }

    @Test
    void emptyAnimalIdsNeverIssueSql() {
        NamedParameterJdbcTemplate mockJdbc = mock(NamedParameterJdbcTemplate.class);
        MedicalTreatmentRepository isolated = new MedicalTreatmentRepository(mockJdbc);

        assertEquals(Map.of(), isolated.findByAnimalIds(List.of()));
        verifyNoInteractions(mockJdbc);
    }
}
