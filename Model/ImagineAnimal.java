package proiect_ispgl.proiect_ispgl.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "imagini_animale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagineAnimal {
    @Id
    @Column(name = "id_imagine")
    private Long id;

    @Column(name = "id_animal")
    private Long animalId;

    @Column(name = "url_imagine")
    private String imageUrl;

    @Column(name = "este_principala")
    private boolean isMain;

}
