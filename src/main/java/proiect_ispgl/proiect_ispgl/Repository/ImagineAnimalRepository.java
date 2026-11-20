package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proiect_ispgl.proiect_ispgl.Model.ImagineAnimal;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

@Repository
public interface ImagineAnimalRepository extends JpaRepository<ImagineAnimal, Long> {
    Optional<ImagineAnimal> findFirstByAnimalIdAndIsMainTrue(Long animalId);

    Optional<ImagineAnimal> findFirstByAnimalId(Long animalId);
    List<ImagineAnimal> findByAnimalIdInOrderByIsMainDescIdAsc(Collection<Long> animalIds);
}
