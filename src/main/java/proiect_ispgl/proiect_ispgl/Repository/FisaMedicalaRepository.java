package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proiect_ispgl.proiect_ispgl.Model.FisaMedicala;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

@Repository
public interface FisaMedicalaRepository extends JpaRepository<FisaMedicala, Long> {
    Optional<FisaMedicala> findByAnimalId(Long animalId);
    List<FisaMedicala> findByAnimalIdIn(Collection<Long> animalIds);
}
