package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import proiect_ispgl.proiect_ispgl.dto.MedicalTreatmentDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MedicalTreatmentRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public MedicalTreatmentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Long, List<MedicalTreatmentDto>> findByAnimalIds(Collection<Long> animalIds) {
        if (animalIds.isEmpty()) {
            return Map.of();
        }

        String sql = """
                SELECT fm.id_animal, t.id_tratament, t.nume_tratament,
                       t.categorie, t.descriere, im.data_inregistrarii
                FROM fisa_medicala fm
                JOIN inregistrari_medicale im ON im.nr_fisa = fm.nr_fisa
                JOIN tratamente t ON t.id_tratament = im.id_tratament
                WHERE fm.id_animal IN (:animalIds)
                ORDER BY fm.id_animal, im.data_inregistrarii DESC NULLS LAST, t.id_tratament
                """;

        return jdbcTemplate.query(sql, Map.of("animalIds", animalIds), resultSet -> {
            Map<Long, List<MedicalTreatmentDto>> treatmentsByAnimal = new LinkedHashMap<>();
            while (resultSet.next()) {
                MedicalTreatmentDto treatment = new MedicalTreatmentDto(
                        resultSet.getObject("id_tratament", Integer.class),
                        resultSet.getString("nume_tratament"),
                        resultSet.getString("categorie"),
                        resultSet.getString("descriere"),
                        resultSet.getObject("data_inregistrarii", LocalDate.class)
                );
                treatmentsByAnimal.computeIfAbsent(resultSet.getLong("id_animal"),
                        ignored -> new ArrayList<>()).add(treatment);
            }
            return treatmentsByAnimal;
        });
    }
}
