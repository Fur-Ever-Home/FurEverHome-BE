package proiect_ispgl.proiect_ispgl.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proiect_ispgl.proiect_ispgl.Model.CentruAnimale;

@Repository
public interface CentruAnimaleRepository extends JpaRepository<CentruAnimale, Integer> {
}