package proiect_ispgl.proiect_ispgl.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Entity
@Table(name = "fisa_medicala")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FisaMedicala {
    @Id
    @Column(name = "nr_fisa")
    private Long id;

    @Column(name = "id_animal")
    private Long animalId;

    @Column(name = "este_vaccinat")
    private Boolean isVaccinated;

    @Column(name = "este_sterilizat")
    private Boolean isSterilized;

    @Column(name = "este_deparazitat")
    private Boolean isDewormed;

}
