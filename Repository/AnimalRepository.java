package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import proiect_ispgl.proiect_ispgl.Model.Animal;

import java.util.Collection;
import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    //@Query("SELECT a FROM Animal a WHERE " + "(:species IS NULL OR LOWER(a.species) = LOWER(:species) )" + "ORDER BY a.id ASC ")

    List<Animal> findBySpeciesIgnoreCase(String species);

    List<Animal> findBySpeciesNotInIgnoreCase(Collection<String> excludedSpecies);
}
